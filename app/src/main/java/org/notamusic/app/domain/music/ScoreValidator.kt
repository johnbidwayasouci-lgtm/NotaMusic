package org.notamusic.app.domain.music

import org.notamusic.app.domain.model.*

class ScoreValidator {
    fun validate(score: Score): List<String> = buildList {
        if (score.parts.isEmpty()) add("A score requires at least one part")
        score.parts.forEach { if (it.staves.isEmpty()) add("Part ${it.name} requires a staff") }
        if (score.tempo.bpm !in 20..400) add("Tempo must be between 20 and 400 BPM")
    }
}
