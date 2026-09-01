package org.notamusic.app.midi

import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.music.MidiRenderer
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/** Standard MIDI type-1 renderer used by NotaMusic playback/export. */
class PlaceholderMidiRenderer : MidiRenderer {
    private val ppq = 480

    override fun render(score: Score, output: OutputStream) {
        val tracks = if (score.parts.isEmpty()) listOf(emptyTrack())
        else score.parts.mapIndexed { index, part -> renderPart(part, score.tempo.bpm, index) }
        val header = ByteArrayOutputStream()
        header.write("MThd".toByteArray(Charsets.US_ASCII))
        writeInt(header, 6)
        writeShort(header, if (tracks.size > 1) 1 else 0)
        writeShort(header, tracks.size)
        writeShort(header, ppq)
        output.write(header.toByteArray())
        tracks.forEach(output::write)
    }

    private fun renderPart(part: Part, bpm: Int, index: Int): ByteArray {
        val events = mutableListOf<Event>()
        val micros = 60_000_000L / bpm.coerceIn(20, 300)
        events += Event(0, Meta(0x51, byteArrayOf((micros shr 16).toByte(), (micros shr 8).toByte(), micros.toByte())))
        events += Event(0, Channel(0xC0, byteArrayOf(program(part).coerceIn(0, 127).toByte())))
        events += Event(0, Meta(0x03, part.name.toByteArray(Charsets.UTF_8)))
        part.staves.filterNot { it.mute }.forEach { staff ->
            staff.measures.forEach { measure ->
                measure.elements.filterIsInstance<Note>().forEach { note ->
                    val start = note.onset.toLong() * ppq / 4L
                    val length = ticks(note.duration, note.dots)
                    val midi = (note.pitch + (note.octave - 4) * 12 + accidental(note.accidental)).coerceIn(0, 127)
                    val channel = if (index == 9) 9 else 0
                    events += Event(start, Channel(0x90 or channel, byteArrayOf(midi.toByte(), velocity(note.dynamic).toByte())))
                    events += Event(start + length, Channel(0x80 or channel, byteArrayOf(midi.toByte(), 0)))
                }
            }
        }
        return track(events)
    }

    private fun program(part: Part): Int = part.staves.firstOrNull()?.instrument?.midiProgram ?: 0

    private fun ticks(duration: Duration, dots: Int): Long {
        val base = when (duration) {
            Duration.WHOLE -> 1920L
            Duration.HALF -> 960L
            Duration.QUARTER -> 480L
            Duration.EIGHTH -> 240L
            Duration.SIXTEENTH -> 120L
            Duration.THIRTY_SECOND -> 60L
        }
        return when (dots.coerceIn(0, 2)) {
            1 -> base + base / 2
            2 -> base + base / 2 + base / 4
            else -> base
        }
    }

    private fun accidental(value: Accidental): Int = when (value) {
        Accidental.SHARP -> 1
        Accidental.FLAT -> -1
        Accidental.NATURAL, Accidental.NONE -> 0
        Accidental.DOUBLE_SHARP -> 2
        Accidental.DOUBLE_FLAT -> -2
    }

    private fun velocity(dynamic: Dynamic?): Int = when (dynamic?.value?.lowercase()) {
        "ppp" -> 32; "pp" -> 40; "p" -> 48; "mp" -> 58
        "mf" -> 70; "f" -> 82; "ff" -> 96; "fff" -> 112
        else -> 70
    }

    private sealed interface Payload
    private data class Channel(val status: Int, val data: ByteArray) : Payload
    private data class Meta(val type: Int, val data: ByteArray) : Payload
    private data class Event(val tick: Long, val payload: Payload)

    private fun track(events: List<Event>): ByteArray {
        val body = ByteArrayOutputStream()
        var last = 0L
        events.sortedWith(compareBy<Event> { it.tick }.thenBy { if (it.payload is Channel && (it.payload.status and 0xF0) == 0x80) 0 else 1 })
            .forEach { event ->
                writeVariable(body, (event.tick - last).coerceAtLeast(0))
                when (val p = event.payload) {
                    is Channel -> {
                        body.write(p.status)
                        p.data.forEach { value -> body.write(value.toInt() and 0xFF) }
                    }
                    is Meta -> {
                        body.write(0xFF)
                        body.write(p.type)
                        writeVariable(body, p.data.size.toLong())
                        p.data.forEach { value -> body.write(value.toInt() and 0xFF) }
                    }
                }
                last = event.tick
            }
        body.write(0)
        body.write(0xFF)
        body.write(0x2F)
        body.write(0)
        return chunk("MTrk", body.toByteArray())
    }

    private fun emptyTrack(): ByteArray = track(emptyList())

    private fun chunk(type: String, bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        type.toByteArray(Charsets.US_ASCII).forEach { out.write(it.toInt()) }
        writeInt(out, bytes.size)
        bytes.forEach { out.write(it.toInt() and 0xFF) }
        return out.toByteArray()
    }

    private fun writeVariable(out: ByteArrayOutputStream, value: Long) {
        var buffer = value and 0x7F
        var v = value ushr 7
        while (v != 0L) {
            buffer = (buffer shl 8) or ((v and 0x7F) or 0x80)
            v = v ushr 7
        }
        while (true) {
            out.write((buffer and 0xFF).toInt())
            if ((buffer and 0x80) == 0L) return
            buffer = buffer ushr 8
        }
    }

    private fun writeShort(out: ByteArrayOutputStream, value: Int) {
        out.write((value ushr 8) and 0xFF)
        out.write(value and 0xFF)
    }

    private fun writeInt(out: ByteArrayOutputStream, value: Int) {
        out.write((value ushr 24) and 0xFF)
        out.write((value ushr 16) and 0xFF)
        out.write((value ushr 8) and 0xFF)
        out.write(value and 0xFF)
    }
}
