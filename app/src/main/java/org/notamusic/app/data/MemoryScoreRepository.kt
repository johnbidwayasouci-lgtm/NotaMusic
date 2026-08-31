package org.notamusic.app.data

import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.ScoreRepository

class MemoryScoreRepository : ScoreRepository {
    private val scores = linkedMapOf<String, Score>()
    @Synchronized override fun list() = scores.values.toList()
    @Synchronized override fun get(id: String) = scores[id]
    @Synchronized override fun save(score: Score) { scores[score.id.value] = score }
    @Synchronized override fun delete(id: String) { scores.remove(id) }
}
