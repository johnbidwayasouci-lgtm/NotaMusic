package org.notamusic.app.playback

import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.PlaybackController

class NoOpPlaybackController : PlaybackController { override var isPlaying: Boolean = false; private set; override fun play(score: Score) { isPlaying = true }; override fun pause() { isPlaying = false }; override fun stop() { isPlaying = false }; override fun setMuted(staffId: String, muted: Boolean) {} }
