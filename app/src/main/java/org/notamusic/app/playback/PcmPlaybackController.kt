package org.notamusic.app.playback

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import org.notamusic.app.domain.notation.CompositionEngine
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

class PcmPlaybackController {
    @Volatile private var playing = false
    private var track: AudioTrack? = null

    fun play(engine: CompositionEngine, bpm: Int = 120) {
        stop()
        playing = true
        thread(name = "notamusic-playback") {
            val sampleRate = 44100
            val min = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val audio = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(min, 4096), AudioTrack.MODE_STREAM)
            track = audio
            audio.play()
            val quarterMs = (60000.0 / bpm.coerceIn(20, 300)).toLong()
            try {
                engine.measures.forEach { measure ->
                    measure.events.sortedBy { it.onset }.forEach { event ->
                        if (!playing) return@thread
                        val ms = (event.duration.toDouble() * quarterMs * 1000.0).toLong().coerceAtLeast(60)
                        if (event.rest || event.pitch == null) writeTone(audio, sampleRate, ms, 0.0)
                        else writeTone(audio, sampleRate, ms, 440.0 * Math.pow(2.0, (event.pitch - 69) / 12.0))
                    }
                }
            } finally {
                runCatching { audio.stop() }; audio.release(); track = null; playing = false
            }
        }
    }

    private fun writeTone(audio: AudioTrack, rate: Int, millis: Long, frequency: Double) {
        val count = (rate * millis / 1000L).toInt()
        val buffer = ShortArray(1024)
        var i = 0
        while (i < count && playing) {
            val n = minOf(buffer.size, count - i)
            for (j in 0 until n) buffer[j] = if (frequency == 0.0) 0 else (sin(2.0 * PI * frequency * (i + j) / rate) * 0.22 * Short.MAX_VALUE).toInt().toShort()
            audio.write(buffer, 0, n)
            i += n
        }
    }

    fun stop() { playing = false; runCatching { track?.pause(); track?.flush() } }
    fun isPlaying() = playing
}
