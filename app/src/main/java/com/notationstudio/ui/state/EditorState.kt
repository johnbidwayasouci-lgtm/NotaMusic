package com.notationstudio.ui.state

import com.notationstudio.domain.model.Score

sealed interface LoadState<out T> { data object Loading : LoadState<Nothing>; data class Ready<T>(val value:T):LoadState<T>; data class Error(val message:String):LoadState<Nothing>; data object Empty:LoadState<Nothing> }
data class EditorState(val score: Score? = null, val isDirty: Boolean = false, val loadState: LoadState<Score> = LoadState.Empty, val pendingDestructiveAction: Boolean = false)

class ScoreCreationTransaction {
    private var draft: Score? = null
    fun begin(initial: Score) { draft = initial }
    fun update(value: Score) { check(draft != null) { "Transaction not started" }; draft = value }
    fun preview(): Score? = draft
    fun commit(): Score = checkNotNull(draft) { "Cannot commit an empty transaction" }
    fun rollback() { draft = null }
}
