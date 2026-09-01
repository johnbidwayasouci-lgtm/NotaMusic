package org.notamusic.app.ui.editor

import org.notamusic.app.domain.notation.CompositionEngine

object ScoreSession {
    var engine: CompositionEngine = CompositionEngine()
        private set
    var fileName: String = "Untitled"
    var title: String = "Untitled"
    var composer: String = ""
    var tempo: Int = 120

    fun reset(title: String = "Untitled", composer: String = "", tempo: Int = 120) {
        engine = CompositionEngine()
        this.title = title
        this.composer = composer
        this.tempo = tempo
        fileName = title.ifBlank { "Untitled" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}
