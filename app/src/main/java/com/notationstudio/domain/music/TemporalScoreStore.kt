package com.notationstudio.domain.music

class TemporalScoreStore(initial: List<TemporalMeasure>) {
    private val history = ScoreHistory(initial.map { it.copy(voices = it.voices.mapValues { (_, v) -> v.toList() }) })
    fun measures(): List<TemporalMeasure> = history.current()
    fun insert(measureNo: Int, voice: Int, element: TemporalElement): EditResult = mutate { ms ->
        val i=ms.indexOfFirst { it.number==measureNo }; if(i<0) return@mutate EditResult.Failure("measure not found")
        val m=ms[i]; val current=m.voice(voice); val end=element.position+element.duration
        if(end>m.capacity) return@mutate EditResult.Failure("element exceeds measure capacity")
        if(current.any { element.position < it.position+it.duration && end > it.position }) return@mutate EditResult.Failure("element overlaps existing content")
        ms.toMutableList().also { it[i]=m.copy(voices=m.voices + (voice to (current+element).sortedBy { e->e.position })) }
    }
    fun delete(measureNo:Int, voice:Int, id:String): EditResult = mutate { ms ->
        val i=ms.indexOfFirst { it.number==measureNo }; if(i<0) return@mutate EditResult.Failure("measure not found")
        val m=ms[i]; if(m.voice(voice).none { it.id==id }) return@mutate EditResult.Failure("element not found")
        ms.toMutableList().also { it[i]=m.copy(voices=m.voices + (voice to m.voice(voice).filterNot { e->e.id==id })) }
    }
    fun modify(measureNo:Int, voice:Int, replacement:TemporalElement):EditResult = delete(measureNo,voice,replacement.id).let { if(it is EditResult.Failure) it else insert(measureNo,voice,replacement) }
    fun undo():Boolean = history.undo()!=null
    fun redo():Boolean = history.redo()!=null
    private fun mutate(block:(List<TemporalMeasure>)->Any): EditResult {
        val result=block(history.current())
        if(result is EditResult.Failure) return result
        @Suppress("UNCHECKED_CAST") val next=result as List<TemporalMeasure>; history.transact(next); return EditResult.Success(Unit)
    }
}
