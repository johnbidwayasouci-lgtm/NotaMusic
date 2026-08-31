package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*
import kotlin.math.max

class ScoreEditorView(context: Context): View(context) {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG); private val engine=CompositionEngine(); private val interaction=EditorInteraction(engine)
 private var downX=0f; private var downY=0f; private var scrollX=0f; private var scrollY=0f
 private val tools=listOf(EditorTool.SELECT,EditorTool.ERASER,EditorTool.WHOLE,EditorTool.HALF,EditorTool.QUARTER,EditorTool.EIGHTH,EditorTool.SIXTEENTH,EditorTool.REST_QUARTER,EditorTool.DOT,EditorTool.TIE,EditorTool.SLUR,EditorTool.TUPLET,EditorTool.TRILL,EditorTool.STACCATO,EditorTool.FERMATA,EditorTool.P,EditorTool.MF,EditorTool.F,EditorTool.CRESCENDO)
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(0xfffafafa.toInt());drawToolbar(c);c.save();c.translate(-scrollX,90f-scrollY);drawScore(c);c.restore()}
 private fun drawToolbar(c:Canvas){paint.color=0xffeeeeee.toInt();c.drawRect(0f,0f,width.toFloat(),84f,paint);paint.color=0xff202020.toInt();paint.textSize=13f;c.drawText("Fonctions",8f,17f,paint);c.drawText("Voix ${engine.voice}",88f,17f,paint);var x=8f;tools.forEach{t->paint.color=if(t==interaction.tool)0xffc8d8ff.toInt() else 0xffdddddd.toInt();c.drawRect(x,28f,x+62f,76f,paint);paint.color=0xff202020.toInt();paint.textSize=9f;c.drawText(t.name.lowercase(),x+3f,56f,paint);x+=66f}}
 private fun drawScore(c:Canvas){var y=30f;engine.measures.forEachIndexed{mi,m->paint.color=0xff202020.toInt();paint.textSize=12f;c.drawText("${m.number}  𝄞  ${m.beats}/${m.beatUnit}",4f,y-8,paint);repeat(5){c.drawLine(90f,y+it*10,width+scrollX,y+it*10,paint)};val mw=max(180f,m.events.size*55f+130f);c.drawLine(90f,y,90f,y+40,paint);m.events.forEach{e->val x=110f+e.onset.toDouble().toFloat()/m.capacity.toDouble()*mw;val yy=y+35-(e.pitch-60)*1.5f;paint.color=if(e.id==engine.selectedId)0xff3f66cc.toInt() else 0xff202020.toInt();if(e.rest)c.drawRect(x,yy-3,x+16,yy+3,paint) else {c.drawOval(x,yy-5,x+15,yy+5,paint);c.drawLine(x+13,yy,x+13,yy-32,paint);if(e.accidental!=Accidental.NONE)c.drawText(accidentalLabel(e.accidental),x-12,yy+5,paint);repeat(e.dots){c.drawCircle(x+20+it*5,yy,2.5f,paint)}}};y+=110f}}
 override fun onTouchEvent(e:MotionEvent):Boolean{when(e.action){MotionEvent.ACTION_DOWN->{downX=e.x;downY=e.y;return true};MotionEvent.ACTION_MOVE->{if(e.y>90){scrollX+=(downX-e.x);scrollY+=(downY-e.y);scrollX=scrollX.coerceAtLeast(0f);scrollY=scrollY.coerceAtLeast(0f);downX=e.x;downY=e.y;invalidate();return true}};MotionEvent.ACTION_UP->{if(e.y<84){val i=((e.x-8)/66).toInt();if(i in tools.indices){interaction.tool=tools[i];invalidate();return true}} else {val sx=e.x+scrollX-110f;val sy=e.y-90f+scrollY;val mi=(sy/110f).toInt().coerceIn(0,engine.measures.lastIndex);val m=engine.measures[mi];val mw=max(180f,m.events.size*55f+130f);val onset=Fraction.of((sx.coerceAtLeast(0f)/mw*m.capacity.toDouble()*64).toLong(),64);val pitch=(60-((sy-(mi*110f+65f))/1.5f).toInt()).coerceIn(36,84);interaction.apply(interaction.hit(mi,onset),pitch,4);invalidate();return true}}};return true}
 private fun accidentalLabel(a:Accidental)=when(a){Accidental.SHARP->"♯";Accidental.FLAT->"♭";Accidental.NATURAL->"♮";Accidental.DOUBLE_SHARP->"𝄪";Accidental.DOUBLE_FLAT->"𝄫";else->""}
}
