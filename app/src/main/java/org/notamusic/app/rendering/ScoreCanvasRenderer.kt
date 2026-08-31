package org.notamusic.app.rendering

import android.graphics.Canvas
import android.graphics.Paint
import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.ScoreRenderer

class ScoreCanvasRenderer : ScoreRenderer {
    override fun render(score: Score, viewportWidth: Int, viewportHeight: Int): Any = score
    fun draw(canvas: Canvas, score: Score, paint: Paint, width: Int) {
        paint.textSize = 18f; canvas.drawText(score.metadata.title, 24f, 32f, paint)
        var y = 76f; repeat(5) { canvas.drawLine(24f, y, width - 24f, y, paint); y += 12f }
    }
}
