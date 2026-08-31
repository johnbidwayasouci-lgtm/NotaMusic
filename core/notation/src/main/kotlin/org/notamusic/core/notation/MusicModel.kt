package org.notamusic.core.notation

import java.util.UUID

enum class PitchClass { C,D,E,F,G,A,B }
enum class Accidental { DOUBLE_FLAT, FLAT, NATURAL, SHARP, DOUBLE_SHARP }
enum class Clef { TREBLE, BASS, ALTO, TENOR }
data class Pitch(val step: PitchClass, val octave: Int, val accidental: Accidental? = null)
data class Duration(val base: Fraction, val dots: Int = 0) { val value get() = (0..dots).fold(base) { a, i -> a + base / Fraction.of(1L shl (i + 1), 1) } }
data class TimeSignature(val beats: Int, val beatUnit: Int) { val duration get() = Fraction.of(beats.toLong(), beatUnit.toLong()) }
data class KeySignature(val fifths: Int = 0, val minor: Boolean = false)
data class Metadata(var title:String="", var subtitle:String="", var composer:String="", var copyright:String="", var source:String="", var encoder:String="")
data class Tempo(var bpm:Int=120)

enum class Dynamic { PPP, PP, P, MP, MF, F, FF, FFF }
enum class Ornament { TRILL, MORDENT, INVERTED_MORDENT, TURN, INVERTED_TURN, STACCATO, SHORT_STACCATO, ACCENT, FERMATA, TREMOLO }
enum class WedgeType { CRESCENDO, DIMINUENDO }

data class TupletSpec(val actual:Int, val normal:Int) { init { require(actual>0 && normal>0) } }

sealed interface MusicElement { val id:String; val start:Fraction; val duration:Duration; val voice:Int }
data class Note(override val id:String=UUID.randomUUID().toString(), override val start:Fraction, override val duration:Duration, override val voice:Int=1, val pitch:Pitch, val tieStart:Boolean=false, val tieStop:Boolean=false, val ornaments:Set<Ornament> = emptySet(), val dynamic:Dynamic?=null, val tuplet:TupletSpec?=null):MusicElement
data class Rest(override val id:String=UUID.randomUUID().toString(), override val start:Fraction, override val duration:Duration, override val voice:Int=1, val invisible:Boolean=false):MusicElement
data class GraceNote(val pitch:Pitch, val slashed:Boolean=false, override val id:String=UUID.randomUUID().toString(), override val start:Fraction, override val duration:Duration=Duration(Fraction.of(1,32)), override val voice:Int=1):MusicElement
data class OrnamentElement(override val id:String=UUID.randomUUID().toString(), override val start:Fraction, override val duration:Duration=Duration(Fraction.ZERO), override val voice:Int=1, val type:Ornament):MusicElement
data class Wedge(override val id:String=UUID.randomUUID().toString(), override val start:Fraction, override val duration:Duration, override val voice:Int=1, val type:WedgeType):MusicElement
data class Tie(val from:String,val to:String)
data class Slur(val from:String,val to:String)
data class Barline(val repeatStart:Boolean=false,val repeatEnd:Boolean=false)
data class Measure(val number:Int,val timeSignature:TimeSignature,val voices:MutableMap<Int,MutableList<MusicElement>> = mutableMapOf(),val barline:Barline=Barline()) { val duration get()=timeSignature.duration; fun elements()=voices.values.flatten().sortedBy{it.start} }
data class Staff(var name:String,var clef:Clef=Clef.TREBLE,var measures:MutableList<Measure> = mutableListOf(),var mute:Boolean=false,var volume:Int=100)
data class Instrument(val id:String,val name:String,val midiProgram:Int=0)
data class Part(val id:String,val instrument:Instrument,val staves:MutableList<Staff>)
data class Score(val id:String=UUID.randomUUID().toString(),var metadata:Metadata=Metadata(),var tempo:Tempo=Tempo(),val parts:MutableList<Part> = mutableListOf(),var keySignature:KeySignature=KeySignature())

data class Selection(val part:Int?=null,val staff:Int?=null,val measure:Int?=null,val elementId:String?=null,val time:Fraction?=null)

fun Measure.usedDuration(voice:Int)=voices[voice].orEmpty().fold(Fraction.ZERO){a,e-> val end=e.start+e.duration.value; if(end>a) end else a }
fun Measure.canInsert(start:Fraction,duration:Duration,voice:Int)=start>=Fraction.ZERO && start+duration.value<=this.duration && voices[voice].orEmpty().none{ start<it.start+it.duration.value && start+duration.value>it.start }
