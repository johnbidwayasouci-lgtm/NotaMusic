package org.notamusic.core.notation

import org.w3c.dom.Element
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.xml.sax.InputSource

class MusicXmlCodec : MusicXmlExporter, MusicXmlImporter {
 override fun exportXml(score: Score): Result<String> = runCatching {
  val f=DocumentBuilderFactory.newInstance(); val d=f.newDocumentBuilder().newDocument(); val root=d.createElement("score-partwise"); root.setAttribute("version","3.1"); d.appendChild(root)
  root.appendChild(d.createElement("work")).appendChild(d.createElement("work-title")).textContent=score.metadata.title
  val id=d.createElement("identification"); root.appendChild(id); d.createElement("creator").also{it.setAttribute("type","composer");it.textContent=score.metadata.composer;id.appendChild(it)}
  val pl=d.createElement("part-list"); root.appendChild(pl)
  score.parts.forEachIndexed{i,p-> d.createElement("score-part").also{s->s.setAttribute("id","P${i+1}");d.createElement("part-name").also{n->n.textContent=p.instrument.name;s.appendChild(n)};pl.appendChild(s)}}
  score.parts.forEachIndexed{i,p-> val part=d.createElement("part");part.setAttribute("id","P${i+1}");root.appendChild(part);p.staves.firstOrNull()?.measures?.forEach{m->
   val me=d.createElement("measure");me.setAttribute("number",m.number.toString());part.appendChild(me)
   val at=d.createElement("attributes");me.appendChild(at);d.createElement("divisions").also{x->x.textContent="480";at.appendChild(x)}
   d.createElement("key").also{x->d.createElement("fifths").also{z->z.textContent=score.keySignature.fifths.toString();x.appendChild(z)};at.appendChild(x)}
   d.createElement("time").also{x->d.createElement("beats").also{z->z.textContent=m.timeSignature.beats.toString();x.appendChild(z)};d.createElement("beat-type").also{z->z.textContent=m.timeSignature.beatUnit.toString();x.appendChild(z)};at.appendChild(x)}
   d.createElement("clef").also{x->d.createElement("sign").also{z->z.textContent=when(p.staves.first().clef){Clef.BASS->"F";Clef.ALTO->"C";else->"G"};x.appendChild(z)};d.createElement("line").also{z->z.textContent=when(p.staves.first().clef){Clef.BASS->"4";Clef.ALTO->"3";else->"2"};x.appendChild(z)};at.appendChild(x)}
   m.elements().forEach{e-> val n=d.createElement("note");me.appendChild(n);if(e is Rest)n.appendChild(d.createElement("rest")) else {val pch=(e as Note).pitch;d.createElement("pitch").also{x->d.createElement("step").also{z->z.textContent=pch.step.name;x.appendChild(z)};pch.accidental?.let{a->d.createElement("alter").also{z->z.textContent=when(a){Accidental.DOUBLE_FLAT->"-2";Accidental.FLAT->"-1";Accidental.NATURAL->"0";Accidental.SHARP->"1";Accidental.DOUBLE_SHARP->"2"};x.appendChild(z)}};d.createElement("octave").also{z->z.textContent=pch.octave.toString();x.appendChild(z)};n.appendChild(x)};d.createElement("duration").also{x->x.textContent=(e.duration.value*Fraction.of(480,1)).numerator.toString();n.appendChild(x)};d.createElement("voice").also{x->x.textContent=e.voice.toString();n.appendChild(x)};if(e.duration.dots>0)repeat(e.duration.dots){n.appendChild(d.createElement("dot"))};if(e is Note){e.tieStart.let{if(it){d.createElement("tie").also{x->x.setAttribute("type","start");n.appendChild(x)}}};e.tieStop.let{if(it){d.createElement("tie").also{x->x.setAttribute("type","stop");n.appendChild(x)}}}} }
  }}
  TransformerFactory.newInstance().newTransformer().also{it.setOutputProperty(OutputKeys.INDENT,"yes")}.let{t->StringWriter().also{w->t.transform(DOMSource(d),StreamResult(w));w.toString()}}
 }
 override fun importXml(xml:String):Result<Score> = runCatching {
  val d=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml))); val score=Score(); d.getElementsByTagName("movement-title").let{if(it.length>0)score.metadata.title=it.item(0).textContent}; d.getElementsByTagName("work-title").let{if(it.length>0)score.metadata.title=it.item(0).textContent}
  val parts=d.getElementsByTagName("part"); for(i in 0 until parts.length){val pe=parts.item(i) as Element;val id=pe.getAttribute("id").ifBlank{"P${i+1}"};val name=when{d.getElementsByTagName("score-part").length>i->(d.getElementsByTagName("score-part").item(i) as Element).getElementsByTagName("part-name").item(0)?.textContent?:"Part ${i+1}";else->"Part ${i+1}"};val staff=Staff(name);val ms=pe.getElementsByTagName("measure");for(j in 0 until ms.length){val me=ms.item(j) as Element;val num=me.getAttribute("number").toIntOrNull()?:j+1;val measure=Measure(num,TimeSignature(4,4));val notes=me.getElementsByTagName("note");var cursor=Fraction.ZERO;for(k in 0 until notes.length){val ne=notes.item(k) as Element;val div=ne.getElementsByTagName("duration").item(0)?.textContent?.toLongOrNull()?:480;val dur=Duration(Fraction.of(div,480));val voice=ne.getElementsByTagName("voice").item(0)?.textContent?.toIntOrNull()?:1;val rest=ne.getElementsByTagName("rest").length>0;val el:MusicElement=if(rest)Rest(start=cursor,duration=dur,voice=voice)else{val p=ne.getElementsByTagName("pitch").item(0) as Element;val step=PitchClass.valueOf(p.getElementsByTagName("step").item(0).textContent);val oct=p.getElementsByTagName("octave").item(0).textContent.toInt();val alter=p.getElementsByTagName("alter").item(0)?.textContent?.toIntOrNull();val acc=when(alter){-2->Accidental.DOUBLE_FLAT;-1->Accidental.FLAT;0->Accidental.NATURAL;1->Accidental.SHARP;2->Accidental.DOUBLE_SHARP;else->null};Note(start=cursor,duration=dur,voice=voice,pitch=Pitch(step,oct,acc))};measure.voices.getOrPut(voice){mutableListOf()}.add(el);cursor+=dur.value};staff.measures.add(measure)};score.parts.add(Part(id,Instrument(id,name),mutableListOf(staff)))};score
 }
}
