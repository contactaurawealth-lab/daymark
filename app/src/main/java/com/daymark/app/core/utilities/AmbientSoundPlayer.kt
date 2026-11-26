package com.daymark.app.core.utilities

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

object AmbientSoundPlayer {
    private var audioTrack: AudioTrack? = null
    private var playerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    var isPlaying: Boolean = false
        private set

    fun toggle(): Boolean {
        if (isPlaying) {
            stop()
        } else {
            start()
        }
        return isPlaying
    }

    fun start() {
        stop()
        isPlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        playerJob = scope.launch {
            val buffer = ShortArray(bufferSize)
            var lastOut = 0.0

            while (isActive) {
                for (i in buffer.indices) {
                    // Soft low-pass filtered brown noise (calm rainfall/ocean waves sensation)
                    val white = Random.nextDouble(-1.0, 1.0)
                    lastOut = (lastOut + (0.02 * white)) / 1.02
                    val scaled = (lastOut * 4000).toInt().coerceIn(-32768, 32767)
                    buffer[i] = scaled.toShort()
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun pause() {
        isPlaying = false
        audioTrack?.pause()
        playerJob?.cancel()
    }

    fun resume() {
        if (audioTrack != null) {
            isPlaying = true
            audioTrack?.play()
            start()
        }
    }

    fun stop() {
        isPlaying = false
        playerJob?.cancel()
        playerJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Safeguard
        }
        audioTrack = null
    }
}
