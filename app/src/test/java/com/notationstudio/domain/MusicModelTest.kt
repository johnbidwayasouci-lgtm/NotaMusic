package com.notationstudio.domain

import com.notationstudio.domain.model.*
import com.notationstudio.ui.state.ScoreCreationTransaction
import org.junit.Assert.*
import org.junit.Test

class MusicModelTest {
    @Test fun noteContainsMusicalPositionAndEditingState() {
        val note = Note(pitch=Pitch(Step.C,4), duration=Duration.QUARTER, dotted=true, voice=2, positionInMeasure=480, selected=true)
        assertEquals(4, note.pitch.octave); assertTrue(note.dotted); assertEquals(2,note.voice); assertTrue(note.selected)
    }
    @Test fun creationTransactionDoesNotCommitBeforeBegin() {
        val tx = ScoreCreationTransaction(); assertNull(tx.preview())
    }
    @Test fun transactionCommitsDraft() {
        val tx = ScoreCreationTransaction(); val score = Score(); tx.begin(score); assertEquals(score, tx.commit())
    }
}
