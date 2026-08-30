package com.notationstudio.data.repository

import com.notationstudio.domain.model.Score
import com.notationstudio.domain.music.ScoreRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryScoreRepository : ScoreRepository {
    private val mutex = Mutex()
    private val scores = linkedMapOf<String, Score>()
    override suspend fun list(): List<Score> = mutex.withLock { scores.values.toList() }
    override suspend fun get(id: String): Score? = mutex.withLock { scores[id] }
    override suspend fun save(score: Score) { mutex.withLock { scores[score.id] = score } }
    override suspend fun delete(id: String) { mutex.withLock { scores.remove(id) } }
}
