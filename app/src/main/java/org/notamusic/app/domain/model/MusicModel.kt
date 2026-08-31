package org.notamusic.app.domain.model

import java.util.UUID

@JvmInline value class ScoreId(val value: String)
enum class Accidental { NONE, SHARP, FLAT, NATURAL, DOUBLE_SHARP, DOUBLE_FLAT }
enum class Duration(val quarterUnits: Int) { WHOLE(16), HALF(8), QUARTER(4), EIGHTH(2), SIXTEENTH(1) }
enum class Clef { TREBLE, BASS, ALTO, TENOR, PERCUSSION }
data class KeySignature(val fifths: Int = 0, val minor: Boolean = false)
data class TimeSignature(val beats: Int = 4, val beatType: Int = 4)
data class Tempo(val bpm: Int = 100)
data class Metadata(val title: String = "Untitled", val subtitle: String = "", val composer: String = "", val rights: String = "", val source: String = "")
data class Instrument(val id: String, val name: String, val midiProgram: Int = 0)

sealed interface MusicElement { val id: String; val voice: Int; val onset: Int }
data class Note(val pitch: Int, val octave: Int, val duration: Duration, val accidental: Accidental = Accidental.NONE, val dotted: Boolean = false, override val voice: Int = 1, override val onset: Int = 0, val tie: Tie? = null, val articulation: String? = null, val ornament: Ornament? = null, val dynamic: Dynamic? = null, val selected: Boolean = false, override val id: String = UUID.randomUUID().toString()) : MusicElement
data class Rest(val duration: Duration, val dotted: Boolean = false, override val voice: Int = 1, override val onset: Int = 0, override val id: String = UUID.randomUUID().toString()) : MusicElement
data class GraceNote(val note: Note, val slash: Boolean = true, override val voice: Int = note.voice, override val onset: Int = note.onset, override val id: String = UUID.randomUUID().toString()) : MusicElement
data class Ornament(val type: String, val accidental: Accidental = Accidental.NONE)
data class Tuplet(val elements: List<MusicElement>, val actual: Int = 3, val normal: Int = 2, override val voice: Int = 1, override val onset: Int = 0, override val id: String = UUID.randomUUID().toString()) : MusicElement
data class Tie(val start: Boolean = true, val end: Boolean = false)
data class Slur(val startElementId: String, val endElementId: String, val id: String = UUID.randomUUID().toString())
data class Wedge(val crescendo: Boolean, val startElementId: String, val endElementId: String? = null, val id: String = UUID.randomUUID().toString())
data class Dynamic(val value: String)
data class Barline(val repeat: Repeat? = null, val style: String = "regular")
data class Repeat(val forward: Boolean = false, val backward: Boolean = false, val times: Int? = null)

data class Measure(val number: Int, val timeSignature: TimeSignature = TimeSignature(), val durationUnits: Int = timeSignature.beats * (16 / timeSignature.beatType), val elements: List<MusicElement> = emptyList(), val barline: Barline = Barline(), val voiceCount: Int = 1)
data class Staff(val id: String = UUID.randomUUID().toString(), val instrument: Instrument, val displayName: String = instrument.name, val clef: Clef = Clef.TREBLE, val transposition: Int = 0, val mute: Boolean = false, val volume: Int = 100, val measures: List<Measure> = emptyList())
data class Part(val id: String = UUID.randomUUID().toString(), val name: String, val staves: List<Staff>)
data class Score(val id: ScoreId = ScoreId(UUID.randomUUID().toString()), val metadata: Metadata = Metadata(), val keySignature: KeySignature = KeySignature(), val tempo: Tempo = Tempo(), val parts: List<Part> = emptyList(), val modified: Boolean = false)
