package org.notamusic.app.ui.editor

import org.notamusic.app.domain.model.Accidental
import org.notamusic.app.domain.notation.*

data class Hit(val measure:Int,val event:RationalEvent?=null,val onset:Fraction=Fraction.ZERO)
class EditorInteraction(private val engine:CompositionEngine) {
 var tool=EditorTool.QUARTER; var accidental=Accidental.NONE
 fun hit(measure:Int,onset:Fraction):Hit { val e=engine.measures.getOrNull(measure)?.events?.minByOrNull{distance(it.onset,onset)}?.takeIf{distance(it.onset,onset)<Fraction.of(1,12)}; return Hit(measure,e,onset) }
 private fun distance(a:Fraction,b:Fraction)=if(a>b)a-b else b-a
 fun apply(hit:Hit,pitch:Int=60,octave:Int=4):Result<Unit>{
  if(tool==EditorTool.SELECT){engine.selectedId=hit.event?.id;engine.selectedMeasure=hit.measure;return Result.success(Unit)}
  if(tool==EditorTool.ERASER){engine.selectedId=hit.event?.id;engine.selectedMeasure=hit.measure;return if(engine.removeSelected())Result.success(Unit) else Result.failure(IllegalArgumentException("Aucun élément à supprimer"))}
  val duration=when(tool){EditorTool.WHOLE,EditorTool.REST_WHOLE->MusicalDuration.WHOLE;EditorTool.HALF,EditorTool.REST_HALF->MusicalDuration.HALF;EditorTool.QUARTER,EditorTool.REST_QUARTER->MusicalDuration.QUARTER;EditorTool.EIGHTH,EditorTool.REST_EIGHTH->MusicalDuration.EIGHTH;EditorTool.SIXTEENTH,EditorTool.REST_SIXTEENTH->MusicalDuration.SIXTEENTH;EditorTool.THIRTY_SECOND,EditorTool.REST_THIRTY_SECOND->MusicalDuration.THIRTY_SECOND;else->null}
  if(duration!=null)return engine.add(hit.measure,hit.onset,duration,pitch,octave,tool.name.startsWith("REST"),accidental).map{}
  val e=hit.event?:return Result.failure(IllegalArgumentException("Cet outil nécessite une note"));engine.selectedId=e.id;engine.selectedMeasure=hit.measure;return Result.success(Unit)
 }
}
