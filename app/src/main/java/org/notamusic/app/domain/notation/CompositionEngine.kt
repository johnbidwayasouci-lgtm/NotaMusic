package org.notamusic.app.domain.notation

import org.notamusic.app.domain.model.Accidental

enum class EditorTool { SELECT, ERASER, WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, THIRTY_SECOND, REST_WHOLE, REST_HALF, REST_QUARTER, REST_EIGHTH, REST_SIXTEENTH, REST_THIRTY_SECOND, DOT, DOUBLE_DOT, TIE, SLUR, TUPLET, TRILL, MORDENT, INVERTED_MORDENT, TURN, INVERTED_TURN, STACCATO, SHORT_STACCATO, ACCENT, FERMATA, TREMOLO, GRACE, PPP, PP, P, MP, MF, F, FF, FFF, CRESCENDO, DIMINUENDO }

data class RationalEvent(val id:String, val onset:Fraction, val duration:Fraction, val pitch:Int?, val octave:Int=4, val accidental:Accidental=Accidental.NONE, val voice:Int=1, val rest:Boolean=false, val dots:Int=0, val ornament:String?=null, val dynamic:String?=null, val tieStart:Boolean=false, val tieEnd:Boolean=false, val tuplet:TupletSpec?=null)
data class RationalMeasure(val number:Int, val beats:Int=4, val beatUnit:Int=4, val events:MutableList<RationalEvent> = mutableListOf()) { val capacity get()=Fraction.of(beats.toLong(),beatUnit.toLong()) }
data class TupletSpec(val actual:Int,val normal:Int)

class CompositionEngine {
 val measures=mutableListOf(RationalMeasure(1)); var voice=1; var selectedId:String?=null; var selectedMeasure=0
 private val undo=ArrayDeque<List<RationalMeasure>>(); private val redo=ArrayDeque<List<RationalMeasure>>()
 private fun snap()=measures.map{it.copy(events=it.events.toMutableList())}
 private fun commit(){undo.addLast(snap());redo.clear()}
 fun add(measureIndex:Int,onset:Fraction,duration:MusicalDuration,pitch:Int=60,octave:Int=4,rest:Boolean=false,accidental:Accidental=Accidental.NONE):Result<RationalEvent>{
  val m=measures.getOrNull(measureIndex)?:return Result.failure(IllegalArgumentException("Mesure introuvable")); val d=duration.total(); val end=onset+d
  if(onset<Fraction.ZERO || end>m.capacity)return Result.failure(IllegalArgumentException("La note dépasse la capacité de la mesure"))
  if(m.events.any{it.voice==voice && onset<it.onset+it.duration && end>it.onset})return Result.failure(IllegalArgumentException("La voix $voice contient déjà un élément à cette position"))
  commit(); val e=RationalEvent(java.util.UUID.randomUUID().toString(),onset,d,pitch,octave,accidental,voice,rest,duration.dots);m.events.add(e);m.events.sortBy{it.onset};selectedId=e.id;return Result.success(e)
 }
 fun removeSelected():Boolean{val m=measures.getOrNull(selectedMeasure)?:return false;val i=m.events.indexOfFirst{it.id==selectedId};if(i<0)return false;commit();m.events.removeAt(i);selectedId=null;return true}
 fun undo():Boolean{if(undo.isEmpty())return false;redo.addLast(snap());restore(undo.removeLast());return true}
 fun redo():Boolean{if(redo.isEmpty())return false;undo.addLast(snap());restore(redo.removeLast());return true}
 private fun restore(s:List<RationalMeasure>){measures.clear();measures.addAll(s.map{it.copy(events=it.events.toMutableList())})}
 fun addMeasure(after:Int){commit();val i=(after+1).coerceIn(0,measures.size);measures.add(i,RationalMeasure(0));renumber()}
 fun deleteMeasure(index:Int):Boolean{if(measures.size<=1)return false;commit();measures.removeAt(index.coerceIn(0,measures.lastIndex));renumber();return true}
 private fun renumber(){measures.forEachIndexed{i,m->measures[i]=m.copy(number=i+1)}}
}
