package com.notationstudio.domain.model

import java.util.UUID

typealias Id = String
fun newId(): Id = UUID.randomUUID().toString()

enum class Step { C, D, E, F, G, A, B }
enum class Duration(val denominator: Int) { WHOLE(1), HALF(2), QUARTER(4), EIGHTH(8), SIXTEENTH(16), THIRTY_SECOND(32) }
enum class Accidental { DOUBLE_FLAT, FLAT, NATURAL, SHARP, DOUBLE_SHARP }
enum class Clef { TREBLE, BASS, ALTO, TENOR, PERCUSSION }
enum class Dynamic { PPP, PP, P, MP, MF, F, FF, FFF }
enum class Barline { NONE, NORMAL, DOUBLE, FINAL, START_REPEAT, END_REPEAT }

data class Pitch(val step: Step, val octave: Int, val accidental: Accidental? = null)
data class TimeSignature(val numerator: Int = 4, val denominator: Int = 4)
data class KeySignature(val fifths: Int = 0, val isMinor: Boolean = false)
data class Tempo(val bpm: Int = 120)
data class Metadata(val title: String = "Untitled", val composer: String = "", val subtitle: String = "")
data class Ornament(val name: String)
data class Tie(val start: Boolean = false, val end: Boolean = false)
data class Slur(val id: Id = newId(), val startElement: Id, val endElement: Id)
data class Wedge(val id: Id = newId(), val crescendo: Boolean, val startElement: Id, val endElement: Id)
data class Repeat(val start: Boolean = false, val end: Boolean = false, val times: Int? = null)

data class DynamicMark(val dynamic: Dynamic)
data class GraceNote(val id: Id = newId(), val pitch: Pitch, val duration: Duration = Duration.EIGHTH)
data class Tuplet(val id: Id = newId(), val actual: Int, val normal: Int, val elements: List<Id> = emptyList())

sealed interface MusicElement { val id: Id }
data class Note(
    override val id: Id = newId(), val pitch: Pitch, val duration: Duration,
    val dotted: Boolean = false, val voice: Int = 1, val positionInMeasure: Int = 0,
    val tie: Tie? = null, val articulation: String? = null, val ornament: Ornament? = null,
    val dynamic: Dynamic? = null, val selected: Boolean = false
) : MusicElement
data class Rest(override val id: Id = newId(), val duration: Duration, val dotted: Boolean = false, val voice: Int = 1, val positionInMeasure: Int = 0) : MusicElement
data class Measure(
    val id: Id = newId(), val number: Int, val timeSignature: TimeSignature,
    val theoreticalDuration: Double, val content: List<MusicElement> = emptyList(),
    val voices: Set<Int> = setOf(1), val barline: Barline = Barline.NORMAL, val repeat: Repeat? = null
)
data class Staff(
    val id: Id = newId(), val instrument: String, val displayName: String, val clef: Clef,
    val transposition: Int = 0, val mute: Boolean = false, val volume: Int = 100,
    val staffIndex: Int = 0, val measures: List<Measure> = emptyList()
)
data class Part(
    val id: Id = newId(), val instrument: String, val displayName: String,
    val staves: List<Staff> = emptyList()
)
data class Score(
    val id: Id = newId(), val metadata: Metadata = Metadata(), val tempo: Tempo = Tempo(),
    val keySignature: KeySignature = KeySignature(), val parts: List<Part> = emptyList(),
    val slurs: List<Slur> = emptyList(), val wedges: List<Wedge> = emptyList(),
    val tuplets: List<Tuplet> = emptyList(), val version: Long = 0L
)
