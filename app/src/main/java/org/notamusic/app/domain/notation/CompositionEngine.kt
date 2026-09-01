package org.notamusic.app.domain.notation

import org.notamusic.app.domain.model.Accidental
import java.util.UUID

enum class EditorTool { SELECT, ERASER, WHOLE, HALF, QUARTER, EIGHTH, SIXTEENTH, THIRTY_SECOND, REST_WHOLE, REST_HALF, REST_QUARTER, REST_EIGHTH, REST_SIXTEENTH, REST_THIRTY_SECOND, DOT, DOUBLE_DOT, TIE, SLUR, TUPLET, TRILL, MORDENT, INVERTED_MORDENT, TURN, INVERTED_TURN, STACCATO, SHORT_STACCATO, ACCENT, FERMATA, TREMOLO, GRACE, PPP, PP, P, MP, MF, F, FF, FFF, CRESCENDO, DIMINUENDO }

data class RationalEvent(val id:String,val onset:Fraction,val duration:Fraction,val pitch:Int?,val octave:Int=4,val accidental:Accidental=Accidental.NONE,val voice:Int=1,val rest:Boolean=false,val dots:Int=0,val ornament:String?=null,val dynamic:String?=null,val tieStart:Boolean=false,val tieEnd:Boolean=false,val tuplet:TupletSpec?=null,val slurStart:Boolean=false,val slurEnd:Boolean=false)
data class RationalMeasure(val number:Int,val beats:Int=4,val beatUnit:Int=4,val events:MutableList<RationalEvent> = mutableListOf()){ val capacity get()=Fraction.of(beats.toLong(),beatUnit.toLong()) }
data class TupletSpec(val actual:Int,val normal:Int)

class CompositionEngine(empty:Boolean=false){
 val measures=mutableListOf<RationalMeasure>(); var voice=1; var selectedId:String?=null; var selectedMeasure=0
 private val undo=ArrayDeque<List<RationalMeasure>>(); private val redo=ArrayDeque<List<RationalMeasure>>()
 init{if(!empty)measures+=RationalMeasure(1)}
 private fun snapshot()=measures.map{it.copy(events=it.events.map{e->e.copy()}.toMutableList())}; private fun commit(){undo.addLast(snapshot());redo.clear()}
 private fun restore(s:List<RationalMeasure>){measures.clear();measures.addAll(s.map{it.copy(events=it.events.map{e->e.copy()}.toMutableList())})}
 fun nextFreeOnset(measureIndex:Int,requestedVoice:Int=voice):Fraction{val m=measures.getOrNull(measureIndex)?:return Fraction.ZERO;var p=Fraction.ZERO;for(e in m.events.filter{it.voice==requestedVoice}.sortedBy{it.onset}){if(e.onset>p)return p;val end=e.onset+e.duration;if(end>p)p=end};return p}
 fun add(measureIndex:Int,onset:Fraction,duration:MusicalDuration,pitch:Int=60,octave:Int=4,rest:Boolean=false,accidental:Accidental=Accidental.NONE):Result<RationalEvent>{val m=measures.getOrNull(measureIndex)?:return Result.failure(IllegalArgumentException("Mesure introuvable"));val d=duration.total();val end=onset+d;if(onset<Fraction.ZERO||end>m.capacity)return Result.failure(IllegalArgumentException("La note dépasse la capacité de la mesure"));if(m.events.any{it.voice==voice&&onset<it.onset+it.duration&&end>it.onset})return Result.failure(IllegalArgumentException("La voix $voice contient déjà un élément à cette position"));commit();val e=RationalEvent(UUID.randomUUID().toString(),onset,d,if(rest)null else pitch.coerceIn(0,127),octave.coerceIn(0,9),accidental,voice,rest,duration.dots);m.events.add(e);m.events.sortWith(compareBy<RationalEvent>{it.onset}.thenBy{it.voice});selectedId=e.id;selectedMeasure=measureIndex;return Result.success(e)}
 private fun update(measure:Int,id:String,fn:(RationalEvent)->RationalEvent):Boolean{val m=measures.getOrNull(measure)?:return false;val i=m.events.indexOfFirst{it.id==id};if(i<0)return false;commit();m.events[i]=fn(m.events[i]);return true}
 fun select(measure:Int,id:String?):Boolean{if(measure !in measures.indices)return false;selectedMeasure=measure;selectedId=id;return true}
 fun updateDots(id:String,measure:Int,dots:Int)=update(measure,id){it.copy(dots=dots.coerceIn(0,2))}
 fun updatePitch(id:String,measure:Int,pitch:Int,accidental:Accidental=Accidental.NONE)=update(measure,id){it.copy(pitch=pitch.coerceIn(0,127),accidental=accidental)}
 fun setVoice(id:String,measure:Int,newVoice:Int):Result<Unit>{if(newVoice !in 1..2)return Result.failure(IllegalArgumentException("Voix invalide"));val e=measures.getOrNull(measure)?.events?.firstOrNull{it.id==id}?:return Result.failure(IllegalArgumentException("Élément introuvable"));if(measures[measure].events.any{it.id!=id&&it.voice==newVoice&&e.onset<it.onset+it.duration&&e.onset+e.duration>it.onset})return Result.failure(IllegalArgumentException("Chevauchement dans la voix $newVoice"));update(measure,id){it.copy(voice=newVoice)};return Result.success(Unit)}
 fun toggleTie(id:String,measure:Int):Boolean{val m=measures.getOrNull(measure)?:return false;val e=m.events.firstOrNull{it.id==id}?:return false;if(e.rest)return false;val target=m.events.firstOrNull{it.id!=id&&it.voice==e.voice&&it.onset==e.onset+e.duration&&it.pitch==e.pitch&&!it.rest}?:return false;commit();m.events[m.events.indexOfFirst{it.id==id}]=e.copy(tieStart=!e.tieStart);m.events[m.events.indexOfFirst{it.id==target.id}]=target.copy(tieEnd=!target.tieEnd);return true}
 fun toggleSlur(id:String,measure:Int)=update(measure,id){it.copy(slurStart=!it.slurStart)}
 fun setOrnament(id:String,measure:Int,name:String)=update(measure,id){if(it.rest)it else it.copy(ornament=name)}
 fun setDynamic(id:String,measure:Int,name:String)=update(measure,id){if(it.rest)it else it.copy(dynamic=name)}
 fun addTuplet(measure:Int,ids:List<String>,actual:Int=3,normal:Int=2):Result<Unit>{if(actual<=0||normal<=0||ids.isEmpty())return Result.failure(IllegalArgumentException("Tuplet invalide"));val m=measures.getOrNull(measure)?:return Result.failure(IllegalArgumentException("Mesure introuvable"));val selected=m.events.filter{it.id in ids};if(selected.size!=ids.size||selected.any{it.rest})return Result.failure(IllegalArgumentException("Le tuplet doit contenir des notes"));commit();m.events.replaceAll{if(it.id in ids)it.copy(tuplet=TupletSpec(actual,normal))else it};return Result.success(Unit)}
 fun removeSelected():Boolean{val m=measures.getOrNull(selectedMeasure)?:return false;val i=m.events.indexOfFirst{it.id==selectedId};if(i<0)return false;commit();m.events.removeAt(i);selectedId=null;return true}
 fun undo():Boolean{if(undo.isEmpty())return false;redo.addLast(snapshot());restore(undo.removeLast());return true};fun redo():Boolean{if(redo.isEmpty())return false;undo.addLast(snapshot());restore(redo.removeLast());return true}
 fun addMeasure(after:Int,beats:Int=4,beatUnit:Int=4){commit();val i=(after+1).coerceIn(0,measures.size);measures.add(i,RationalMeasure(i+1,beats,beatUnit));renumber()}
 fun deleteMeasure(index:Int):Boolean{if(measures.size<=1||index !in measures.indices)return false;commit();measures.removeAt(index);renumber();return true}
 fun changeMeter(index:Int,beats:Int,beatUnit:Int):Boolean{if(index !in measures.indices||beats<=0||beatUnit<=0)return false;val cap=Fraction.of(beats.toLong(),beatUnit.toLong());val m=measures[index];if(m.events.any{it.onset+it.duration>cap})return false;commit();measures[index]=m.copy(beats=beats,beatUnit=beatUnit);return true}
 private fun renumber(){measures.forEachIndexed{i,m->measures[i]=m.copy(number=i+1)}}
}
