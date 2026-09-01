package org.notamusic.app.midi

import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.music.MidiRenderer
import java.io.ByteArrayOutputStream
import java.io.OutputStream

class PlaceholderMidiRenderer : MidiRenderer {
 override fun render(score: Score, output: OutputStream) {
  val tracks=score.parts.map{partTrack(it,score.tempo.bpm.coerceIn(20,300))}
  val h=ByteArrayOutputStream();h.write("MThd".toByteArray());i32(h,6);i16(h,if(tracks.size>1)1 else 0);i16(h,tracks.size.coerceAtLeast(1));i16(h,480);output.write(h.toByteArray());if(tracks.isEmpty())output.write(track(emptyList())) else tracks.forEach{output.write(it)}
 }
 private fun partTrack(p:Part,bpm:Int):ByteArray{val e=mutableListOf<E>();val us=60000000/bpm;e+=E(0,0xFF,0x51,byteArrayOf((us shr 16).toByte(),(us shr 8).toByte(),us.toByte()));e+=E(0,0xC0 or (p.staves.firstOrNull()?.instrument?.midiProgram?.coerceIn(0,127)?:0),null,byteArrayOf());p.staves.filterNot{it.mute}.forEach{s->s.measures.forEach{m->m.elements.filterIsInstance<Note>().forEach{n->val st=n.onset.toLong()*30;val len=ticks(n.duration,n.dots);val pc=(n.pitch+(n.octave-4)*12+alt(n.accidental)).coerceIn(0,127);e+=E(st,0x90,pc,byteArrayOf(vel(n.dynamic).toByte()));e+=E(st+len,0x80,pc,byteArrayOf(0))}}}};return track(e)}
 private fun ticks(d:Duration,dots:Int):Long{val b=when(d){Duration.WHOLE->1920L;Duration.HALF->960;Duration.QUARTER->480;Duration.EIGHTH->240;Duration.SIXTEENTH->120;Duration.THIRTY_SECOND->60};return when(dots){1->b+b/2;2->b+b/2+b/4;else->b}}
 private fun alt(a:Accidental)=when(a){Accidental.SHARP->1;Accidental.FLAT->-1;Accidental.NATURAL->0;Accidental.DOUBLE_SHARP->2;Accidental.DOUBLE_FLAT->-2;else->0}
 private fun vel(d:Dynamic?)=when(d?.value?.lowercase()){"ppp"->32;"pp"->40;"p"->48;"mp"->58;"mf"->70;"f"->82;"ff"->96;"fff"->112;else->70}
 private data class E(val tick:Long,val status:Int,val d1:Int?,val d2:ByteArray)
 private fun track(es:List<E>):ByteArray{val b=ByteArrayOutputStream();var last=0L;es.sortedWith(compareBy<E>{it.tick}.thenBy{if((it.status and 0xF0)==0x80)0 else 1}).forEach{x->var delta=x.tick-last;if(delta<0)delta=0;var v=delta;var buf=(v and 127).toInt();while({v=v shr 7;v>0}()){buf=buf shl 8;buf=buf or ((v and 127).toInt() or 128)};while(true){b.write(buf and 255);if((buf and 128)==0)break;buf=buf shr 8};b.write(x.status);if(x.status==0xFF){b.write(x.d1?:0);b.write(x.d2.size);b.write(x.d2)}else{b.write(x.d1?:0);if((x.status and 0xF0)!=0xC0)b.write(x.d2.firstOrNull()?.toInt()?:0)};last=x.tick};b.write(0);b.write(0xFF);b.write(0x2F);b.write(0);val o=ByteArrayOutputStream();o.write("MTrk".toByteArray());i32(o,b.size());o.write(b.toByteArray());return o.toByteArray()}
 private fun i16(o:ByteArrayOutputStream,v:Int){o.write(v shr 8);o.write(v)};private fun i32(o:ByteArrayOutputStream,v:Int){o.write(v shr 24);o.write(v shr 16);o.write(v shr 8);o.write(v)}
}