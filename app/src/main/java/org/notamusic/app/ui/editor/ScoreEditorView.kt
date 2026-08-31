package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*
import kotlin.math.max

class ScoreEditorView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engine = CompositionEngine()
    private val interaction = EditorInteraction(engine)
    private val groups = linkedMapOf(
        "Fonctions" to listOf(EditorTool.SELECT, EditorTool.ERASER),
        "Voix" to emptyList(),
        "Notes" to listOf(EditorTool.WHOLE, EditorTool.HALF, EditorTool.QUARTER, EditorTool.EIGHTH, EditorTool.SIXTEENTH, EditorTool.THIRTY_SECOND),
        "Silences" to listOf(EditorTool.REST_WHOLE, EditorTool.REST_HALF, EditorTool.REST_QUARTER, EditorTool.REST_EIGHTH, EditorTool.REST_SIXTEENTH, EditorTool.REST_THIRTY_SECOND),
        "Add-one" to listOf(EditorTool.DOT, EditorTool.DOUBLE_DOT, EditorTool.TIE, EditorTool.SLUR, EditorTool.TUPLET),
        "Ornements" to listOf(EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO, EditorTool.GRACE),
        "Dynamiques" to listOf(EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF, EditorTool.CRESCENDO, EditorTool.DIMINUENDO)
    )
    private var openGroup = "Notes"
    private var downX = 0f
    private var downY = 0f
    private var scrollX = 0f
    private var scrollY = 0f
    private var scale = 1f
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale = (scale * detector.scaleFactor).coerceIn(0.6f, 2.5f)
            invalidate()
            return true
        }
    })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xfffafafa.toInt())
        drawToolbar(canvas)
        canvas.save()
        canvas.scale(scale, scale)
        canvas.translate(-scrollX, 90f - scrollY)
        drawScore(canvas)
        canvas.restore()
    }

    private fun drawToolbar(canvas: Canvas) {
        paint.color = 0xffeeeeee.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), 84f, paint)
        paint.textSize = 12f
        var x = 6f
        groups.keys.forEach { name ->
            paint.color = if (name == openGroup) 0xffc8d8ff.toInt() else 0xffdddddd.toInt()
            canvas.drawRect(x, 4f, x + 76f, 25f, paint)
            paint.color = 0xff202020.toInt()
            canvas.drawText(name, x + 4f, 19f, paint)
            x += 80f
        }
        x = 6f
        groups[openGroup].orEmpty().forEach { tool ->
            paint.color = if (tool == interaction.tool) 0xff8fb2ff.toInt() else 0xffdddddd.toInt()
            canvas.drawRect(x, 30f, x + 58f, 78f, paint)
            paint.color = 0xff202020.toInt()
            paint.textSize = 8f
            canvas.drawText(label(tool), x + 3f, 56f, paint)
            x += 62f
        }
        paint.textSize = 11f
        canvas.drawText("Voix ${engine.voice}", width - 70f, 19f, paint)
    }

    private fun label(tool: EditorTool): String = when (tool) {
        EditorTool.THIRTY_SECOND -> "1/32"
        EditorTool.SIXTEENTH -> "1/16"
        EditorTool.EIGHTH -> "1/8"
        EditorTool.QUARTER -> "1/4"
        EditorTool.HALF -> "1/2"
        EditorTool.WHOLE -> "ronde"
        EditorTool.ERASER -> "gomme"
        EditorTool.SELECT -> "sélect."
        else -> tool.name.lowercase().replace('_', ' ')
    }

    private fun drawScore(canvas: Canvas) {
        var y = 30f
        engine.measures.forEach { measure ->
            paint.color = 0xff202020.toInt()
            paint.textSize = 12f
            canvas.drawText("${measure.number}  𝄞  ${measure.beats}/${measure.beatUnit}", 4f, y - 8f, paint)
            repeat(5) { staffLine ->
                canvas.drawLine(90f, y + staffLine * 10f, width / scale + scrollX, y + staffLine * 10f, paint)
            }
            val measureWidth = max(180f, measure.events.size * 55f + 130f)
            canvas.drawLine(90f, y, 90f, y + 40f, paint)
            measure.events.forEach { event ->
                val x = 110f + (event.onset.toDouble() / measure.capacity.toDouble()).toFloat() * measureWidth
                val pitchOffset = ((event.pitch ?: 60) - 60) * 1.5f
                val yy = y + 35f - pitchOffset
                paint.color = if (event.id == engine.selectedId) 0xff3f66cc.toInt() else 0xff202020.toInt()
                if (event.rest) {
                    canvas.drawRect(x, yy - 3f, x + 16f, yy + 3f, paint)
                } else {
                    canvas.drawOval(x, yy - 5f, x + 15f, yy + 5f, paint)
                    canvas.drawLine(x + 13f, yy, x + 13f, yy - 32f, paint)
                    val accidentalText = accidentalLabel(event.accidental)
                    if (accidentalText.isNotEmpty()) canvas.drawText(accidentalText, x - 12f, yy + 5f, paint)
                    repeat(event.dots) { dot -> canvas.drawCircle(x + 20f + dot * 5f, yy, 2.5f, paint) }
                    event.dynamic?.let { canvas.drawText(it, x, yy + 28f, paint) }
                    event.ornament?.let { canvas.drawText("✧", x, yy - 12f, paint) }
                    if (event.tieStart) canvas.drawArc(x + 10f, yy - 4f, x + 70f, yy + 16f, 180f, 180f, false, paint)
                }
            }
            y += 110f
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.y > 84f) {
                    scrollX = (scrollX + (downX - event.x) / scale).coerceAtLeast(0f)
                    scrollY = (scrollY + (downY - event.y) / scale).coerceAtLeast(0f)
                    downX = event.x
                    downY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (event.y < 28f) {
                    val index = (event.x / 80f).toInt()
                    if (index in groups.keys.indices) openGroup = groups.keys.elementAt(index)
                    invalidate()
                    return true
                }
                if (event.y in 30f..84f) {
                    val index = ((event.x - 6f) / 62f).toInt()
                    val tools = groups[openGroup].orEmpty()
                    if (index in tools.indices) interaction.tool = tools[index]
                    invalidate()
                    return true
                }
                if (event.y >= 84f && engine.measures.isNotEmpty()) {
                    val sx = event.x / scale + scrollX - 110f
                    val sy = event.y / scale - 90f + scrollY
                    val measureIndex = (sy / 110f).toInt().coerceIn(0, engine.measures.lastIndex)
                    val measure = engine.measures[measureIndex]
                    val measureWidth = max(180f, measure.events.size * 55f + 130f)
                    val capacity = measure.capacity.toDouble()
                    val onset = Fraction.of((sx.coerceAtLeast(0f) / measureWidth * capacity * 64.0).toLong(), 64)
                    val hit = interaction.hit(measureIndex, onset)
                    val pitch = pitchFromY(sy - measureIndex * 110f)
                    interaction.apply(hit, pitch)
                    invalidate()
                    return true
                }
                return true
            }
        }
        return true
    }

    private fun pitchFromY(y: Float): Int = (60 - ((y - 65f) / 1.5f).toInt()).coerceIn(36, 84)

    private fun accidentalLabel(accidental: Accidental): String = when (accidental) {
        Accidental.SHARP -> "♯"
        Accidental.FLAT -> "♭"
        Accidental.NATURAL -> "♮"
        Accidental.DOUBLE_SHARP -> "𝄪"
        Accidental.DOUBLE_FLAT -> "𝄫"
        else -> ""
    }
}
