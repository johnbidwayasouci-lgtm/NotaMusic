package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import org.notamusic.app.data.persistence.ScoreFileStore
import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScoreNotationView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val engine get() = ScoreSession.engine
    private val store = ScoreFileStore(context)
    private var category = 0
    private var tool = EditorTool.QUARTER
    private var scrollX = 0f
    private var scrollY = 0f
    private var scale = 1f
    private var downX = 0f
    private var downY = 0f
    private var dragging = false
    private val categories = listOf("Figures", "Silences", "Édition", "Ornements", "Dynamiques")
    private val figureTools = listOf(EditorTool.WHOLE, EditorTool.HALF, EditorTool.QUARTER, EditorTool.EIGHTH, EditorTool.SIXTEENTH, EditorTool.THIRTY_SECOND)
    private val restTools = listOf(EditorTool.REST_WHOLE, EditorTool.REST_HALF, EditorTool.REST_QUARTER, EditorTool.REST_EIGHTH, EditorTool.REST_SIXTEENTH, EditorTool.REST_THIRTY_SECOND)
    private val editTools = listOf(EditorTool.SELECT, EditorTool.ERASER, EditorTool.DOT, EditorTool.DOUBLE_DOT, EditorTool.TIE, EditorTool.SLUR, EditorTool.TUPLET)
    private val ornamentTools = listOf(EditorTool.TRILL, EditorTool.MORDENT, EditorTool.INVERTED_MORDENT, EditorTool.TURN, EditorTool.INVERTED_TURN, EditorTool.STACCATO, EditorTool.SHORT_STACCATO, EditorTool.ACCENT, EditorTool.FERMATA, EditorTool.TREMOLO, EditorTool.GRACE)
    private val dynamicTools = listOf(EditorTool.PPP, EditorTool.PP, EditorTool.P, EditorTool.MP, EditorTool.MF, EditorTool.F, EditorTool.FF, EditorTool.FFF, EditorTool.CRESCENDO, EditorTool.DIMINUENDO)
    private val zoom = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() { override fun onScale(d: ScaleGestureDetector): Boolean { scale = (scale * d.scaleFactor).coerceIn(.7f, 1.8f); invalidate(); return true } })

    override fun onDraw(c: Canvas) {
        c.drawColor(0xffede9e1.toInt())
        drawHeader(c)
        c.save(); c.clipRect(0f, 108f, width.toFloat(), height.toFloat()); c.scale(scale, scale); c.translate(-scrollX, 108f - scrollY); drawScore(c); c.restore()
    }

    private fun drawHeader(c: Canvas) {
        paint.style = Paint.Style.FILL; paint.color = 0xff24262a.toInt(); c.drawRect(0f,0f,width.toFloat(),108f,paint)
        paint.color=0xfff7f3eb.toInt(); paint.typeface=Typeface.DEFAULT_BOLD; paint.textSize=18f; c.drawText("NotaMusic",18f,25f,paint)
        paint.typeface=Typeface.DEFAULT; paint.textSize=9f; paint.color=0xffbcb8b0.toInt(); c.drawText("COMPOSEUR · ÉDITEUR DE PARTITION",18f,41f,paint)
        var x=210f
        categories.forEachIndexed { i,n -> paint.color=if(i==category)0xffd8a94f.toInt() else 0xff383b40.toInt(); c.drawRoundRect(x,8f,x+86f,35f,7f,7f,paint); paint.color=if(i==category)0xff292a2d.toInt() else 0xffddd8cf.toInt(); paint.textSize=10f; c.drawText(n,x+9f,26f,paint); x+=91f }
        paint.color=0xff383b40.toInt(); c.drawRect(0f,47f,width.toFloat(),108f,paint)
        val tools=currentTools(); var bx=10f
        tools.forEach { t -> if(bx<width-60f){ paint.color=if(t==tool)0xffe1b45a.toInt() else 0xfff4f0e8.toInt(); c.drawRoundRect(bx,54f,bx+72f,99f,7f,7f,paint); paint.color=if(t==tool)0xff282a2d.toInt() else 0xff54565a.toInt(); paint.typeface=Typeface.DEFAULT_BOLD; paint.textSize=8f; c.drawText(shortLabel(t),bx+6f,70f,paint); paint.textSize=17f; paint.typeface=Typeface.DEFAULT; c.drawText(glyph(t),bx+8f,91f,paint); bx+=77f } }
    }

    private fun currentTools()=when(category){0->figureTools;1->restTools;2->editTools;3->ornamentTools;else->dynamicTools}
    private fun shortLabel(t:EditorTool)=when(t){EditorTool.WHOLE->"RONDE";EditorTool.HALF->"BLANCHE";EditorTool.QUARTER->"NOIRE";EditorTool.EIGHTH->"CROCHE";EditorTool.SIXTEENTH->"DOUBLE";EditorTool.THIRTY_SECOND->"TRIPLE";EditorTool.REST_WHOLE->"PAUSE";EditorTool.REST_HALF->"DEMI-PAUSE";EditorTool.REST_QUARTER->"SOUPIR";EditorTool.REST_EIGHTH->"DEMI-SOUPIR";EditorTool.REST_SIXTEENTH->"1/16";EditorTool.REST_THIRTY_SECOND->"1/32";EditorTool.SELECT->"SÉLECTION";EditorTool.ERASER->"EFFACER";EditorTool.DOT->"POINT";EditorTool.DOUBLE_DOT->"2 POINTS";EditorTool.TIE->"LIAISON";EditorTool.SLUR->"COULÉ";EditorTool.TUPLET->"TRIOLET";else->t.name.replace('_',' ')}
    private fun glyph(t:EditorTool)=when(t){EditorTool.WHOLE->"𝅝";EditorTool.HALF->"𝅗𝅥";EditorTool.QUARTER->"♩";EditorTool.EIGHTH->"♪";EditorTool.SIXTEENTH->"𝅘𝅥𝅮";EditorTool.THIRTY_SECOND->"𝅘𝅥𝅯";EditorTool.REST_WHOLE->"𝄻";EditorTool.REST_HALF->"𝄼";EditorTool.REST_QUARTER->"𝄽";EditorTool.SELECT->"⌖";EditorTool.ERASER->"⌫";EditorTool.TIE->"⌒";EditorTool.SLUR->"∩";EditorTool.TUPLET->"3";else->"•"}

    private fun drawScore(c:Canvas){
        val pageW=max(900f,width/scale-32f); val left=24f; val top=28f
        engine.measures.chunked(2).forEachIndexed { row,pair -> val y=top+row*178f; pair.forEachIndexed { slot,m->drawMeasure(c,m,left+slot*(pageW/2f),y,pageW/2f-16f,slot==0)} }
        if(engine.measures.isEmpty()){paint.color=0xff77736d.toInt();paint.textSize=18f;c.drawText("Votre partition est vide. Choisissez une figure puis touchez une mesure.",left,top+40f,paint)}
    }
    private fun drawMeasure(c:Canvas,m:RationalMeasure,x:Float,y:Float,w:Float,first:Boolean){
        val st=y+48f; paint.style=Paint.Style.STROKE;paint.strokeWidth=1.35f;paint.color=0xff222428.toInt();repeat(5){i->c.drawLine(x,st+i*10f,x+w,st+i*10f,paint)}
        c.drawLine(x+w,st,x+w,st+40f,paint)
        paint.style=Paint.Style.FILL;paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=10f;paint.color=0xff6b665e.toInt();c.drawText("${m.number}",x,y+14f,paint)
        if(first){paint.typeface=Typeface.DEFAULT;paint.textSize=40f;paint.color=0xff1d1f22.toInt();c.drawText("𝄞",x+8f,st+31f,paint);paint.typeface=Typeface.DEFAULT_BOLD;paint.textSize=12f;c.drawText("${m.beats}",x+49f,st+12f,paint);c.drawText("${m.beatUnit}",x+49f,st+30f,paint)}
        val start=x+if(first)78f else 14f;val span=w-(start-x)-10f
        m.events.sortedWith(compareBy<RationalEvent>{it.onset}.thenBy{it.voice}).forEach{e->drawEvent(c,e,start+(e.onset.toDouble()/m.capacity.toDouble()).toFloat()*span,st)}
    }
    private fun drawEvent(c:Canvas,e:RationalEvent,x:Float,st:Float){
        val y=st+32f-((e.pitch?:60)-64)*2.5f
        if(e.id==engine.selectedId){paint.style=Paint.Style.FILL;paint.color=0x38537ea6;c.drawRoundRect(x-17f,y-22f,x+23f,y+22f,6f,6f,paint)}
        paint.color=0xff15171a.toInt();if(e.rest)drawRest(c,x,st+20f,e.duration)else drawNote(c,e,x,y)
        e.dynamic?.let{paint.textSize=10f;paint.typeface=Typeface.DEFAULT_BOLD;c.drawText(it,x-5f,st+61f,paint)}
        e.ornament?.let{paint.textSize=11f;paint.typeface=Typeface.DEFAULT;c.drawText(ornament(it),x-5f,y-15f,paint)}
        e.tuplet?.let{paint.textSize=9f;paint.typeface=Typeface.DEFAULT_BOLD;c.drawText("${it.actual}",x-3f,y-29f,paint)}
        if(e.tieStart)curve(c,x+4f,y+9f,x+48f,y+9f,false);if(e.slurStart)curve(c,x,y-10f,x+48f,y-10f,true)
    }
    private fun drawNote(c:Canvas,e:RationalEvent,x:Float,y:Float){paint.style=if(e.duration>=Fraction.HALF)Paint.Style.STROKE else Paint.Style.FILL;paint.strokeWidth=1.7f;c.drawOval(x-7f,y-5f,x+7f,y+5f,paint);if(e.duration<=Fraction.QUARTER)c.drawLine(x+6f,y,x+6f,y-31f,paint);if(e.duration<=Fraction.EIGHTH)c.drawLine(x+6f,y-31f,x+20f,y-25f,paint);if(e.duration<=Fraction.SIXTEENTH)c.drawLine(x+6f,y-25f,x+20f,y-19f,paint);if(e.duration<=Fraction.THIRTY_SECOND)c.drawLine(x+6f,y-19f,x+20f,y-13f,paint);val a=accidental(e.accidental);if(a.isNotEmpty()){paint.textSize=15f;paint.typeface=Typeface.DEFAULT;c.drawText(a,x-21f,y+6f,paint)};repeat(e.dots){i->c.drawCircle(x+12f+i*5f,y,1.8f,paint)}}
    private fun drawRest(c:Canvas,x:Float,y:Float,d:Fraction){paint.style=Paint.Style.FILL;when{d==Fraction.ONE->c.drawRect(x-7f,y-7f,x+7f,y,paint);d==Fraction.HALF->c.drawRect(x-7f,y,x+7f,y+7f,paint);d<=Fraction.EIGHTH->{val p=Path();p.moveTo(x,y-9f);p.lineTo(x+7f,y);p.lineTo(x-4f,y+9f);p.close();c.drawPath(p,paint)}else->c.drawRect(x-2f,y-7f,x+2f,y+7f,paint)}}
    private fun curve(c:Canvas,x1:Float,y1:Float,x2:Float,y2:Float,above:Boolean){paint.style=Paint.Style.STROKE;paint.strokeWidth=1.2f;val p=Path();p.moveTo(x1,y1);p.quadTo((x1+x2)/2f,y1+if(above)-12f else 12f,x2,y2);c.drawPath(p,paint)}
    private fun ornament(s:String)=when{ s.contains("STACCATO")->"•";s.contains("FERMATA")->"𝄐";s.contains("TRILL")->"tr";s.contains("MORDENT")->"𝆭";else->"~" }
    private fun accidental(a:Accidental)=when(a){Accidental.SHARP->"♯";Accidental.FLAT->"♭";Accidental.NATURAL->"♮";Accidental.DOUBLE_SHARP->"𝄪";Accidental.DOUBLE_FLAT->"𝄫";else->""}

    override fun onTouchEvent(e:MotionEvent):Boolean{zoom.onTouchEvent(e);when(e.actionMasked){MotionEvent.ACTION_DOWN->{downX=e.x;downY=e.y;dragging=false;return true};MotionEvent.ACTION_MOVE->{if(abs(e.x-downX)+abs(e.y-downY)>8f)dragging=true;if(dragging&&e.y>108f){scrollX=max(0f,scrollX+(downX-e.x)/scale);scrollY=max(0f,scrollY+(downY-e.y)/scale);downX=e.x;downY=e.y;invalidate()};return true};MotionEvent.ACTION_UP->{if(dragging)return true;if(e.y<44f){category=((e.x-210f)/91f).toInt().coerceIn(0,categories.lastIndex);tool=currentTools().first();invalidate();return true};if(e.y in 48f..106f){val i=((e.x-10f)/77f).toInt();currentTools().getOrNull(i)?.let{tool=it};invalidate();return true};editAt(e.x,e.y);return true}};return true}
    private fun editAt(sx:Float,sy:Float){val x=sx/scale+scrollX;val y=sy/scale-108f+scrollY;val pageW=max(900f,width/scale-32f);val row=(y/178f).toInt();val slot=min(1,(x/(pageW/2f)).toInt());val index=row*2+slot;val m=engine.measures.getOrNull(index)?:return;val mx=24f+slot*(pageW/2f);val w=pageW/2f-16f;if(x !in mx..mx+w)return;val start=mx+if(slot==0)78f else 14f;val span=w-(start-mx)-10f;val onset=Fraction.of((((x-start).coerceIn(0f,span)/span)*m.capacity.toDouble()*256.0).toLong(),256);val hit=m.events.minByOrNull{distance(it.onset,onset).toDouble()}?.takeIf{distance(it.onset,onset)<Fraction.of(1,10)};apply(index,hit,onset,y-row*178f-28f)}
    private fun apply(index:Int,hit:RationalEvent?,onset:Fraction,y:Float){val d=duration(tool);if(d!=null&&hit!=null){engine.select(index,hit.id);engine.updateDuration(hit.id,index,d)}else when(tool){EditorTool.SELECT->engine.select(index,hit?.id);EditorTool.ERASER->{engine.select(index,hit?.id);engine.removeSelected()};EditorTool.DOT->hit?.let{engine.updateDots(it.id,index,1)};EditorTool.DOUBLE_DOT->hit?.let{engine.updateDots(it.id,index,2)};EditorTool.TIE->hit?.let{engine.toggleTie(it.id,index)};EditorTool.SLUR->hit?.let{engine.toggleSlur(it.id,index)};EditorTool.TUPLET->hit?.let{engine.addTuplet(index,listOf(it.id))};EditorTool.TRILL,EditorTool.MORDENT,EditorTool.INVERTED_MORDENT,EditorTool.TURN,EditorTool.INVERTED_TURN,EditorTool.STACCATO,EditorTool.SHORT_STACCATO,EditorTool.ACCENT,EditorTool.FERMATA,EditorTool.TREMOLO,EditorTool.GRACE->hit?.let{engine.setOrnament(it.id,index,tool.name)};EditorTool.PPP,EditorTool.PP,EditorTool.P,EditorTool.MP,EditorTool.MF,EditorTool.F,EditorTool.FF,EditorTool.FFF->hit?.let{engine.setDynamic(it.id,index,tool.name.lowercase())};EditorTool.CRESCENDO,EditorTool.DIMINUENDO->hit?.let{engine.setDynamic(it.id,index,if(tool==EditorTool.CRESCENDO)"<" else ">")};else->d?.let{engine.add(index,hit?.onset?:engine.nextFreeOnset(index),it,64,4,tool.name.startsWith("REST"),Accidental.NONE)}};invalidate()}
    private fun duration(t:EditorTool)=when(t){EditorTool.WHOLE,EditorTool.REST_WHOLE->MusicalDuration.WHOLE;EditorTool.HALF,EditorTool.REST_HALF->MusicalDuration.HALF;EditorTool.QUARTER,EditorTool.REST_QUARTER->MusicalDuration.QUARTER;EditorTool.EIGHTH,EditorTool.REST_EIGHTH->MusicalDuration.EIGHTH;EditorTool.SIXTEENTH,EditorTool.REST_SIXTEENTH->MusicalDuration.SIXTEENTH;EditorTool.THIRTY_SECOND,EditorTool.REST_THIRTY_SECOND->MusicalDuration.THIRTY_SECOND;else->null}
    private fun distance(a:Fraction,b:Fraction)=if(a>b)a-b else b-a
    fun saveCurrent()=runCatching{store.save(engine,ScoreSession.fileName,ScoreSession.title,ScoreSession.composer,ScoreSession.tempo)}.isSuccess
}
