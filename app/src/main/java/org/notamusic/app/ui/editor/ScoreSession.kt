package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.notation.CompositionEngine
import org.notamusic.app.domain.notation.RationalMeasure

object ScoreSession {
    var engine: CompositionEngine = CompositionEngine()
    var score: Score? = null
    var fileName: String = "Untitled"
    var title: String = "Untitled"
    var composer: String = ""
    var tempo: Int = 120

    fun reset(title: String = "Untitled", composer: String = "", tempo: Int = 120) {
        engine = CompositionEngine()
        score = null
        this.title = title
        this.composer = composer
        this.tempo = tempo
        fileName = safeFileName(title)
    }

    fun start(score: Score) {
        this.score = score
        this.title = score.metadata.title.ifBlank { "Untitled" }
        this.composer = score.metadata.composer
        this.tempo = score.tempo.bpm
        this.fileName = safeFileName(this.title)
        val firstStaff = score.parts.firstOrNull()?.staves?.firstOrNull()
        engine = CompositionEngine(empty = true)
        if (firstStaff != null && firstStaff.measures.isNotEmpty()) {
            firstStaff.measures.forEach { measure ->
                val target = RationalMeasure(measure.number, measure.timeSignature.beats, measure.timeSignature.beatType)
                engine.measures.add(target)
            }
        }
        if (engine.measures.isEmpty()) engine.measures.add(RationalMeasure(1))
    }

    fun syncMetadata(title: String, composer: String, tempo: Int) {
        this.title = title.ifBlank { "Untitled" }
        this.composer = composer
        this.tempo = tempo.coerceIn(20, 400)
        score = score?.copy(
            metadata = score!!.metadata.copy(title = this.title, composer = this.composer),
            tempo = score!!.tempo.copy(bpm = this.tempo)
        )
        fileName = safeFileName(this.title)
    }

    private fun safeFileName(value: String) = value.ifBlank { "Untitled" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
