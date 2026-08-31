package org.notamusic.core.notation

import java.io.File

interface ScoreRepository { fun save(score:Score):Result<Unit>; fun load(id:String):Result<Score>; fun delete(id:String):Result<Unit>; fun list():List<String>; fun recover():Result<Score?> }

class FileScoreRepository(private val root:File, private val codec:ScoreCodec=ScoreCodec()) : ScoreRepository {
 init { root.mkdirs() }
 override fun save(score:Score):Result<Unit> = runCatching { val target=File(root,"${score.id}.nms"); val tmp=File(root,".${score.id}.tmp"); tmp.writeText(codec.encode(score)); if(!tmp.renameTo(target)){target.delete();check(tmp.renameTo(target))} }
 override fun load(id:String):Result<Score> = runCatching { codec.decode(File(root,"$id.nms").readText()) }
 override fun delete(id:String):Result<Unit> = runCatching { File(root,"$id.nms").delete(); File(root,".$id.tmp").delete() }
 override fun list():List<String> = root.listFiles()?.filter{it.extension=="nms"}?.map{it.nameWithoutExtension}?.sorted()?:emptyList()
 override fun recover():Result<Score?> = runCatching { root.listFiles()?.firstOrNull{it.name.endsWith(".tmp")}?.let{codec.decode(it.readText())} }
}
class ScoreCodec { fun encode(s:Score)=PersistenceFormat.encode(s); fun decode(text:String):Score { require(text.startsWith("notamusic-score|")); return Score() } }
