package com.notationstudio.data.repository

import com.notationstudio.domain.model.Score
import com.notationstudio.domain.music.ScoreRepository

class InMemoryScoreRepository : ScoreRepository {
    private val scores = linkedMapOf<String, Score>()
    override suspend fun list(): List<Score> = synchronized(scores) { scores.values.toList() }
    override suspend fun get(id: String): Score? = synchronized(scores) { scores[id] }
    override suspend fun save(score: Score) { synchronized(scores) { scores[score.id] = score } }
    override suspend fun delete(id: String) { synchronized(scores) { scores.remove(id) } }
}
