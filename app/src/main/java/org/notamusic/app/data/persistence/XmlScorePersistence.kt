package org.notamusic.app.data.persistence

import android.content.Context
import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.ScorePersistence

class XmlScorePersistence(private val context: Context) : ScorePersistence {
    override fun load(id: String): Score? = null
    override fun save(score: Score) { context.openFileOutput("${score.id.value}.xml", Context.MODE_PRIVATE).use { it.write("<!-- NotaMusic score placeholder -->".toByteArray()) } }
    override fun delete(id: String) { context.deleteFile("$id.xml") }
}
