package org.notamusic.app.playback

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import org.notamusic.app.domain.notation.CompositionEngine
import org.notamusic.app.domain.notation.RationalEvent
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

class PcmPlaybackController {
    @Volatile private var playing = false
    private var track: AudioTrack? = null

    fun play(engine: CompositionEngine, bpm: Int = 120) {
        stop(); playing = true
        thread(name = "notamusic-playback") {
            val rate = 44100
            val min = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            val audio = AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(min, 4096), AudioTrack.MODE_STREAM)
            track = audio; audio.play()
            val quarterMs = 60000.0 / bpm.coerceIn(20, 300)
            try {
                engine.measures.forEach { measure ->
                    if (!playing) return@thread
                    val lengthMs = measure.capacity.toDouble() * 4.0 * quarterMs
                    renderMeasure(audio, rate, measure.events, lengthMs)
                }
            } finally {
                runCatching { audio.stop() }; audio.release(); track = null; playing = false
            }
        }
    }

    private fun renderMeasure(audio: AudioTrack, rate: Int, events: List<RationalEvent>, lengthMs: Double) {
        val total = max(1, (rate * lengthMs / 1000.0).toInt())
        val chunk = ShortArray(1024)
        var base = 0
        while (base < total && playing) {
            val count = minOf(chunk.size, total - base)
            for (i in 0 until count) {
                val frame = base + i
                var sample = 0.0
                events.filterNot { it.rest || it.pitch == null }.forEach { e ->
                    val start = (e.onset.toDouble() * 4.0 * rate).toInt()
                    val end = start + (e.duration.toDouble() * 4.0 * rate).toInt()
                    if (frame in start until end) {
                        val phase = frame - start
                        val frequency = 440.0 * 2.0.pow((e.pitch!! - 69) / 12.0)
                        val attack = minOf(1.0, phase / (rate * 0.012))
                        val remaining = end - frame
                        val release = minOf(1.0, remaining / (rate * 0.025))
                        val velocity = when (e.dynamic) { "ppp" -> .10; "pp" -> .14; "p" -> .18; "mp" -> .21; "mf" -> .25; "f" -> .29; "ff" -> .33; "fff" -> .37; else -> .24 }
                        sample += sin(2.0 * PI * frequency * phase / rate) * velocity * attack * release
                    }
                }
                chunk[i] = (sample.coerceIn(-0.92, 0.92) * Short.MAX_VALUE).toInt().toShort()
            }
            audio.write(chunk, 0, count); base += count
        }
    }

    fun stop() { playing = false; runCatching { track?.pause(); track?.flush() } }
    fun isPlaying() = playing
}
