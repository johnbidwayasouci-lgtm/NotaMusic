package org.notamusic.core.notation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

class ScoreRenderer {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 fun render(canvas:Canvas, score:Score, scale:Float, scrollX:Float, scrollY:Float, selection:Selection=Selection()){
  canvas.save(); canvas.scale(scale,scale); canvas.translate(-scrollX,-scrollY); var y=40f
  score.parts.forEachIndexed{pi,p-> p.staves.forEachIndexed{si,s->
   paint.style=Paint.Style.FILL; paint.textSize=14f; canvas.drawText(s.name,8f,y+4,paint)
   repeat(5){line-> paint.strokeWidth=1f; canvas.drawLine(60f,y+line*10, canvas.width/scale+scrollX,y+line*10,paint)}
   canvas.drawText(if(s.clef==Clef.BASS) "𝄢" else if(s.clef==Clef.ALTO||s.clef==Clef.TENOR) "𝄡" else "𝄞",65f,y+35,paint)
   LayoutEngine().layout(s,canvas.width/scale).forEach{ml->
    val x=ml.bounds.left; canvas.drawLine(x,y,x,y+40,paint); canvas.drawText(ml.measure.number.toString(),x+3,y-6,paint)
    ml.elements.forEach{le-> val n=le.element as? Note ?: return@forEach; val nx=le.bounds.left; val ny=y+50-(n.pitch.octave-4)*3-"CDEFGAB".indexOf(n.pitch.step.name.first())*2; canvas.drawOval(nx,ny,nx+14,ny+9,paint); canvas.drawLine(nx+12,ny+4,nx+12,ny-28,paint); n.dynamic?.let{canvas.drawText(it.name.lowercase(),nx, y+78,paint)} }
   }; y+=100f
  }}; canvas.restore()
 }
}
