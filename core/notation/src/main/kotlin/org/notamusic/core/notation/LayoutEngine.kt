package org.notamusic.core.notation

data class Rect(val left:Float,val top:Float,val right:Float,val bottom:Float)
data class LayoutElement(val element:MusicElement,val bounds:Rect)
data class MeasureLayout(val measure:Measure,val bounds:Rect,val elements:List<LayoutElement>)
class LayoutEngine(private val staffSpace:Float=10f) {
 fun layout(staff:Staff,width:Float):List<MeasureLayout>{
  var x=40f
  return staff.measures.map{m->
   val complexity=m.elements().size
   val measureWidth=(maxOf(120f, complexity*34f + 70f)).coerceAtMost(width.coerceAtLeast(160f))
   val elements=m.elements().map{e-> val px=x+50f+e.start.toDouble().toFloat()/m.duration.toDouble()* (measureWidth-70f); LayoutElement(e,Rect(px-12,40,px+24,100)) }
   val out=MeasureLayout(m,Rect(x,20,x+measureWidth,120),elements); x+=measureWidth; out }
 }
}
