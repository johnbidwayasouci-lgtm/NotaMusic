package org.notamusic.core.notation

class ScoreEditor(private val score: Score) {
    private val undo = ArrayDeque<ScoreSnapshot>()
    private val redo = ArrayDeque<ScoreSnapshot>()
    var selection: Selection = Selection(); private set
    private fun snapshot() = ScoreSnapshot(score.deepCopy())
    private fun commit() { undo.addLast(snapshot()); redo.clear() }
    fun select(s: Selection) { selection=s }
    fun insert(part:Int,staff:Int,measure:Int,element:MusicElement): Result<Unit> {
        val m=score.parts.getOrNull(part)?.staves?.getOrNull(staff)?.measures?.getOrNull(measure) ?: return Result.failure(IllegalArgumentException("measure not found"))
        if(!m.canInsert(element.start,element.duration,element.voice)) return Result.failure(IllegalArgumentException("temporal capacity exceeded or overlap"))
        commit(); m.voices.getOrPut(element.voice){mutableListOf()}.add(element); m.voices[element.voice]?.sortBy{it.start}; return Result.success(Unit)
    }
    fun remove(part:Int,staff:Int,measure:Int,id:String):Boolean { val m=score.parts[part].staves[staff].measures[measure]; val v=m.voices.values.firstOrNull{it.any(e->e.id==id)} ?: return false; commit(); return v.removeIf{it.id==id} }
    fun undo():Boolean { if(undo.isEmpty()) return false; redo.addLast(snapshot()); restore(undo.removeLast().score); return true }
    fun redo():Boolean { if(redo.isEmpty()) return false; undo.addLast(snapshot()); restore(redo.removeLast().score); return true }
    private fun restore(s:Score) { score.metadata=s.metadata; score.tempo=s.tempo; score.keySignature=s.keySignature; score.parts.clear(); score.parts.addAll(s.parts) }
}
data class ScoreSnapshot(val score:Score)
private fun Score.deepCopy():Score = Score(id,metadata.copy(),tempo.copy(),parts.map{p->p.copy(staves=p.staves.map{s->s.copy(measures=s.measures.map{m->m.copy(voices=m.voices.mapValues{(_,v)->v.toMutableList()}.toMutableMap())}.toMutableList())}.toMutableList())}.toMutableList(),keySignature.copy())
