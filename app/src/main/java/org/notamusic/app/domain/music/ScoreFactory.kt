package org.notamusic.app.domain.music

import org.notamusic.app.domain.model.*
import org.notamusic.app.ui.create.CreateScoreDraft
import org.notamusic.app.ui.create.InstrumentCatalog

class ScoreFactory {
    fun create(draft: CreateScoreDraft): Score {
        val parts = draft.instruments.take(15).map { instrument ->
            val option = InstrumentCatalog.options.firstOrNull { it.instrument.id == instrument.id }
            val measures = listOf(Measure(1, draft.timeSignature))
            val staves = if (option?.grandStaff == true) {
                listOf(
                    Staff(instrument = instrument, displayName = "${instrument.name} · RH", clef = Clef.TREBLE, measures = measures),
                    Staff(instrument = instrument, displayName = "${instrument.name} · LH", clef = Clef.BASS, measures = measures)
                )
            } else {
                listOf(
                    Staff(
                        instrument = instrument,
                        displayName = instrument.name,
                        clef = when (instrument.id) {
                            "cello", "double_bass", "bassoon", "trombone", "tuba", "bass" -> Clef.BASS
                            "viola", "alto" -> Clef.ALTO
                            "tenor" -> Clef.TENOR
                            "percussion", "drum_kit" -> Clef.PERCUSSION
                            else -> Clef.TREBLE
                        },
                        transposition = option?.transposition ?: 0,
                        measures = measures
                    )
                )
            }
            Part(name = instrument.name, staves = staves)
        }
        return Score(
            metadata = draft.metadata,
            keySignature = draft.keySignature,
            tempo = draft.tempo,
            parts = parts
        )
    }
}
