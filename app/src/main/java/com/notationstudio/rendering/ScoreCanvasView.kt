package com.notationstudio.rendering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import com.notationstudio.domain.model.*
import kotlin.math.max

class ScoreCanvasView(context: Context) : View(context) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var score:Score?=null
    private var zoom=1f
    private var selectedVoice=1
    private var scrollX=0f
    private var downX=0f
    fun setScore(value:Score?){score=value;invalidate()}
    fun setZoom(value:Float){zoom=value.coerceIn(.6f,2f);invalidate()}
    fun setSelectedVoice(value:Int){selectedVoice=value;invalidate()}
    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas);canvas.drawColor(0xFFFAF8F2.toInt());val s=score?:return
        canvas.save();canvas.scale(zoom,zoom);canvas.translate(-scrollX,0f)
        paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=20f;paint.color=0xFF202124.toInt();canvas.drawText(s.metadata.title,24f,30f,paint)
        paint.typeface=Typeface.DEFAULT;paint.textSize=12f;canvas.drawText("${s.metadata.composer}  ·  ${s.timeSignature.numerator}/${s.timeSignature.denominator}  ·  ${s.tempo.bpm} BPM",24f,50f,paint)
        var y=86f
        s.parts.flatMap{it.staves}.forEachIndexed{index,staff->
            paint.textSize=11f;paint.typeface=Typeface.DEFAULT_BOLD;canvas.drawText(staff.displayName,8f,y-12f,paint)
            paint.typeface=Typeface.DEFAULT;for(line in 0 until 5)canvas.drawLine(72f,y+line*9f,1100f,y+line*9f,paint)
            paint.textSize=26f;canvas.drawText(clefSymbol(staff.clef),78f,y+30f,paint)
            paint.textSize=15f;canvas.drawText("${s.timeSignature.numerator}/${s.timeSignature.denominator}",108f,y+22f,paint)
            var x=160f
            staff.measures.forEach{m->
                paint.strokeWidth=1f;canvas.drawLine(x,y-2f,x,y+38f,paint);paint.textSize=9f;canvas.drawText(m.number.toString(),x+3f,y-5f,paint)
                m.content.forEach{element->if(element is Note && element.voice==selectedVoice){val ny=y+27f-(element.pitch.octave-4)*4f;paint.textSize=18f;canvas.drawText(noteGlyph(element),x+18f,ny,paint);if(element.selected){paint.style=Paint.Style.STROKE;canvas.drawCircle(x+23f,ny-5f,12f,paint);paint.style=Paint.Style.FILL};x+=max(26f,42f/element.duration.denominator*8f)}}
                canvas.drawLine(x,y-2f,x,y+38f,paint);x+=18f
            };y+=82f
        }
        if(s.parts.isEmpty()){paint.textSize=16f;canvas.drawText("Aucune portée — configurez les instruments pour commencer.",24f,y+20f,paint)}
        canvas.restore()
    }
    override fun onTouchEvent(event:MotionEvent):Boolean{when(event.action){MotionEvent.ACTION_DOWN->{downX=event.x;return true};MotionEvent.ACTION_MOVE->{scrollX=max(0f,scrollX+(downX-event.x)/zoom);downX=event.x;invalidate();return true};MotionEvent.ACTION_UP->{invalidate();return true}};return true}
    private fun clefSymbol(c:Clef)=when(c){Clef.TREBLE->"𝄞";Clef.BASS->"𝄢";Clef.ALTO->"𝄡";Clef.TENOR->"𝄡";Clef.PERCUSSION->"𝄪"}
    private fun noteGlyph(n:Note)=if(n.accidentalText().isNotEmpty())n.accidentalText()+"●" else "●"
    private fun Note.accidentalText()=when(pitch.accidental){Accidental.DOUBLE_FLAT->"𝄫";Accidental.FLAT->"♭";Accidental.NATURAL->"♮";Accidental.SHARP->"♯";Accidental.DOUBLE_SHARP->"𝄪";null->""}
}
