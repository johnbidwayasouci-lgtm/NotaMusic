package com.notationstudio.rendering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.notationstudio.domain.model.Score

class ScoreCanvasView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var score: Score? = null
    fun setScore(value: Score?) { score = value; invalidate() }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFFFAF8F2.toInt())
        paint.color = 0xFF202124.toInt()
        paint.strokeWidth = 1f
        repeat(5) { canvas.drawLine(24f, 70f + it * 12f, width - 24f, 70f + it * 12f, paint) }
        paint.textSize = 28f
        canvas.drawText(score?.metadata?.title ?: "Nouvelle partition", 24f, 40f, paint)
    }
}
