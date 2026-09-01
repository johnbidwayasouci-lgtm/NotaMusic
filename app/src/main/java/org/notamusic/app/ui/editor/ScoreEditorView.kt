package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScoreEditorView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isSubpixelText = true }
    private val engine get() = ScoreSession.engine
    private val store = org.notamusic.app.data.persistence.ScoreFileStore(context)
    private val tools = linkedMapOf(
        "Notes" to listOf(EditorTool.WHOLE, EditorTool.HALF, EditorTool.QUARTER, EditorTool.EIGHTH, EditorTool.SIXTEENTH, EditorTool.THIRTY_SECOND),
        "Silences" to listOf(EditorTool.REST_WHOLE, EditorTool.REST_HALF, EditorTool.REST_QUARTER, EditorTool.REST_EIGHTH, EditorTool.REST_SIXTEENTH, EditorTool.REST_THIRTY_SECOND),
        "Ajouts" to listOf(EditorTool.SELECT, EditorTool.ERASER, EditorTool.DOT, EditorTool.DOUBLE_DOT, EditorTool.TIE, EditorTool.SLUR, EditorTool.TUPLET),
        "Ornements" to listOf(EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO, EditorTool.GRACE),
        "Dynamiques" to listOf(EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF, EditorTool.CRESCENDO, EditorTool.DIMINUENDO)
    )
    private var group = "Notes"
    private var tool = EditorTool.QUARTER
    private var accidental = Accidental.NONE
    private var scrollX = 0f
    private var scrollY = 0f
    private var scale = 1f
    private var downX = 0f
    private var downY = 0f
    private var dragging = false
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean { scale = (scale * d.scaleFactor).coerceIn(.65f, 2.4f); invalidate(); return true }
    })

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(0xfffaf9f6.toInt())
        drawToolbar(canvas)
        canvas.save(); canvas.scale(scale, scale); canvas.translate(-scrollX, 92f - scrollY)
        drawScore(canvas)
        canvas.restore()
    }

    private fun drawToolbar(c: Canvas) {
        paint.style = Paint.Style.FILL; paint.color = 0xffe8e5df.toInt(); c.drawRect(0f, 0f, width.toFloat(), 90f, paint)
        paint.textSize = 12f
        var x = 4f
        tools.keys.forEach { name ->
            paint.color = if (name == group) 0xffb9cbe8.toInt() else 0xffd6d2cb.toInt(); c.drawRoundRect(x, 4f, x + 82f, 28f, 4f, 4f, paint)
            paint.color = 0xff202020.toInt(); c.drawText(name, x + 7f, 20f, paint); x += 86f
        }
        x = 4f
        tools[group].orEmpty().forEach { t ->
            paint.color = if (t == tool) 0xff8eaee0.toInt() else 0xffd6d2cb.toInt(); c.drawRoundRect(x, 34f, x + 62f, 86f, 4f, 4f, paint)
            paint.color = 0xff202020.toInt(); paint.textSize = 9f; c.drawText(label(t), x + 4f, 62f, paint); x += 66f
        }
        paint.textSize = 11f; paint.color = 0xff202020.toInt(); c.drawText("v${engine.voice}", width - 35f, 20f, paint)
    }

    private fun label(t: EditorTool) = when (t) {
        EditorTool.WHOLE -> "1/1"; EditorTool.HALF -> "1/2"; EditorTool.QUARTER -> "1/4"; EditorTool.EIGHTH -> "1/8"; EditorTool.SIXTEENTH -> "1/16"; EditorTool.THIRTY_SECOND -> "1/32"
        EditorTool.REST_WHOLE -> "sil 1/1"; EditorTool.REST_HALF -> "sil 1/2"; EditorTool.REST_QUARTER -> "sil 1/4"; EditorTool.REST_EIGHTH -> "sil 1/8"; EditorTool.REST_SIXTEENTH -> "sil 1/16"; EditorTool.REST_THIRTY_SECOND -> "sil 1/32"
        EditorTool.DOT -> "."; EditorTool.DOUBLE_DOT -> ".."; EditorTool.SELECT -> "sélect"; EditorTool.ERASER -> "gomme"; EditorTool.TIE -> "liaison"; EditorTool.SLUR -> "legato"; EditorTool.TUPLET -> "3:2"
        else -> t.name.lowercase().replace('_', ' ')
    }

    private fun drawScore(c: Canvas) {
        var y = 34f
        val usable = max(500f, width / scale - 30f)
        engine.measures.forEachIndexed { index, m ->
            val measureW = measureWidth(m, usable)
            val x = 20f + engine.measures.take(index).sumOf { measureWidth(it, usable).toDouble() }.toFloat()
            drawMeasure(c, m, x, y, measureW)
            y += 122f
        }
        if (engine.measures.isEmpty()) engine.addMeasure(-1)
    }

    private fun measureWidth(m: RationalMeasure, usable: Float): Float {
        val eventCount = m.events.size
        val rhythmWeight = m.events.sumOf { (1.0 / max(.03125, it.duration.toDouble())) }
        return min(max(190f, 155f + eventCount * 38f + rhythmWeight.toFloat() * 2f), max(usable, 190f))
    }

    private fun drawMeasure(c: Canvas, m: RationalMeasure, x: Float, y: Float, w: Float) {
        paint.color = 0xff202020.toInt(); paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.2f
        repeat(5) { c.drawLine(x, y + it * 10f, x + w, y + it * 10f, paint) }
        c.drawLine(x, y, x, y + 40f, paint); c.drawLine(x + w, y, x + w, y + 40f, paint)
        paint.style = Paint.Style.FILL; paint.textSize = 11f; c.drawText("${m.number}", x - 16f, y - 8f, paint)
        paint.textSize = 34f; c.drawText("𝄞", x + 7f, y + 32f, paint)
        paint.textSize = 15f; c.drawText("${m.beats}", x + 42f, y + 12f, paint); c.drawText("${m.beatUnit}", x + 42f, y + 30f, paint)
        val start = x + 68f; val span = max(70f, w - 82f)
        val events = m.events.sortedWith(compareBy<RationalEvent> { it.onset }.thenBy { it.voice })
        events.forEach { drawEvent(c, it, start + it.onset.toDouble().toFloat() / m.capacity.toDouble() * span, y) }
        drawBarline(c, m, x + w, y)
    }

    private fun drawEvent(c: Canvas, e: RationalEvent, x: Float, y: Float) {
        val selected = e.id == engine.selectedId
        val pitch = e.pitch ?: 60
        val staffStep = (pitch - 64) / 2f
        val noteY = y + 32f - staffStep * 5f
        paint.color = if (selected) 0xff355fa3.toInt() else 0xff181818.toInt(); paint.style = Paint.Style.FILL
        if (e.rest) drawRest(c, x, noteY, e.duration) else drawNote(c, e, x, noteY)
        e.dynamic?.let { paint.textSize = 10f; c.drawText(it, x - 5f, y + 61f, paint) }
        e.ornament?.let { paint.textSize = 9f; c.drawText(ornamentGlyph(it), x - 4f, noteY - 14f, paint) }
        if (e.tuplet != null) { paint.textSize = 9f; c.drawText("${e.tuplet.actual}:${e.tuplet.normal}", x - 3f, noteY - 25f, paint) }
        if (e.tieStart) drawCurve(c, x + 5f, noteY + 7f, x + 46f, noteY + 7f)
        if (e.slurStart) drawCurve(c, x, noteY - 10f, x + 48f, noteY - 10f)
    }

    private fun drawNote(c: Canvas, e: RationalEvent, x: Float, y: Float) {
        val filled = e.duration < Fraction.HALF
        paint.style = if (filled) Paint.Style.FILL else Paint.Style.STROKE; paint.strokeWidth = 1.5f
        c.drawOval(x - 6f, y - 4f, x + 6f, y + 4f, paint)
        if (e.duration <= Fraction.QUARTER) c.drawLine(x + 5f, y, x + 5f, y - 28f, paint)
        if (e.duration <= Fraction.EIGHTH) c.drawLine(x + 5f, y - 28f, x + 17f, y - 22f, paint)
        if (e.duration <= Fraction.SIXTEENTH) c.drawLine(x + 5f, y - 22f, x + 17f, y - 16f, paint)
        if (e.duration <= Fraction.THIRTY_SECOND) c.drawLine(x + 5f, y - 16f, x + 17f, y - 10f, paint)
        accidentalGlyph(e.accidental).takeIf { it.isNotEmpty() }?.let { paint.textSize = 13f; c.drawText(it, x - 17f, y + 5f, paint) }
        repeat(e.dots) { c.drawCircle(x + 11f + it * 5f, y, 1.8f, paint) }
    }

    private fun drawRest(c: Canvas, x: Float, y: Float, d: Fraction) {
        paint.style = Paint.Style.FILL
        when {
            d == Fraction.ONE -> c.drawRect(x - 7f, y - 5f, x + 7f, y, paint)
            d == Fraction.HALF -> c.drawRect(x - 7f, y, x + 7f, y + 5f, paint)
            d <= Fraction.EIGHTH -> { val p = Path(); p.moveTo(x, y - 9f); p.lineTo(x + 7f, y); p.lineTo(x - 4f, y + 8f); p.close(); c.drawPath(p, paint) }
            else -> c.drawRect(x - 2f, y - 6f, x + 2f, y + 6f, paint)
        }
    }

    private fun drawBarline(c: Canvas, x: Float, y: Float, m: RationalMeasure? = null) {
        paint.color = 0xff202020.toInt(); paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.2f; c.drawLine(x, y, x, y + 40f, paint)
    }
    private fun drawCurve(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) { val p = Path(); p.moveTo(x1, y1); p.quadTo((x1 + x2) / 2f, y1 + 10f, x2, y2); c.drawPath(p, paint) }
    private fun accidentalGlyph(a: Accidental) = when (a) { Accidental.SHARP -> "♯"; Accidental.FLAT -> "♭"; Accidental.NATURAL -> "♮"; Accidental.DOUBLE_SHARP -> "𝄪"; Accidental.DOUBLE_FLAT -> "𝄫"; else -> "" }
    private fun ornamentGlyph(s: String) = when { s.contains("TRILL") -> "tr"; s.contains("STACCATO") -> "•"; s.contains("FERMATA") -> "𝄐"; s.contains("ACCENT") -> ">"; else -> "~" }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> { downX = ev.x; downY = ev.y; dragging = false; return true }
            MotionEvent.ACTION_MOVE -> {
                if (abs(ev.x - downX) + abs(ev.y - downY) > 8f) dragging = true
                if (dragging && ev.y > 90f) { scrollX = max(0f, scrollX + (downX - ev.x) / scale); scrollY = max(0f, scrollY + (downY - ev.y) / scale); downX = ev.x; downY = ev.y; invalidate() }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) return true
                if (ev.y < 30f) { val i = (ev.x / 86f).toInt(); if (i in tools.keys.indices) group = tools.keys.elementAt(i); invalidate(); return true }
                if (ev.y in 34f..90f) { val i = ((ev.x - 4f) / 66f).toInt(); tools[group]?.getOrNull(i)?.let { tool = it }; invalidate(); return true }
                editAt(ev.x, ev.y); return true
            }
        }
        return true
    }

    private fun editAt(screenX: Float, screenY: Float) {
        val x = screenX / scale + scrollX
        val y = screenY / scale - 92f + scrollY
        var top = 34f
        engine.measures.forEachIndexed { index, m ->
            val w = measureWidth(m, max(500f, width / scale - 30f))
            val left = 20f + engine.measures.take(index).sumOf { measureWidth(it, max(500f, width / scale - 30f)).toDouble() }.toFloat()
            if (y in top - 12f..top + 55f && x in left..left + w) { applyTool(index, m, x, left, w, y - top); return }
            top += 122f
        }
    }

    private fun applyTool(index: Int, m: RationalMeasure, x: Float, left: Float, w: Float, localY: Float) {
        val span = max(70f, w - 82f); val onset = Fraction.of((max(0f, x - (left + 68f)) / span * m.capacity.toDouble() * 256.0).toLong(), 256)
        val hit = m.events.minByOrNull { distance(it.onset, onset).toDouble() }?.takeIf { distance(it.onset, onset) < Fraction.of(1, 16) }
        when (tool) {
            EditorTool.SELECT -> engine.select(index, hit?.id)
            EditorTool.ERASER -> { engine.select(index, hit?.id); engine.removeSelected() }
            EditorTool.TIE -> hit?.let { engine.toggleTie(it.id, index) }
            EditorTool.SLUR -> hit?.let { engine.toggleSlur(it.id, index) }
            EditorTool.DOT -> hit?.let { engine.updateDots(it.id, index, 1) }
            EditorTool.DOUBLE_DOT -> hit?.let { engine.updateDots(it.id, index, 2) }
            EditorTool.TUPLET -> hit?.let { engine.addTuplet(index, listOf(it.id)) }
            EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO -> hit?.let { engine.setOrnament(it.id, index, tool.name) }
            EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF -> hit?.let { engine.setDynamic(it.id, index, tool.name.lowercase()) }
            EditorTool.CRESCENDO, EditorTool.DIMINUENDO -> hit?.let { engine.setDynamic(it.id, index, if (tool == EditorTool.CRESCENDO) "<" else ">") }
            EditorTool.GRACE -> hit?.let { engine.setOrnament(it.id, index, "GRACE") }
            else -> {
                val duration = when (tool) { EditorTool.WHOLE, EditorTool.REST_WHOLE -> MusicalDuration.WHOLE; EditorTool.HALF, EditorTool.REST_HALF -> MusicalDuration.HALF; EditorTool.QUARTER, EditorTool.REST_QUARTER -> MusicalDuration.QUARTER; EditorTool.EIGHTH, EditorTool.REST_EIGHTH -> MusicalDuration.EIGHTH; EditorTool.SIXTEENTH, EditorTool.REST_SIXTEENTH -> MusicalDuration.SIXTEENTH; EditorTool.THIRTY_SECOND, EditorTool.REST_THIRTY_SECOND -> MusicalDuration.THIRTY_SECOND; else -> null }
                duration?.let { d -> engine.add(index, hit?.onset ?: engine.nextFreeOnset(index), d, pitchFromY(localY), 4, tool.name.startsWith("REST"), accidental) }
            }
        }
        invalidate()
    }
    private fun pitchFromY(y: Float): Int = (64 - ((y - 32f) / 5f * 2f).toInt()).coerceIn(36, 84)
    private fun distance(a: Fraction, b: Fraction) = if (a > b) a - b else b - a

    fun saveCurrent(): Boolean = runCatching { store.save(engine, ScoreSession.fileName, ScoreSession.title, ScoreSession.composer, ScoreSession.tempo) }.isSuccess
}
