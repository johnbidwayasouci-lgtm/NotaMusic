package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.Score

data class EditorUiState(val score: Score, val selectedElementId: String? = null, val playing: Boolean = false, val dirty: Boolean = false, val error: String? = null)
