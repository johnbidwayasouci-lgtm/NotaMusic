package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*

data class Hit(val measure:Int,val event:RationalEvent?=null,val onset:Fraction=Fraction.ZERO)
class EditorInteraction(private val engine:CompositionEngine) {
 var tool=EditorTool.QUARTER; var accidental=Accidental.NONE
 var errorMessage:String?=null; private set
 fun hit(measure:Int,onset:Fraction):Hit { val e=engine.measures.getOrNull(measure)?.events?.minByOrNull{distance(it.onset,onset)}?.takeIf{distance(it.onset,onset)<Fraction.of(1,12)}; return Hit(measure,e,onset) }
 private fun distance(a:Fraction,b:Fraction)=if(a>b)a-b else b-a
 fun apply(hit:Hit,pitch:Int=60,octave:Int=4):Result<Unit>{
  errorMessage=null
  if(tool==EditorTool.SELECT){engine.selectedId=hit.event?.id;engine.selectedMeasure=hit.measure;return Result.success(Unit)}
  if(tool==EditorTool.ERASER){engine.selectedId=hit.event?.id;engine.selectedMeasure=hit.measure;return if(engine.removeSelected())Result.success(Unit) else fail("Aucun élément à supprimer")}
  val duration=when(tool){EditorTool.WHOLE,EditorTool.REST_WHOLE->MusicalDuration.WHOLE;EditorTool.HALF,EditorTool.REST_HALF->MusicalDuration.HALF;EditorTool.QUARTER,EditorTool.REST_QUARTER->MusicalDuration.QUARTER;EditorTool.EIGHTH,EditorTool.REST_EIGHTH->MusicalDuration.EIGHTH;EditorTool.SIXTEENTH,EditorTool.REST_SIXTEENTH->MusicalDuration.SIXTEENTH;EditorTool.THIRTY_SECOND,EditorTool.REST_THIRTY_SECOND->MusicalDuration.THIRTY_SECOND;else->null}
  if(duration!=null){val onset=if(hit.event==null)engine.nextFreeOnset(hit.measure,engine.voice) else hit.event.onset;return engine.add(hit.measure,onset,duration,pitch,octave,tool.name.startsWith("REST"),accidental).map{}}
  val e=hit.event?:return fail("Cet outil nécessite un élément musical")
  engine.selectedId=e.id;engine.selectedMeasure=hit.measure
  when(tool){EditorTool.DOT->engine.updateDots(e.id,hit.measure,1);EditorTool.DOUBLE_DOT->engine.updateDots(e.id,hit.measure,2);EditorTool.TIE->engine.toggleTie(e.id,hit.measure);EditorTool.SLUR->engine.toggleSlur(e.id,hit.measure);EditorTool.TRILL,EditorTool.MORDENT,EditorTool.INVERTED_MORDENT,EditorTool.TURN,EditorTool.INVERTED_TURN,EditorTool.STACCATO,EditorTool.SHORT_STACCATO,EditorTool.ACCENT,EditorTool.FERMATA,EditorTool.TREMOLO,EditorTool.GRACE->engine.setOrnament(e.id,hit.measure,tool.name);EditorTool.PPP,EditorTool.PP,EditorTool.P,EditorTool.MP,EditorTool.MF,EditorTool.F,EditorTool.FF,EditorTool.FFF->engine.setDynamic(e.id,hit.measure,tool.name.lowercase());else->return fail("Cet outil ne s'applique pas ici")}
  return Result.success(Unit)
 }
 private fun fail(m:String)=Result.failure<Unit>(IllegalArgumentException(m)).also{errorMessage=m}
}
