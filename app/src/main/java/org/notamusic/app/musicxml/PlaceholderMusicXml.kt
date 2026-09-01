package org.notamusic.app.musicxml

import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.music.MusicXmlExporter
import org.notamusic.app.domain.music.MusicXmlImporter
import java.io.InputStream
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Document
import org.w3c.dom.Element

/** MusicXML 3.x partwise codec covering the notation model used by NotaMusic. */
class PlaceholderMusicXml : MusicXmlImporter, MusicXmlExporter {
    override fun export(score: Score, output: OutputStream) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val root = document.createElement("score-partwise")
        root.setAttribute("version", "3.1")
        document.appendChild(root)

        if (score.metadata.title.isNotBlank()) {
            root.child("work", document).child("work-title", document, score.metadata.title)
        }
        val identification = root.child("identification", document)
        if (score.metadata.composer.isNotBlank()) {
            identification.child("creator", document, score.metadata.composer).setAttribute("type", "composer")
        }

        val partList = root.child("part-list", document)
        score.parts.forEachIndexed { index, part ->
            val scorePart = partList.child("score-part", document)
            scorePart.setAttribute("id", "P${index + 1}")
            scorePart.child("part-name", document, part.name)
        }

        score.parts.forEachIndexed { index, part ->
            val xmlPart = root.child("part", document)
            xmlPart.setAttribute("id", "P${index + 1}")
            val staff = part.staves.firstOrNull() ?: return@forEachIndexed

            staff.measures.forEach { measure ->
                val xmlMeasure = xmlPart.child("measure", document)
                xmlMeasure.setAttribute("number", measure.number.toString())
                val attributes = xmlMeasure.child("attributes", document)
                attributes.child("divisions", document, "4")
                val key = attributes.child("key", document)
                key.child("fifths", document, score.keySignature.fifths.toString())
                if (score.keySignature.minor) key.child("mode", document, "minor")
                val time = attributes.child("time", document)
                time.child("beats", document, measure.timeSignature.beats.toString())
                time.child("beat-type", document, measure.timeSignature.beatType.toString())
                val clef = attributes.child("clef", document)
                clef.child("sign", document, clefSign(staff.clef))
                clef.child("line", document, clefLine(staff.clef))

                measure.elements.sortedBy { it.onset }.forEach { element ->
                    writeElement(document, xmlMeasure, element)
                }
                writeBarline(document, xmlMeasure, measure.barline)
            }
        }

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.transform(DOMSource(document), StreamResult(output))
    }

    private fun writeElement(document: Document, measure: Element, element: MusicElement) {
        when (element) {
            is Note -> writeNote(document, measure, element)
            is Rest -> writeRest(document, measure, element)
            is GraceNote -> writeNote(document, measure, element.note, true)
            else -> Unit
        }
    }

    private fun writeNote(document: Document, measure: Element, note: Note, grace: Boolean = false) {
        val xml = measure.child("note", document)
        if (grace) xml.child("grace", document)
        val pitch = xml.child("pitch", document)
        pitch.child("step", document, pitchStep(note.pitch, note.accidental))
        accidentalAlter(note.accidental)?.let { pitch.child("alter", document, it.toString()) }
        pitch.child("octave", document, note.octave.toString())
        writeDuration(xml, note.duration)
        writeCommonNoteFields(xml, note.voice, note.dots)
        note.tie?.let { tie ->
            if (tie.start) xml.child("tie", document).setAttribute("type", "start")
            if (tie.end) xml.child("tie", document).setAttribute("type", "stop")
        }
        note.dynamic?.let { dynamic ->
            val direction = measure.child("direction", document)
            direction.child("direction-type", document).child(dynamic.value, document)
        }
    }

    private fun writeRest(document: Document, measure: Element, rest: Rest) {
        val xml = measure.child("note", document)
        xml.child("rest", document)
        writeDuration(xml, rest.duration)
        writeCommonNoteFields(xml, rest.voice, rest.dots)
    }

    private fun writeDuration(note: Element, duration: Duration) {
        // Divisions are quarter-note subdivisions. The current model represents
        // 32nds with the same integer unit as 16ths, so they are exported as 16th
        // duration while retaining the correct MusicXML type for round-tripping.
        val value = when (duration) {
            Duration.WHOLE -> 16
            Duration.HALF -> 8
            Duration.QUARTER -> 4
            Duration.EIGHTH -> 2
            Duration.SIXTEENTH, Duration.THIRTY_SECOND -> 1
        }
        note.child("duration", note.ownerDocument, value.toString())
        note.child("type", note.ownerDocument, durationType(duration))
    }

    private fun writeCommonNoteFields(note: Element, voice: Int, dots: Int) {
        note.child("voice", note.ownerDocument, voice.toString())
        repeat(dots.coerceIn(0, 2)) { note.child("dot", note.ownerDocument) }
    }

    private fun writeBarline(document: Document, measure: Element, barline: Barline) {
        val repeat = barline.repeat ?: return
        if (!repeat.forward && !repeat.backward) return
        val xml = measure.child("barline", document)
        xml.setAttribute("location", if (repeat.forward) "left" else "right")
        xml.child("bar-style", document, barline.style)
        val r = xml.child("repeat", document)
        r.setAttribute("direction", if (repeat.forward) "forward" else "backward")
        repeat.times?.let { r.setAttribute("times", it.toString()) }
    }

    override fun import(input: InputStream): Score {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
        require(document.documentElement.nodeName == "score-partwise") { "Unsupported MusicXML root" }

        val title = firstText(document, "work-title") ?: firstText(document, "movement-title") ?: "Untitled"
        val composer = firstText(document, "creator") ?: ""
        val fifths = firstText(document, "fifths")?.toIntOrNull() ?: 0
        val minor = firstText(document, "mode") == "minor"
        val tempo = firstText(document, "per-minute")?.toIntOrNull()?.let(::Tempo) ?: Tempo()
        val scoreKey = KeySignature(fifths, minor)

        val parts = mutableListOf<Part>()
        val partNodes = document.getElementsByTagName("part")
        for (i in 0 until partNodes.length) {
            val partElement = partNodes.item(i) as? Element ?: continue
            val id = partElement.getAttribute("id").ifBlank { "P${i + 1}" }
            val name = partName(document, id, i)
            val measures = mutableListOf<Measure>()
            val measureNodes = partElement.childElements("measure")
            for (j in 0 until measureNodes.length) {
                val measureElement = measureNodes.item(j) as Element
                val time = measureElement.childElement("attributes")?.childElement("time")
                val beats = time?.childText("beats")?.toIntOrNull() ?: 4
                val beatType = time?.childText("beat-type")?.toIntOrNull() ?: 4
                val clef = parseClef(measureElement.childElement("attributes")?.childElement("clef"))
                val elements = mutableListOf<MusicElement>()
                var onset = 0
                val noteNodes = measureElement.childElements("note")
                for (k in 0 until noteNodes.length) {
                    val note = noteNodes.item(k) as Element
                    val duration = parseDuration(note.childText("type")) ?: continue
                    val dots = note.childElements("dot").length.coerceIn(0, 2)
                    val voice = note.childText("voice")?.toIntOrNull() ?: 1
                    val isGrace = note.childElement("grace") != null
                    val element: MusicElement
                    if (note.childElement("rest") != null) {
                        element = Rest(duration = duration, dots = dots, voice = voice, onset = onset)
                    } else {
                        val pitch = note.childElement("pitch") ?: continue
                        val step = pitch.childText("step") ?: "C"
                        val octave = pitch.childText("octave")?.toIntOrNull() ?: 4
                        val alter = pitch.childText("alter")?.toDoubleOrNull()
                        val accidental = when (alter) {
                            -2.0 -> Accidental.DOUBLE_FLAT
                            -1.0 -> Accidental.FLAT
                            0.0 -> Accidental.NATURAL
                            1.0 -> Accidental.SHARP
                            2.0 -> Accidental.DOUBLE_SHARP
                            else -> Accidental.NONE
                        }
                        val tieNodes = note.childElements("tie")
                        val tie = if (tieNodes.length > 0) {
                            Tie(start = (0 until tieNodes.length).any { (tieNodes.item(it) as Element).getAttribute("type") == "start" },
                                end = (0 until tieNodes.length).any { (tieNodes.item(it) as Element).getAttribute("type") == "stop" })
                        } else null
                        val parsed = Note(basePitch(step), octave, duration, accidental, dots = dots, voice = voice, onset = onset, tie = tie)
                        element = if (isGrace) GraceNote(parsed, slash = note.childElement("grace")?.getAttribute("slash") != "no") else parsed
                    }
                    elements += element
                    if (!isGrace) onset += duration.quarterUnits
                }
                val barline = parseBarline(measureElement)
                measures += Measure(
                    number = measureElement.getAttribute("number").toIntOrNull() ?: j + 1,
                    timeSignature = TimeSignature(beats, beatType),
                    elements = elements,
                    barline = barline,
                    voiceCount = elements.maxOfOrNull { it.voice } ?: 1
                )
                if (clef == Clef.PERCUSSION) Unit
            }
            val instrument = Instrument(id, name)
            parts += Part(id, name, listOf(Staff(instrument = instrument, displayName = name, measures = measures)))
        }
        return Score(metadata = Metadata(title = title, composer = composer), keySignature = scoreKey, tempo = tempo, parts = parts)
    }

    private fun parseBarline(measure: Element): Barline {
        val barline = measure.childElement("barline") ?: return Barline()
        val repeat = barline.childElement("repeat") ?: return Barline(style = barline.childText("bar-style") ?: "regular")
        return Barline(
            repeat = Repeat(forward = repeat.getAttribute("direction") == "forward", backward = repeat.getAttribute("direction") == "backward", times = repeat.getAttribute("times").toIntOrNull()),
            style = barline.childText("bar-style") ?: "regular"
        )
    }

    private fun partName(document: Document, id: String, index: Int): String {
        val nodes = document.getElementsByTagName("score-part")
        for (i in 0 until nodes.length) {
            val part = nodes.item(i) as? Element ?: continue
            if (part.getAttribute("id") == id) return part.childText("part-name") ?: "Part ${index + 1}"
        }
        return "Part ${index + 1}"
    }

    private fun parseDuration(type: String?): Duration? = when (type) {
        "whole" -> Duration.WHOLE
        "half" -> Duration.HALF
        "quarter" -> Duration.QUARTER
        "eighth" -> Duration.EIGHTH
        "16th" -> Duration.SIXTEENTH
        "32nd" -> Duration.THIRTY_SECOND
        else -> null
    }

    private fun durationType(duration: Duration): String = when (duration) {
        Duration.WHOLE -> "whole"
        Duration.HALF -> "half"
        Duration.QUARTER -> "quarter"
        Duration.EIGHTH -> "eighth"
        Duration.SIXTEENTH -> "16th"
        Duration.THIRTY_SECOND -> "32nd"
    }

    private fun clefSign(clef: Clef): String = when (clef) {
        Clef.BASS -> "F"
        Clef.ALTO, Clef.TENOR -> "C"
        Clef.PERCUSSION -> "percussion"
        Clef.TREBLE -> "G"
    }

    private fun clefLine(clef: Clef): String = when (clef) {
        Clef.BASS -> "4"
        Clef.ALTO -> "3"
        Clef.TENOR -> "4"
        Clef.PERCUSSION -> "2"
        Clef.TREBLE -> "2"
    }

    private fun parseClef(clef: Element?): Clef = when (clef?.childText("sign")) {
        "F" -> Clef.BASS
        "C" -> if (clef.childText("line") == "4") Clef.TENOR else Clef.ALTO
        "percussion" -> Clef.PERCUSSION
        else -> Clef.TREBLE
    }

    private fun pitchStep(pitch: Int, accidental: Accidental): String {
        val pc = ((pitch + accidentalAlter(accidental).orZero()) % 12 + 12) % 12
        return when (pc) { 0, 1 -> "C"; 2, 3 -> "D"; 4 -> "E"; 5, 6 -> "F"; 7, 8 -> "G"; 9, 10 -> "A"; else -> "B" }
    }

    private fun basePitch(step: String): Int = when (step.uppercase()) {
        "C" -> 60; "D" -> 62; "E" -> 64; "F" -> 65; "G" -> 67; "A" -> 69; else -> 71
    }

    private fun accidentalAlter(accidental: Accidental): Int? = when (accidental) {
        Accidental.DOUBLE_FLAT -> -2
        Accidental.FLAT -> -1
        Accidental.NATURAL -> 0
        Accidental.SHARP -> 1
        Accidental.DOUBLE_SHARP -> 2
        Accidental.NONE -> null
    }

    private fun Int?.orZero(): Int = this ?: 0

    private fun firstText(document: Document, tag: String): String? = document.getElementsByTagName(tag).item(0)?.textContent?.trim()?.takeIf { it.isNotEmpty() }

    private fun Element.child(tag: String, document: Document, text: String? = null): Element {
        val child = document.createElement(tag)
        if (text != null) child.textContent = text
        appendChild(child)
        return child
    }

    private fun Element.childElement(tag: String): Element? {
        val children = childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node is Element && node.tagName == tag) return node
        }
        return null
    }

    private fun Element.childText(tag: String): String? = childElement(tag)?.textContent?.trim()

    private fun Element.childElements(tag: String): org.w3c.dom.NodeList {
        val matches = mutableListOf<org.w3c.dom.Element>()
        val children = childNodes
        for (i in 0 until children.length) {
            val node = children.item(i)
            if (node is Element && node.tagName == tag) matches += node
        }
        return SimpleNodeList(matches)
    }

    private class SimpleNodeList(private val items: List<Element>) : org.w3c.dom.NodeList {
        override fun getLength(): Int = items.size
        override fun item(index: Int): org.w3c.dom.Node? = items.getOrNull(index)
    }
}
