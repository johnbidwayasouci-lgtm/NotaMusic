package org.notamusic.app.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.ScoreValidator

class ScoreValidatorTest { @Test fun emptyScoreIsRejected() { assertTrue(ScoreValidator().validate(Score()).isNotEmpty()) } }
