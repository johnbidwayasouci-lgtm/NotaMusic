package org.notamusic.core.notation

import java.io.File
import java.util.Base64

interface ScoreRepository { fun save(score:Score):Result<Unit>; fun load(id:String):Result<Score>; fun delete(id:String):Result<Unit>; fun list():List<String>; fun recover():Result<Score?> }

class FileScoreRepository(private val root:File, private val codec:ScoreCodec=ScoreCodec()) : ScoreRepository {
 init { root.mkdirs() }
 override fun save(score:Score):Result<Unit> = runCatching { val target=File(root,"${score.id}.nms"); val tmp=File(root,".${score.id}.tmp"); tmp.writeText(codec.encode(score)); check(tmp.renameTo(target) || (target.delete() && tmp.renameTo(target))) }
 override fun load(id:String):Result<Score> = runCatching { codec.decode(File(root,"$id.nms").readText()) }
 override fun delete(id:String):Result<Unit> = runCatching { File(root,"$id.nms").delete(); File(root,".$id.tmp").delete() }
 override fun list():List<String> = root.listFiles()?.filter{it.extension=="nms"}?.map{it.nameWithoutExtension}?.sorted()?:emptyList()
 override fun recover():Result<Score?> = runCatching { root.listFiles()?.firstOrNull{it.name.endsWith(".tmp")}?.let{codec.decode(it.readText())} }
}

class ScoreCodec {
 fun encode(s:Score)=PersistenceFormat.encode(s)
 fun decode(text:String):Score {
  val h=text.lineSequence().firstOrNull()?.split('|') ?: error("invalid score header")
  require(h.size>=3 && h[0]=="notamusic-score"); require(h[1].toIntOrNull()==PersistenceFormat.SCHEMA_VERSION){"unsupported schema"}
  val score=Score(id=h[2]); val lines=text.lineSequence().drop(1).toList(); var currentPart:Part?=null; var currentStaff:Staff?=null; var currentMeasure:Measure?=null
  fun u(s:String)=String(Base64.getDecoder().decode(s),Charsets.UTF_8)
  lines.forEach{l->val p=l.split('|');when(p[0]){
   "meta"->{score.metadata=Metadata(u(p[1]),u(p[2]),u(p[3]),u(p[4]),u(p[5]),u(p[6]))}
   "tempo"->{score.tempo=Tempo(p[1].toInt());score.keySignature=KeySignature(p[3].toInt(),p[4].toBoolean())}
   "part"->{val part=Part(u(p[2]),Instrument(u(p[3]),u(p[4]),p[5].toInt()),mutableListOf());score.parts.add(part);currentPart=part}
   "staff"->{val st=Staff(u(p[3]),Clef.valueOf(p[4]),mutableListOf(),p[5].toBoolean(),p[6].toInt());currentPart!!.staves.add(st);currentStaff=st}
   "measure"->{val m=Measure(p[3].toInt(),TimeSignature(p[4].toInt(),p[5].toInt()),mutableMapOf(),Barline(p[6].toBoolean(),p[7].toBoolean()));currentStaff!!.measures.add(m);currentMeasure=m}
   "note"->{val v=p[4].toInt();val acc=p[11].takeIf{it!="null"}?.let{Accidental.valueOf(it)};val orn=p[15].takeIf{it.isNotEmpty()}?.split(',')?.filter{it.isNotEmpty()}?.map{Ornament.valueOf(it)}?.toSet()?:emptySet();val tup=if(p[16].toInt()>0)TupletSpec(p[16].toInt(),p[17].toInt())else null;val d=Duration(FractionParser.parse(p[7]),p[8].toInt());currentMeasure!!.voices.getOrPut(v){mutableListOf()}.add(Note(p[5],FractionParser.parse(p[6]),d,v,Pitch(PitchClass.valueOf(p[9]),p[10].toInt(),acc),p[12].toBoolean(),p[13].toBoolean(),orn,p[14].takeIf{it!="null"}?.let{Dynamic.valueOf(it)},tup))}
   "rest"->{val v=p[4].toInt();currentMeasure!!.voices.getOrPut(v){mutableListOf()}.add(Rest(p[5],FractionParser.parse(p[6]),Duration(FractionParser.parse(p[7]),p[8].toInt()),v))}
  }};score
 }
}
object FractionParser { fun parse(s:String):Fraction { val p=s.split('/');return if(p.size==1)Fraction.of(p[0].toLong(),1)else Fraction.of(p[0].toLong(),p[1].toLong()) } }
