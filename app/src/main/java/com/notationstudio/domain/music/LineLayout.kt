package com.notationstudio.domain.music

data class LayoutElement(val id: String, val x: Float, val width: Float)
data class LayoutMeasure(val number: Int, val x: Float, val width: Float, val elements: List<LayoutElement>)

data class LayoutLine(val measures: List<LayoutMeasure>, val width: Float)

/** Music-aware horizontal layout. Width grows with temporal density instead of fixed measure widths. */
object ScoreLineLayout {
    fun layout(measures: List<TemporalMeasure>, minMeasureWidth: Float = 90f, unitWidth: Float = 220f, gap: Float = 12f): LayoutLine {
        var x=0f
        val result=measures.map { measure ->
            val density=measure.voices.values.sumOf { it.size }.coerceAtLeast(1)
            val durationFactor=measure.capacity.toDouble().coerceAtLeast(0.125).toFloat()
            val width=maxOf(minMeasureWidth, unitWidth*durationFactor + density*18f)
            val elements=measure.voices.values.flatten().sortedBy { it.position }.map { e ->
                LayoutElement(e.id, x + (e.position / measure.capacity).toDouble().toFloat()*width, 18f)
            }
            val out=LayoutMeasure(measure.number,x,width,elements); x += width+gap; out
        }
        return LayoutLine(result, x)
    }
}
