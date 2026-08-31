package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.*
import kotlin.math.max

data class MeasureLayout(val measure: Measure, val left: Float, val width: Float)
data class StaffLayout(val staff: Staff, val top: Float, val height: Float, val measures: List<MeasureLayout>)

class ScoreLayoutEngine(private val baseMeasureWidth: Float = 220f) {
    fun layout(score: Score, scale: Float = 1f): List<StaffLayout> {
        val result=mutableListOf<StaffLayout>(); var y=36f
        score.parts.flatMap { it.staves }.forEach { staff ->
            var x=72f
            val measures=staff.measures.map { m ->
                val width=max(baseMeasureWidth, baseMeasureWidth + m.elements.size*28f)*scale
                val l=MeasureLayout(m,x,width); x+=width; l
            }
            result += StaffLayout(staff,y,92f*scale,measures); y+=120f*scale
        }
        return result
    }
}
