package com.notationstudio.domain.music

class ScoreHistory<T>(initial: T) {
    private val undo = ArrayDeque<T>(); private val redo = ArrayDeque<T>(); private var current = initial
    fun current(): T = current
    fun transact(next: T) { undo.addLast(current); current=next; redo.clear() }
    fun undo(): T? { if(undo.isEmpty()) return null; redo.addLast(current); current=undo.removeLast(); return current }
    fun redo(): T? { if(redo.isEmpty()) return null; undo.addLast(current); current=redo.removeLast(); return current }
    fun canUndo()=undo.isNotEmpty(); fun canRedo()=redo.isNotEmpty()
}
