package com.notationstudio.domain.music

import org.junit.Assert.*
import org.junit.Test

class TemporalScoreStoreTest {
    private fun store()=TemporalScoreStore(List(2){TemporalMeasure(it+1,TimeSignature(4,4))})
    private fun note(pos:Fraction,d:Fraction)=TemporalElement(kind=ElementKind.NOTE,position=pos,duration=d,pitch=Pitch(Step.C,4))
    @Test fun insert_delete_undo_redo_are_transactional(){
        val s=store(); val n=note(Fraction.ZERO,Fraction.QUARTER)
        assertTrue(s.insert(1,1,n) is EditResult.Success); assertEquals(1,s.measures()[0].voice(1).size)
        assertTrue(s.delete(1,1,n.id) is EditResult.Success); assertTrue(s.measures()[0].voice(1).isEmpty())
        assertTrue(s.undo()); assertEquals(1,s.measures()[0].voice(1).size)
        assertTrue(s.undo()); assertTrue(s.measures()[0].voice(1).isEmpty())
        assertTrue(s.redo()); assertEquals(1,s.measures()[0].voice(1).size)
    }
    @Test fun independent_voices_can_share_time(){ val s=store(); assertTrue(s.insert(1,1,note(Fraction.ZERO,Fraction.WHOLE)) is EditResult.Success); assertTrue(s.insert(1,2,note(Fraction.ZERO,Fraction.WHOLE)) is EditResult.Success) }
}
