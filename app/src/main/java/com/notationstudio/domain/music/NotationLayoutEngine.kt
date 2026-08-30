package com.notationstudio.domain.music

data class NoteLayout(val id:String,val x:Float,val staffY:Float,val stemUp:Boolean)
data class MeasureLayout(val number:Int,val x:Float,val width:Float,val notes:List<NoteLayout>)

class NotationLayoutEngine {
    fun layout(measures:List<TemporalMeasure>, pixelsPerQuarter:Float=120f):List<MeasureLayout>{
        var x=0f
        return measures.map { m ->
            val elements=m.voices.values.flatten().sortedBy { it.position }
            val density=elements.sumOf { maxOf(1, (it.duration.toDouble()*16).toInt()) }
            val width=maxOf(110f, pixelsPerQuarter*m.capacity.toDouble().toFloat()+density*3f)
            val notes=elements.mapIndexed { index,e ->
                val ratio=(e.position/m.capacity).toDouble().toFloat()
                NoteLayout(e.id,x+ratio*width,120f+(index%7)*6f,index%2==0)
            }
            MeasureLayout(m.number,x,width,notes).also { x+=width+20f }
        }
    }
}
