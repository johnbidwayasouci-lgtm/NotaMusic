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

class ScoreEditorView(context: Context): View(context) {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 private val engine=CompositionEngine()
 private val interaction=EditorInteraction(engine)
 private val groups=linkedMapOf("Fonctions" to listOf(EditorTool.SELECT,EditorTool.ERASER),"Voix" to emptyList(),"Notes" to listOf(EditorTool.WHOLE,EditorTool.HALF,EditorTool.QUARTER,EditorTool.EIGHTH,EditorTool.SIXTEENTH,EditorTool.THIRTY_SECOND),"Silences" to listOf(EditorTool.REST_WHOLE,EditorTool.REST_HALF,EditorTool.REST_QUARTER,EditorTool.REST_EIGHTH,EditorTool.REST_SIXTEENTH,EditorTool.REST_THIRTY_SECOND),"Add-one" to listOf(EditorTool.DOT,EditorTool.DOUBLE_DOT,EditorTool.TIE,EditorTool.SLUR,EditorTool.TUPLET),"Ornements" to listOf(EditorTool.TRILL,EditorTool.MORDENT,EditorTool.INVERTED_MORDENT,EditorTool.TURN,EditorTool.INVERTED_TURN,EditorTool.STACCATO,EditorTool.SHORT_STACCATO,EditorTool.ACCENT,EditorTool.FERMATA,EditorTool.TREMOLO,EditorTool.GRACE),"Dynamiques" to listOf(EditorTool.PPP,EditorTool.PP,EditorTool.P,EditorTool.MP,EditorTool.MF,EditorTool.F,EditorTool.FF,EditorTool.FFF,EditorTool.CRESCENDO,EditorTool.DIMINUENDO))
 private var openGroup="Notes"; private var downX=0f; private var downY=0f; private var scrollX=0f; private var scrollY=0f; private var scale=1f
 private val scaleDetector=ScaleGestureDetector(context,object:ScaleGestureDetector.SimpleOnScaleGestureListener(){override fun onScale(d:ScaleGestureDetector):Boolean{scale=(scale*d.scaleFactor).coerceIn(.6f,2.5f);invalidate();return true}})
 override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(0xfffafafa.toInt());drawToolbar(c);c.save();c.scale(scale,scale);c.translate(-scrollX,90f-scrollY);drawScore(c);c.restore()}
 private fun drawToolbar(c:Canvas){paint.color=0xffeeeeee.toInt();c.drawRect(0f,0f,width.toFloat(),84f,paint);paint.color=0xff202020.toInt();paint.textSize=12f;var x=6f;groups.keys.forEach{name->paint.color=if(name==openGroup)0xffc8d8ff.toInt() else 0xffdddddd.toInt();c.drawRect(x,4f,x+76f,25f,paint);paint.color=0xff202020.toInt();c.drawText(name,x+4f,19f,paint);x+=80f};x=6f;groups[openGroup].orEmpty().forEach{t->paint.color=if(t==interaction.tool)0xff8fb2ff.toInt() else 0xffdddddd.toInt();c.drawRect(x,30f,x+58f,78f,paint);paint.color=0xff202020.toInt();paint.textSize=8f;c.drawText(label(t),x+3f,56f,paint);x+=62f};paint.textSize=11f;c.drawText("Voix ${engine.voice}",width-70f,19f,paint)}
 private fun label(t:EditorTool)=when(t){EditorTool.THIRTY_SECOND->"1/32";EditorTool.SIXTEENTH->"1/16";EditorTool.EIGHTH->"1/8";EditorTool.QUARTER->"1/4";EditorTool.HALF->"1/2";EditorTool.WHOLE->"ronde";EditorTool.ERASER->"gomme";EditorTool.SELECT->"sélect.";else->t.name.lowercase().replace('_',' ')}
 private fun drawScore(c:Canvas){var y=30f;engine.measures.forEachIndexed{mi,m->paint.color=0xff202020.toInt();paint.textSize=12f;c.drawText("${m.number}  𝄞  ${m.beats}/${m.beatUnit}",4f,y-8,paint);repeat(5){c.drawLine(90f,y+it*10,width/scale+scrollX,y+it*10,paint)};val mw=max(180f,m.events.size*55f+130f);c.drawLine(90f,y,90f,y+40,paint);m.events.forEach{e->val x=110f+e.onset.toDouble().toFloat()/m.capacity.toDouble()*mw;val yy=y+35-(e.pitch?:60-60)*1.5f;paint.color=if(e.id==engine.selectedId)0xff3f66cc.toInt() else 0xff202020.toInt();if(e.rest)c.drawRect(x,yy-3,x+16,yy+3,paint) else {c.drawOval(x,yy-5,x+15,yy+5,paint);c.drawLine(x+13,yy,x+13,yy-32,paint);if(e.accidental!=Accidental.NONE)c.drawText(accidentalLabel(e.accidental),x-12,yy+5,paint);repeat(e.dots){c.drawCircle(x+20+it*5,yy,2.5f,paint)};e.dynamic?.let{d->c.drawText(d,x,yy+28,paint)};e.ornament?.let{o->c.drawText("✧",x,yy-12,paint)};if(e.tieStart)c.drawArc(x+10,yy-4,x+70,yy+16,180f,180f,false,paint)}};y+=110f}}
 override fun onTouchEvent(e:MotionEvent):Boolean{scaleDetector.onTouchEvent(e);when(e.action){MotionEvent.ACTION_DOWN->{downX=e.x;downY=e.y;return true};MotionEvent.ACTION_MOVE->{if(e.y>84){scrollX+=(downX-e.x)/scale;scrollY+=(downY-e.y)/scale;scrollX=scrollX.coerceAtLeast(0f);scrollY=scrollY.coerceAtLeast(0f);downX=e.x;downY=e.y;invalidate();return true}};MotionEvent.ACTION_UP->{if(e.y<28){val i=(e.x/80f).toInt();if(i in groups.keys.indices)openGroup=groups.keys.elementAt(i);invalidate();return true};if(e.y in 30f..84f){val i=((e.x-6)/62f).toInt();val ts=groups[openGroup].orEmpty();if(i in ts.indices){interaction.tool=ts[i];invalidate();return true}};if(e.y>=84){val sx=(e.x/scale)+scrollX-110f;val sy=(e.y/scale)-90f+scrollY;val mi=(sy/110f).toInt().coerceIn(0,engine.measures.lastIndex);val m=engine.measures[mi];val mw=max(180f,m.events.size*55f+130f);val onset=Fraction.of((sx.coerceAtLeast(0f)/mw*m.capacity.toDouble()*64).toLong(),64);val hit=interaction.hit(mi,onset);interaction.apply(hit,pitchFromY(sy-mi*110f),4);invalidate();return true}};return true}
 private fun pitchFromY(y:Float)=(60-((y-65f)/1.5f).toInt()).coerceIn(36,84)
 private fun accidentalLabel(a:Accidental)=when(a){Accidental.SHARP->"♯";Accidental.FLAT->"♭";Accidental.NATURAL->"♮";Accidental.DOUBLE_SHARP->"𝄪";Accidental.DOUBLE_FLAT->"𝄫";else->""}
}
