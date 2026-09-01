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
        engine = CompositionEngine()
        score = null
        this.title = title
        this.composer = composer
        this.tempo = tempo
        this.tempoMarking = "Allegro"
        fileName = safeFileName(title)
    }

    fun start(score: Score) {
        this.score = score
        this.title = score.metadata.title.ifBlank { "Untitled" }
        this.composer = score.metadata.composer
        this.tempo = score.tempo.bpm
        this.tempoMarking = score.tempo.marking
        this.fileName = safeFileName(this.title)
        val firstStaff = score.parts.firstOrNull()?.staves?.firstOrNull()
        engine = CompositionEngine(empty = true)
        if (firstStaff != null && firstStaff.measures.isNotEmpty()) {
            firstStaff.measures.forEach { measure ->
                engine.measures.add(RationalMeasure(measure.number, measure.timeSignature.beats, measure.timeSignature.beatType))
            }
        }
        if (engine.measures.isEmpty()) engine.measures.add(RationalMeasure(1))
    }

    fun syncMetadata(title: String, composer: String, tempo: Int, marking: String = tempoMarking) {
        this.title = title.ifBlank { "Untitled" }
        this.composer = composer
        this.tempo = tempo.coerceIn(20, 400)
        this.tempoMarking = marking.ifBlank { "Allegro" }
        score = score?.copy(
            metadata = score!!.metadata.copy(title = this.title, composer = this.composer),
            tempo = score!!.tempo.copy(bpm = this.tempo, marking = this.tempoMarking)
        )
        fileName = safeFileName(this.title)
    }

    fun currentScore(): Score {
        val template = score
        val parts = template?.parts?.ifEmpty { null } ?: listOf(
            Part(name = title.ifBlank { "Piano" }, staves = listOf(Staff(instrument = Instrument("piano", "Piano", 0))))
        )
        val sourcePart = parts.first()
        val sourceStaff = sourcePart.staves.firstOrNull() ?: Staff(instrument = Instrument("piano", "Piano", 0))
        val measures = engine.measures.map { measure ->
            Measure(
                number = measure.number,
                timeSignature = TimeSignature(measure.beats, measure.beatUnit),
                elements = measure.events.map { event ->
                    val duration = durationFor(event.duration)
                    val onset = (event.onset.toDouble() * 32.0).toInt()
                    if (event.rest || event.pitch == null) {
                        Rest(duration, dots = event.dots, voice = event.voice, onset = onset)
                    } else {
                        Note(
                            pitch = event.pitch,
                            octave = event.octave,
                            duration = duration,
                            accidental = event.accidental,
                            dots = event.dots,
                            voice = event.voice,
                            onset = onset,
                            ornament = event.ornament?.let { Ornament(it) },
                            dynamic = event.dynamic?.let { Dynamic(it) },
                            tie = if (event.tieStart || event.tieEnd) Tie(event.tieStart, event.tieEnd) else null
                        )
                    }
                }
            )
        }
        val updatedStaff = sourceStaff.copy(measures = measures)
        val updatedPart = sourcePart.copy(staves = listOf(updatedStaff) + sourcePart.staves.drop(1))
        return (template ?: Score()).copy(
            metadata = Metadata(title = title, composer = composer),
            tempo = Tempo(tempo, tempoMarking),
            parts = listOf(updatedPart) + parts.drop(1)
        )
    }

    private fun durationFor(value: Fraction): Duration = when {
        value == Fraction.ONE -> Duration.WHOLE
        value == Fraction.HALF -> Duration.HALF
        value == Fraction.QUARTER -> Duration.QUARTER
        value == Fraction.EIGHTH -> Duration.EIGHTH
        value == Fraction.SIXTEENTH -> Duration.SIXTEENTH
        else -> Duration.THIRTY_SECOND
    }

    private fun safeFileName(value: String) = value.ifBlank { "Untitled" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
