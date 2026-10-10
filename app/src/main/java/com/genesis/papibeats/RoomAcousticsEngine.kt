package com.genesis.papibeats

import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.pow

class RoomAcousticsEngine(val sampleRate: Int = 44100) {

    data class RoomModeResult(
        val lengthMode: Float,
        val widthMode: Float,
        val heightMode: Float,
        val status: String
    )

    fun analyzeRoomModes(lengthM: Float, widthM: Float, heightM: Float): RoomModeResult {
        val c = 343.0f // Speed of sound in m/s
        val fL = if (lengthM > 0f) c / (2 * lengthM) else 0f
        val fW = if (widthM > 0f) c / (2 * widthM) else 0f
        val fH = if (heightM > 0f) c / (2 * heightM) else 0f
        return RoomModeResult(
            lengthMode = fL,
            widthMode = fW,
            heightMode = fH,
            status = "Modos axiales calculados (SovereignEMA_G5618)"
        )
    }

    fun generateSineSweep(startFreq: Float = 20f, endFreq: Float = 20000f, durationSec: Float = 1f): FloatArray {
        val numSamples = (sampleRate * durationSec).toInt()
        val sweep = FloatArray(numSamples)
        val k = (endFreq / startFreq).toDouble().pow(1.0 / durationSec)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val phase = 2 * PI * startFreq * ((k.pow(t.toDouble()) - 1.0) / ln(k))
            sweep[i] = sin(phase).toFloat()
        }
        return sweep
    }
}
