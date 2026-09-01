package org.notamusic.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.music.ScoreFactory
import org.notamusic.app.ui.create.CreateScoreDraft

class ScoreFactoryTest {
    @Test fun createsAllSelectedParts() {
        val instruments = listOf(Instrument("violin", "Violin", 40), Instrument("cello", "Cello", 42))
        val score = ScoreFactory().create(CreateScoreDraft(instruments = instruments))
        assertEquals(2, score.parts.size)
        assertEquals("Violin", score.parts[0].name)
        assertEquals("Cello", score.parts[1].name)
        assertTrue(score.parts.all { it.staves.size == 1 })
    }
    @Test fun pianoCreatesGrandStaff() {
        val score = ScoreFactory().create(CreateScoreDraft(instruments = listOf(Instrument("piano", "Piano", 0))))
        assertEquals(1, score.parts.size)
        assertEquals(2, score.parts.single().staves.size)
        assertEquals(Clef.TREBLE, score.parts.single().staves[0].clef)
        assertEquals(Clef.BASS, score.parts.single().staves[1].clef)
    }
}
