package org.notamusic.app.data.persistence

import android.content.Context
import org.notamusic.app.domain.notation.CompositionEngine
import org.notamusic.app.domain.notation.Fraction
import org.notamusic.app.domain.notation.MusicalDuration
import org.notamusic.app.domain.notation.RationalEvent
import org.notamusic.app.domain.notation.RationalMeasure
import java.io.File
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Element

class ScoreFileStore(private val context: Context) {
    private val dir: File get() = context.filesDir
    private fun file(name: String): File = File(dir, safeName(name) + ".xml")

    fun list(): List<String> = dir.listFiles { f -> f.isFile && f.extension == "xml" && f.name != "settings.xml" }
        ?.sortedByDescending { it.lastModified() }
        ?.map { it.nameWithoutExtension } ?: emptyList()

    fun save(engine: CompositionEngine, name: String, title: String, composer: String, tempo: Int) {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val root = doc.createElement("notamusic-score").also { doc.appendChild(it) }
        root.setAttribute("version", "1")
        root.appendChild(doc.createElement("title").also { it.textContent = title })
        root.appendChild(doc.createElement("composer").also { it.textContent = composer })
        root.appendChild(doc.createElement("tempo").also { it.textContent = tempo.toString() })
        val measures = doc.createElement("measures").also { root.appendChild(it) }
        engine.measures.forEach { m ->
            val me = doc.createElement("measure").also { measures.appendChild(it) }
            me.setAttribute("number", m.number.toString())
            me.setAttribute("beats", m.beats.toString())
            me.setAttribute("beat-unit", m.beatUnit.toString())
            m.events.forEach { e ->
                val ee = doc.createElement(if (e.rest) "rest" else "note").also { me.appendChild(it) }
                ee.setAttribute("id", e.id)
                ee.setAttribute("onset", "${e.onset.n}/${e.onset.d}")
                ee.setAttribute("duration", "${e.duration.n}/${e.duration.d}")
                ee.setAttribute("voice", e.voice.toString())
                ee.setAttribute("dots", e.dots.toString())
                if (!e.rest) {
                    ee.setAttribute("pitch", (e.pitch ?: 60).toString())
                    ee.setAttribute("octave", e.octave.toString())
                    ee.setAttribute("accidental", e.accidental.name)
                    e.ornament?.let { ee.setAttribute("ornament", it) }
                    e.dynamic?.let { ee.setAttribute("dynamic", it) }
                    ee.setAttribute("tie-start", e.tieStart.toString())
                    ee.setAttribute("tie-end", e.tieEnd.toString())
                    ee.setAttribute("slur-start", e.slurStart.toString())
                    ee.setAttribute("slur-end", e.slurEnd.toString())
                    e.tuplet?.let { t -> ee.setAttribute("tuplet", "${t.actual}:${t.normal}") }
                }
            }
        }
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        }
        transformer.transform(DOMSource(doc), StreamResult(file(name)))
    }

    fun load(name: String): LoadedScore? {
        val f = file(name)
        if (!f.exists()) return null
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f)
        val root = doc.documentElement
        val title = root.childText("title").ifBlank { name }
        val composer = root.childText("composer")
        val tempo = root.childText("tempo").toIntOrNull() ?: 120
        val engine = CompositionEngine(empty = true)
        val list = root.getElementsByTagName("measure")
        for (i in 0 until list.length) {
            val me = list.item(i) as Element
            val number = me.getAttribute("number").toIntOrNull() ?: i + 1
            val beats = me.getAttribute("beats").toIntOrNull() ?: 4
            val beatUnit = me.getAttribute("beat-unit").toIntOrNull() ?: 4
            engine.measures.add(RationalMeasure(number, beats, beatUnit))
            val target = engine.measures.last()
            val notes = me.childNodes
            for (j in 0 until notes.length) {
                val node = notes.item(j) as? Element ?: continue
                if (node.tagName != "note" && node.tagName != "rest") continue
                val onset = parseFraction(node.getAttribute("onset"))
                val duration = parseFraction(node.getAttribute("duration"))
                val voice = node.getAttribute("voice").toIntOrNull() ?: 1
                val dots = node.getAttribute("dots").toIntOrNull() ?: 0
                val rest = node.tagName == "rest"
                val e = RationalEvent(
                    id = node.getAttribute("id").ifBlank { UUID.randomUUID().toString() },
                    onset = onset, duration = duration,
                    pitch = if (rest) null else node.getAttribute("pitch").toIntOrNull() ?: 60,
                    octave = node.getAttribute("octave").toIntOrNull() ?: 4,
                    accidental = runCatching { org.notamusic.app.domain.model.Accidental.valueOf(node.getAttribute("accidental")) }.getOrDefault(org.notamusic.app.domain.model.Accidental.NONE),
                    voice = voice, rest = rest, dots = dots,
                    ornament = node.getAttribute("ornament").ifBlank { null },
                    dynamic = node.getAttribute("dynamic").ifBlank { null },
                    tieStart = node.getAttribute("tie-start").toBoolean(),
                    tieEnd = node.getAttribute("tie-end").toBoolean(),
                    slurStart = node.getAttribute("slur-start").toBoolean(),
                    slurEnd = node.getAttribute("slur-end").toBoolean()
                )
                target.events.add(e)
            }
        }
        if (engine.measures.isEmpty()) engine.measures.add(RationalMeasure(1))
        return LoadedScore(engine, title, composer, tempo)
    }

    fun delete(name: String): Boolean = file(name).delete()

    private fun safeName(name: String) = name.ifBlank { "Untitled" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
    private fun parseFraction(s: String): Fraction {
        val p = s.split('/')
        return if (p.size == 2) Fraction.of(p[0].toLongOrNull() ?: 0, p[1].toLongOrNull() ?: 1) else Fraction.ZERO
    }
    private fun Element.childText(tag: String): String = getElementsByTagName(tag).item(0)?.textContent.orEmpty()

    data class LoadedScore(val engine: CompositionEngine, val title: String, val composer: String, val tempo: Int)
}
