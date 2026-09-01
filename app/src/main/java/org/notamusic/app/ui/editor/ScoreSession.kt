package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.notation.CompositionEngine
import org.notamusic.app.domain.notation.Fraction
import org.notamusic.app.domain.notation.RationalMeasure

object ScoreSession {
    var engine: CompositionEngine = CompositionEngine()
    var score: Score? = null
    var fileName: String = "Untitled"
    var title: String = "Untitled"
    var composer: String = ""
    var tempo: Int = 120
    var tempoMarking: String = "Allegro"

    fun reset(title: String = "Untitled", composer: String = "", tempo: Int = 120) {
        engine = CompositionEngine(); score = null; this.title = title; this.composer = composer; this.tempo = tempo; this.tempoMarking = "Allegro"; fileName = safeFileName(title)
    }
    fun start(score: Score) {
        this.score=score; title=score.metadata.title.ifBlank{"Untitled"}; composer=score.metadata.composer; tempo=score.tempo.bpm; tempoMarking=score.tempo.marking; fileName=safeFileName(title)
        val firstStaff=score.parts.firstOrNull()?.staves?.firstOrNull(); engine=CompositionEngine(empty=true)
        firstStaff?.measures?.forEach{m->engine.measures.add(RationalMeasure(m.number,m.timeSignature.beats,m.timeSignature.beatType))}
        if(engine.measures.isEmpty())engine.measures.add(RationalMeasure(1))
    }
    fun syncMetadata(title:String,composer:String,tempo:Int,marking:String=tempoMarking){this.title=title.ifBlank{"Untitled"};this.composer=composer;this.tempo=tempo.coerceIn(20,400);this.tempoMarking=marking.ifBlank{"Allegro"};score=score?.copy(metadata=score!!.metadata.copy(title=this.title,composer=this.composer),tempo=score!!.tempo.copy(bpm=this.tempo,marking=this.tempoMarking));fileName=safeFileName(this.title)}
    fun currentScore(): Score {
        val template=score; val parts=template?.parts?.takeIf{it.isNotEmpty()}?:listOf(Part(name=title.ifBlank{"Piano"},staves=listOf(Staff(instrument=Instrument("piano","Piano",0)))))
        val sourcePart=parts.first(); val sourceStaff=sourcePart.staves.firstOrNull()?:Staff(instrument=Instrument("piano","Piano",0))
        val measures=engine.measures.map{m->Measure(m.number,TimeSignature(m.beats,m.beatUnit),m.events.map{e->val d=durationFor(e.duration);val onset=(e.onset.toDouble()*32.0).toInt();if(e.rest||e.pitch==null)Rest(d,dots=e.dots,voice=e.voice,onset=onset)else Note(e.pitch,e.octave,d,e.accidental,dots=e.dots,voice=e.voice,onset=onset,ornament=e.ornament?.let{Ornament(it)},dynamic=e.dynamic?.let{Dynamic(it)},tie=if(e.tieStart||e.tieEnd)Tie(e.tieStart,e.tieEnd)else null)})}
        val updatedStaff=sourceStaff.copy(measures=measures); val updatedPart=sourcePart.copy(staves=listOf(updatedStaff)+sourcePart.staves.drop(1))
        return (template?:Score()).copy(metadata=Metadata(title=title,composer=composer),tempo=Tempo(tempo,tempoMarking),parts=listOf(updatedPart)+parts.drop(1))
    }
    private fun durationFor(v:Fraction):Duration=when{v==Fraction.ONE->Duration.WHOLE;v==Fraction.HALF->Duration.HALF;v==Fraction.QUARTER->Duration.QUARTER;v==Fraction.EIGHTH->Duration.EIGHTH;v==Fraction.SIXTEENTH->Duration.SIXTEENTH;else->Duration.THIRTY_SECOND}
    private fun safeFileName(v:String)=v.ifBlank{"Untitled"}.replace(Regex("[^A-Za-z0-9._-]"),"_")
}
