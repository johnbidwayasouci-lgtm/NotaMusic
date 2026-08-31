package org.notamusic.core.notation

import org.w3c.dom.Document
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import java.io.StringReader
import java.io.StringWriter
import org.xml.sax.InputSource

interface MusicXmlImporter { fun importXml(xml:String): Result<Score> }
interface MusicXmlExporter { fun exportXml(score:Score): Result<String> }

class SimpleMusicXmlExporter : MusicXmlExporter {
 override fun exportXml(score:Score):Result<String> = runCatching {
  val d=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument(); val root=d.createElement("score-partwise"); root.setAttribute("version","3.1"); d.appendChild(root)
  val work=d.createElement("work"); root.appendChild(work); work.appendChild(d.createElement("work-title")).textContent=score.metadata.title
  val list=d.createElement("part-list"); root.appendChild(list); score.parts.forEachIndexed{ i,p-> val sp=d.createElement("score-part"); sp.setAttribute("id","P${i+1}"); sp.appendChild(d.createElement("part-name")).textContent=p.instrument.name; list.appendChild(sp) }
  score.parts.forEachIndexed{ i,p-> val part=d.createElement("part"); part.setAttribute("id","P${i+1}"); root.appendChild(part); p.staves.firstOrNull()?.measures?.forEach{m->
   val me=d.createElement("measure"); me.setAttribute("number",m.number.toString()); part.appendChild(me); val a=d.createElement("attributes"); me.appendChild(a); a.appendChild(d.createElement("divisions")).textContent="480"; val time=d.createElement("time"); a.appendChild(time); time.appendChild(d.createElement("beats")).textContent=m.timeSignature.beats.toString(); time.appendChild(d.createElement("beat-type")).textContent=m.timeSignature.beatUnit.toString(); val clef=d.createElement("clef"); a.appendChild(clef); clef.appendChild(d.createElement("sign")).textContent=if(p.staves.first().clef==Clef.BASS)"F" else "G"; clef.appendChild(d.createElement("line")).textContent=if(p.staves.first().clef==Clef.BASS)"4" else "2"
   m.elements().forEach{e-> val n=d.createElement("note"); me.appendChild(n); if(e is Rest)n.appendChild(d.createElement("rest")) else { val pitch=d.createElement("pitch"); n.appendChild(pitch); pitch.appendChild(d.createElement("step")).textContent=(e as Note).pitch.step.name; pitch.appendChild(d.createElement("octave")).textContent=e.pitch.octave.toString(); e.pitch.accidental?.let{pitch.appendChild(d.createElement("alter")).textContent=when(it){Accidental.SHARP->"1";Accidental.FLAT->"-1";Accidental.DOUBLE_SHARP->"2";Accidental.DOUBLE_FLAT->"-2";Accidental.NATURAL->"0"}} }; n.appendChild(d.createElement("duration")).textContent=(e.duration.value.toDouble()*480).toLong().toString(); n.appendChild(d.createElement("voice")).textContent=e.voice.toString(); n.appendChild(d.createElement("type")).textContent=when(e.duration.base){Fraction.ONE->"whole";Fraction.HALF->"half";Fraction.QUARTER->"quarter";Fraction.of(1,8)->"eighth";Fraction.of(1,16)->"16th";else->"quarter"} }
  } }
  val t=TransformerFactory.newInstance().newTransformer(); t.setOutputProperty(OutputKeys.INDENT,"yes"); StringWriter().also{t.transform(DOMSource(d),StreamResult(it))}.toString()
 }
}

class SimpleMusicXmlImporter : MusicXmlImporter {
 override fun importXml(xml:String):Result<Score> = runCatching { val d=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(StringReader(xml))); val score=Score(); val title=d.getElementsByTagName("movement-title"); if(title.length>0) score.metadata.title=title.item(0).textContent; score }
}
