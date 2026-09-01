package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.notamusic.app.data.persistence.ScoreFileStore
import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*
import kotlin.math.abs
import kotlin.math.max

class ScoreEditorView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engine get() = ScoreSession.engine
    private val store = ScoreFileStore(context)
    private val groups = linkedMapOf(
        "Notes" to listOf(EditorTool.WHOLE, EditorTool.HALF, EditorTool.QUARTER, EditorTool.EIGHTH, EditorTool.SIXTEENTH, EditorTool.THIRTY_SECOND),
        "Silences" to listOf(EditorTool.REST_WHOLE, EditorTool.REST_HALF, EditorTool.REST_QUARTER, EditorTool.REST_EIGHTH, EditorTool.REST_SIXTEENTH, EditorTool.REST_THIRTY_SECOND),
        "Ajouts" to listOf(EditorTool.SELECT, EditorTool.ERASER, EditorTool.DOT, EditorTool.DOUBLE_DOT, EditorTool.TIE, EditorTool.SLUR, EditorTool.TUPLET),
        "Ornements" to listOf(EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO, EditorTool.GRACE),
        "Dynamiques" to listOf(EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF, EditorTool.CRESCENDO, EditorTool.DIMINUENDO)
    )
    private var group = "Notes"
    private var tool = EditorTool.QUARTER
    private var scrollX = 0f
    private var scrollY = 0f
    private var scale = 1f
    private var lastX = 0f
    private var lastY = 0f
    private var moving = false
    private val zoom = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean { scale = (scale * d.scaleFactor).coerceIn(0.6f, 2.5f); invalidate(); return true }
    })

    override fun onDraw(c: Canvas) {
        c.drawColor(0xfffaf9f6.toInt())
        drawToolbar(c)
        c.save(); c.scale(scale, scale); c.translate(-scrollX, 92f - scrollY)
        var y = 35f
        engine.measures.forEach { m -> drawMeasure(c, m, 20f, y, measureWidth(m)); y += 122f }
        c.restore()
    }

    private fun drawToolbar(c: Canvas) {
        paint.style = Paint.Style.FILL; paint.color = 0xffe6e2dc.toInt(); c.drawRect(0f, 0f, width.toFloat(), 90f, paint)
        var x = 4f; paint.textSize = 11f
        groups.keys.forEach { n ->
            paint.color = if (n == group) 0xffb9cbe8.toInt() else 0xffd3d0ca.toInt(); c.drawRect(x, 4f, x + 82f, 28f, paint)
            paint.color = 0xff202020.toInt(); c.drawText(n, x + 5f, 20f, paint); x += 86f
        }
        x = 4f
        groups[group].orEmpty().forEach { t ->
            paint.color = if (t == tool) 0xff8eafe0.toInt() else 0xffd3d0ca.toInt(); c.drawRect(x, 34f, x + 62f, 86f, paint)
            paint.color = 0xff202020.toInt(); paint.textSize = 8f; c.drawText(label(t), x + 4f, 62f, paint); x += 66f
        }
    }

    private fun label(t: EditorTool) = when (t) {
        EditorTool.WHOLE -> "1/1"; EditorTool.HALF -> "1/2"; EditorTool.QUARTER -> "1/4"; EditorTool.EIGHTH -> "1/8"; EditorTool.SIXTEENTH -> "1/16"; EditorTool.THIRTY_SECOND -> "1/32"
        EditorTool.REST_WHOLE -> "sil 1/1"; EditorTool.REST_HALF -> "sil 1/2"; EditorTool.REST_QUARTER -> "sil 1/4"; EditorTool.REST_EIGHTH -> "sil 1/8"; EditorTool.REST_SIXTEENTH -> "sil 1/16"; EditorTool.REST_THIRTY_SECOND -> "sil 1/32"
        EditorTool.DOT -> "."; EditorTool.DOUBLE_DOT -> ".."; EditorTool.SELECT -> "select"; EditorTool.ERASER -> "gomme"; EditorTool.TIE -> "tie"; EditorTool.SLUR -> "slur"; EditorTool.TUPLET -> "3:2"
        else -> t.name.lowercase()
    }

    private fun measureWidth(m: RationalMeasure) = max(210f, 170f + m.events.size * 42f + m.events.sumOf { 1.0 / max(0.03125, it.duration.toDouble()) }.toFloat() * 1.5f)

    private fun drawMeasure(c: Canvas, m: RationalMeasure, x: Float, y: Float, w: Float) {
        paint.color = 0xff202020.toInt(); paint.style = Paint.Style.STROKE; paint.strokeWidth = 1.2f
        repeat(5) { i -> c.drawLine(x, y + i * 10f, x + w, y + i * 10f, paint) }
        c.drawLine(x, y, x, y + 40f, paint); c.drawLine(x + w, y, x + w, y + 40f, paint)
        paint.style = Paint.Style.FILL; paint.textSize = 11f; c.drawText(m.number.toString(), x - 16f, y - 7f, paint)
        paint.textSize = 30f; c.drawText("𝄞", x + 7f, y + 31f, paint)
        paint.textSize = 14f; c.drawText(m.beats.toString(), x + 42f, y + 12f, paint); c.drawText(m.beatUnit.toString(), x + 42f, y + 30f, paint)
        val start = x + 68f; val span = w - 82f
        m.events.sortedBy { it.onset }.forEach { e ->
            val px = (start + (e.onset.toDouble() / m.capacity.toDouble()).toFloat() * span)
            drawEvent(c, e, px, y)
        }
    }

    private fun drawEvent(c: Canvas, e: RationalEvent, x: Float, y: Float) {
        paint.color = if (e.id == engine.selectedId) 0xff315d9f.toInt() else 0xff181818.toInt()
        val noteY = y + 32f - ((e.pitch ?: 60) - 64) / 2f * 5f
        if (e.rest) drawRest(c, x, noteY, e.duration) else drawNote(c, e, x, noteY)
        e.dynamic?.let { paint.textSize = 9f; c.drawText(it, x - 5f, y + 61f, paint) }
        e.ornament?.let { paint.textSize = 8f; c.drawText(if (it.contains("STACCATO")) "•" else if (it.contains("FERMATA")) "𝄐" else "~", x - 4f, noteY - 14f, paint) }
        e.tuplet?.let { paint.textSize = 8f; c.drawText("${it.actual}:${it.normal}", x - 4f, noteY - 24f, paint) }
        if (e.tieStart) curve(c, x + 4f, noteY + 8f, x + 48f, noteY + 8f)
        if (e.slurStart) curve(c, x, noteY - 10f, x + 48f, noteY - 10f)
    }

    private fun drawNote(c: Canvas, e: RationalEvent, x: Float, y: Float) {
        paint.style = if (e.duration < Fraction.HALF) Paint.Style.FILL else Paint.Style.STROKE; paint.strokeWidth = 1.5f
        c.drawOval(x - 6f, y - 4f, x + 6f, y + 4f, paint)
        if (e.duration <= Fraction.QUARTER) c.drawLine(x + 5f, y, x + 5f, y - 28f, paint)
        if (e.duration <= Fraction.EIGHTH) c.drawLine(x + 5f, y - 28f, x + 17f, y - 22f, paint)
        if (e.duration <= Fraction.SIXTEENTH) c.drawLine(x + 5f, y - 22f, x + 17f, y - 16f, paint)
        if (e.duration <= Fraction.THIRTY_SECOND) c.drawLine(x + 5f, y - 16f, x + 17f, y - 10f, paint)
        accidental(e.accidental).takeIf { it.isNotEmpty() }?.let { paint.textSize = 13f; c.drawText(it, x - 17f, y + 5f, paint) }
        repeat(e.dots) { i -> c.drawCircle(x + 11f + i * 5f, y, 1.7f, paint) }
    }

    private fun drawRest(c: Canvas, x: Float, y: Float, d: Fraction) {
        paint.style = Paint.Style.FILL
        when { d == Fraction.ONE -> c.drawRect(x - 7f, y - 5f, x + 7f, y, paint); d == Fraction.HALF -> c.drawRect(x - 7f, y, x + 7f, y + 5f, paint); d <= Fraction.EIGHTH -> { val q = Path(); q.moveTo(x, y - 8f); q.lineTo(x + 7f, y); q.lineTo(x - 4f, y + 8f); q.close(); c.drawPath(q, paint) }; else -> c.drawRect(x - 2f, y - 6f, x + 2f, y + 6f, paint) }
    }

    private fun curve(c: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) { val q = Path(); q.moveTo(x1, y1); q.quadTo((x1 + x2) / 2f, y1 + 10f, x2, y2); c.drawPath(q, paint) }
    private fun accidental(a: Accidental) = when (a) { Accidental.SHARP -> "♯"; Accidental.FLAT -> "♭"; Accidental.NATURAL -> "♮"; Accidental.DOUBLE_SHARP -> "𝄪"; Accidental.DOUBLE_FLAT -> "𝄫"; else -> "" }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        zoom.onTouchEvent(e)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lastX = e.x; lastY = e.y; moving = false; return true }
            MotionEvent.ACTION_MOVE -> { if (abs(e.x - lastX) + abs(e.y - lastY) > 8f) moving = true; if (moving && e.y > 90f) { scrollX = max(0f, scrollX + (lastX - e.x) / scale); scrollY = max(0f, scrollY + (lastY - e.y) / scale); lastX = e.x; lastY = e.y; invalidate() }; return true }
            MotionEvent.ACTION_UP -> { if (moving) return true; if (e.y < 30f) { groups.keys.elementAtOrNull((e.x / 86f).toInt())?.let { group = it }; invalidate(); return true }; if (e.y >= 34f && e.y <= 90f) { groups[group]?.getOrNull(((e.x - 4f) / 66f).toInt())?.let { tool = it }; invalidate(); return true }; edit(e.x, e.y); return true }
        }
        return true
    }

    private fun edit(sx: Float, sy: Float) {
        val x = sx / scale + scrollX; val targetY = sy / scale - 92f + scrollY
        var top = 35f
        engine.measures.forEachIndexed { index, m ->
            val w = measureWidth(m)
            if (targetY in top - 12f..top + 55f && x in 20f..20f + w) {
                val span = w - 82f; val onset = Fraction.of((((x - 88f).coerceAtLeast(0f) / span) * m.capacity.toDouble() * 256.0).toLong(), 256)
                val hit = m.events.minByOrNull { distance(it.onset, onset).toDouble() }?.let { candidate -> if (distance(candidate.onset, onset) < Fraction.of(1, 16)) candidate else null }
                apply(index, hit, onset, targetY - top); return
            }
            top += 122f
        }
    }

    private fun apply(index: Int, hit: RationalEvent?, onset: Fraction, y: Float) {
        when (tool) {
            EditorTool.SELECT -> engine.select(index, hit?.id)
            EditorTool.ERASER -> { engine.select(index, hit?.id); engine.removeSelected() }
            EditorTool.DOT -> hit?.let { engine.updateDots(it.id, index, 1) }
            EditorTool.DOUBLE_DOT -> hit?.let { engine.updateDots(it.id, index, 2) }
            EditorTool.TIE -> hit?.let { engine.toggleTie(it.id, index) }
            EditorTool.SLUR -> hit?.let { engine.toggleSlur(it.id, index) }
            EditorTool.TUPLET -> hit?.let { engine.addTuplet(index, listOf(it.id)) }
            EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO, EditorTool.GRACE -> hit?.let { engine.setOrnament(it.id, index, tool.name) }
            EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF -> hit?.let { engine.setDynamic(it.id, index, tool.name.lowercase()) }
            EditorTool.CRESCENDO, EditorTool.DIMINUENDO -> hit?.let { engine.setDynamic(it.id, index, if (tool == EditorTool.CRESCENDO) "<" else ">") }
            else -> durationFor(tool)?.let { d -> engine.add(index, hit?.onset ?: engine.nextFreeOnset(index), d, pitch(y), 4, tool.name.startsWith("REST"), Accidental.NONE) }
        }
        invalidate()
    }

    private fun durationFor(t: EditorTool) = when (t) { EditorTool.WHOLE, EditorTool.REST_WHOLE -> MusicalDuration.WHOLE; EditorTool.HALF, EditorTool.REST_HALF -> MusicalDuration.HALF; EditorTool.QUARTER, EditorTool.REST_QUARTER -> MusicalDuration.QUARTER; EditorTool.EIGHTH, EditorTool.REST_EIGHTH -> MusicalDuration.EIGHTH; EditorTool.SIXTEENTH, EditorTool.REST_SIXTEENTH -> MusicalDuration.SIXTEENTH; EditorTool.THIRTY_SECOND, EditorTool.REST_THIRTY_SECOND -> MusicalDuration.THIRTY_SECOND; else -> null }
    private fun pitch(y: Float) = (64f - (y - 32f) / 5f * 2f).toInt().coerceIn(36, 84)
    private fun distance(a: Fraction, b: Fraction) = if (a > b) a - b else b - a
    fun saveCurrent() = runCatching { store.save(engine, ScoreSession.fileName, ScoreSession.title, ScoreSession.composer, ScoreSession.tempo) }.isSuccess
}
