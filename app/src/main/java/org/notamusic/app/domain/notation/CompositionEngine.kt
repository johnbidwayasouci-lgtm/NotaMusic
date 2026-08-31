package org.notamusic.app.domain.notation

import org.notamusic.app.domain.model.Accidental

enum class EditorTool { SELECT, ERASER, WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, THIRTY_SECOND, REST_WHOLE, REST_HALF, REST_QUARTER, REST_EIGHTH, REST_SIXTEENTH, REST_THIRTY_SECOND, DOT, DOUBLE_DOT, TIE, SLUR, TUPLET, TRILL, MORDENT, INVERTED_MORDENT, TURN, INVERTED_TURN, STACCATO, SHORT_STACCATO, ACCENT, FERMATA, TREMOLO, GRACE, PPP, PP, P, MP, MF, F, FF, FFF, CRESCENDO, DIMINUENDO }

data class RationalEvent(val id:String, val onset:Fraction, val duration:Fraction, val pitch:Int?, val octave:Int=4, val accidental:Accidental=Accidental.NONE, val voice:Int=1, val rest:Boolean=false, val dots:Int=0, val ornament:String?=null, val dynamic:String?=null, val tieStart:Boolean=false, val tieEnd:Boolean=false, val tuplet:TupletSpec?=null, val slurStart:Boolean=false, val slurEnd:Boolean=false)
data class RationalMeasure(val number:Int, val beats:Int=4, val beatUnit:Int=4, val events:MutableList<RationalEvent> = mutableListOf()) { val capacity get()=Fraction.of(beats.toLong(),beatUnit.toLong()) }
data class TupletSpec(val actual:Int,val normal:Int)

class CompositionEngine {
 val measures=mutableListOf(RationalMeasure(1)); var voice=1; var selectedId:String?=null; var selectedMeasure=0
 private val undo=ArrayDeque<List<RationalMeasure>>(); private val redo=ArrayDeque<List<RationalMeasure>>()
 private fun snap()=measures.map{it.copy(events=it.events.toMutableList())}
 private fun commit(){undo.addLast(snap());redo.clear()}
 fun nextFreeOnset(measureIndex:Int, requestedVoice:Int=voice):Fraction { val events=measures[measureIndex].events.filter{it.voice==requestedVoice}.sortedBy{it.onset}; var p=Fraction.ZERO; for(e in events){if(e.onset>p)return p; val end=e.onset+e.duration;if(end>p)p=end}; return p }
 fun add(measureIndex:Int,onset:Fraction,duration:MusicalDuration,pitch:Int=60,octave:Int=4,rest:Boolean=false,accidental:Accidental=Accidental.NONE):Result<RationalEvent>{
  val m=measures.getOrNull(measureIndex)?:return Result.failure(IllegalArgumentException("Mesure introuvable")); val d=duration.total(); val end=onset+d
  if(onset<Fraction.ZERO || end>m.capacity)return Result.failure(IllegalArgumentException("La note dépasse la capacité de la mesure"))
  if(m.events.any{it.voice==voice && onset<it.onset+it.duration && end>it.onset})return Result.failure(IllegalArgumentException("La voix $voice contient déjà un élément à cette position"))
  commit(); val e=RationalEvent(java.util.UUID.randomUUID().toString(),onset,d,pitch,octave,accidental,voice,rest,duration.dots);m.events.add(e);m.events.sortWith(compareBy<RationalEvent>{it.onset}.thenBy{it.voice});selectedId=e.id;selectedMeasure=measureIndex;return Result.success(e)
 }
 private fun update(measure:Int,id:String,fn:(RationalEvent)->RationalEvent):Boolean { val m=measures.getOrNull(measure)?:return false; val i=m.events.indexOfFirst{it.id==id}; if(i<0)return false; commit();m.events[i]=fn(m.events[i]);return true }
 fun updateDots(id:String,measure:Int,dots:Int)=update(measure,id){it.copy(dots=dots.coerceIn(0,2))}
 fun updatePitch(id:String,measure:Int,pitch:Int,accidental:Accidental=Accidental.NONE)=update(measure,id){it.copy(pitch=pitch.coerceIn(0,127),accidental=accidental)}
 fun setVoice(id:String,measure:Int,newVoice:Int):Result<Unit>{if(newVoice<1)return Result.failure(IllegalArgumentException("Voix invalide"));val e=measures.getOrNull(measure)?.events?.firstOrNull{it.id==id}?:return Result.failure(IllegalArgumentException("Élément introuvable"));if(measures[measure].events.any{it.id!=id&&it.voice==newVoice&&e.onset<it.onset+it.duration&&e.onset+e.duration>it.onset})return Result.failure(IllegalArgumentException("Chevauchement dans la voix $newVoice"));update(measure,id){it.copy(voice=newVoice)};return Result.success(Unit)}
 fun toggleTie(id:String,measure:Int):Boolean { val m=measures.getOrNull(measure)?:return false;val i=m.events.indexOfFirst{it.id==id};if(i<0)return false;val e=m.events[i];if(e.rest)return false;val target=m.events.firstOrNull{it.voice==e.voice&&it.onset==e.onset+e.duration&&it.pitch==e.pitch&&!it.rest}?:return false;commit();m.events[i]=e.copy(tieStart=!e.tieStart);val ti=m.events.indexOfFirst{it.id==target.id};m.events[ti]=target.copy(tieEnd=!target.tieEnd);return true }
 fun toggleSlur(id:String,measure:Int):Boolean { val m=measures.getOrNull(measure)?:return false;val i=m.events.indexOfFirst{it.id==id};if(i<0)return false;commit();m.events[i]=m.events[i].copy(slurStart=!m.events[i].slurStart);return true }
 fun setOrnament(id:String,measure:Int,name:String):Boolean=update(measure,id){if(it.rest)it else it.copy(ornament=name)}
 fun setDynamic(id:String,measure:Int,name:String):Boolean=update(measure,id){if(it.rest)it else it.copy(dynamic=name)}
 fun addTuplet(measure:Int,ids:List<String>,actual:Int,normal:Int):Result<Unit>{if(actual<=0||normal<=0||ids.isEmpty())return Result.failure(IllegalArgumentException("Tuplet invalide"));val m=measures.getOrNull(measure)?:return Result.failure(IllegalArgumentException("Mesure introuvable"));val selected=m.events.filter{it.id in ids};if(selected.size!=ids.size||selected.any{it.rest})return Result.failure(IllegalArgumentException("Un tuplet doit contenir des notes valides"));val start=selected.minOf{it.onset};val end=selected.maxOf{it.onset+it.duration};if(end>m.capacity)return Result.failure(IllegalArgumentException("Le tuplet dépasse la mesure"));commit();m.events.replaceAll{if(it.id in ids)it.copy(tuplet=TupletSpec(actual,normal))else it};return Result.success(Unit)}
 fun removeSelected():Boolean{val m=measures.getOrNull(selectedMeasure)?:return false;val i=m.events.indexOfFirst{it.id==selectedId};if(i<0)return false;commit();m.events.removeAt(i);selectedId=null;return true}
 fun undo():Boolean{if(undo.isEmpty())return false;redo.addLast(snap());restore(undo.removeLast());return true}
 fun redo():Boolean{if(redo.isEmpty())return false;undo.addLast(snap());restore(redo.removeLast());return true}
 private fun restore(s:List<RationalMeasure>){measures.clear();measures.addAll(s.map{it.copy(events=it.events.toMutableList())})}
 fun addMeasure(after:Int){commit();val i=(after+1).coerceIn(0,measures.size);measures.add(i,RationalMeasure(0));renumber()}
 fun deleteMeasure(index:Int):Boolean{if(measures.size<=1)return false;commit();measures.removeAt(index.coerceIn(0,measures.lastIndex));renumber();return true}
 private fun renumber(){measures.forEachIndexed{i,m->measures[i]=m.copy(number=i+1)}}
}
