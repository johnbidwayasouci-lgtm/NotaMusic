package org.notamusic.core.notation

import java.io.ByteArrayOutputStream

class MidiRenderer {
 fun render(score:Score, ppq:Int=480):ByteArray { val tracks=score.parts.map{partTrack(it,score.tempo.bpm,ppq)}; val out=ByteArrayOutputStream(); out.write("MThd".toByteArray()); writeInt(out,6); writeShort(out,1); writeShort(out,tracks.size); writeShort(out,ppq); tracks.forEach{out.write("MTrk".toByteArray()); writeInt(out,it.size); out.write(it)}; return out.toByteArray() }
 private fun partTrack(p:Part,bpm:Int,ppq:Int):ByteArray { val b=ByteArrayOutputStream(); event(b,0,0xFF,0x51,byteArrayOf((60000000/bpm shr 16).toByte(),(60000000/bpm shr 8).toByte(),(60000000/bpm).toByte())); event(b,0,0xC0 or (p.instrument.midiProgram and 15),0); var last=0L; p.staves.filter{!it.mute}.flatMap{it.measures.flatMap{m->m.elements().filterIsInstance<Note>()}}.sortedBy{it.start}.forEach{n-> val start=n.start.toTicks(ppq); val delta=(start-last).coerceAtLeast(0); event(b,delta,0x90 or 0,(midiPitch(n.pitch)).coerceIn(0,127),velocity(n.dynamic)); val len=n.duration.value.toTicks(ppq); event(b,len,0x80 or 0,midiPitch(n.pitch),0); last=start+len}; event(b,0,0xFF,0x2F,0); return b.toByteArray() }
 private fun midiPitch(p:Pitch)=12*(p.octave+1)+p.step.ordinal + when(p.accidental){Accidental.SHARP->1;Accidental.DOUBLE_SHARP->2;Accidental.FLAT->-1;Accidental.DOUBLE_FLAT->-2;else->0}
 private fun velocity(d:Dynamic?)=when(d){Dynamic.PPP->32;Dynamic.PP->40;Dynamic.P->48;Dynamic.MP->58;Dynamic.MF->70;Dynamic.F->82;Dynamic.FF->96;Dynamic.FFF->112;else->70}
 private fun event(o:ByteArrayOutputStream,delta:Long,status:Int,vararg data:Int){writeVar(o,delta);o.write(status);data.forEach{o.write(it)} }
 private fun event(o:ByteArrayOutputStream,delta:Long,status:Int,type:Int,data:ByteArray){writeVar(o,delta);o.write(status);o.write(type);o.write(data.size);o.write(data)}
 private fun writeVar(o:ByteArrayOutputStream,v0:Long){var v=v0; var buffer=(v and 0x7f).toInt(); while({v=v shr 7;v>0}()){buffer=buffer shl 8;buffer=buffer or ((v and 0x7f).toInt() or 0x80)}; while(true){o.write(buffer and 0xff);if((buffer and 0x80)==0)break;buffer=buffer shr 8} }
 private fun writeShort(o:ByteArrayOutputStream,v:Int){o.write(v shr 8);o.write(v)}; private fun writeInt(o:ByteArrayOutputStream,v:Int){o.write(v shr 24);o.write(v shr 16);o.write(v shr 8);o.write(v)}
}
