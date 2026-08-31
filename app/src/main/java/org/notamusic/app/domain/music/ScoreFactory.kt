package org.notamusic.app.domain.music

import org.notamusic.app.domain.model.*
import org.notamusic.app.ui.create.CreateScoreDraft

class ScoreFactory {
    fun create(draft: CreateScoreDraft): Score {
        val parts = draft.instruments.map { instrument ->
            val staff = Staff(instrument = instrument, displayName = instrument.name, measures = listOf(Measure(1, draft.timeSignature)))
            Part(name = instrument.name, staves = listOf(staff))
        }
        return Score(metadata = draft.metadata, keySignature = draft.keySignature, tempo = draft.tempo, parts = parts)
    }
}
