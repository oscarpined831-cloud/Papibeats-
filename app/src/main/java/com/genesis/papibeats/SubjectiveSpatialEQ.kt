package com.genesis.papibeats

class SubjectiveSpatialEQ {
    var guard: Boolean = true

    fun mapFrequencyToSpace(yAxis: String, xAxis: String, freq: Float): Map<String, Any> {
        val gain = when (yAxis) {
            "HIGH_TOP" -> 15.0f
            "LOW_BOTTOM" -> 12.0f
            else -> 0.0f
        }
        val autoGain = if (guard && gain != 0.0f) -gain else 0.0f
        return mapOf(
            "gain" to gain,
            "autoGain" to autoGain,
            "yAxis" to yAxis,
            "xAxis" to xAxis,
            "freq" to freq,
            "status" to "DSP Ajustado: ${gain}dB (${yAxis})"
        )
    }

    fun setSpecialMode(mode: String): String {
        return when (mode) {
            "ANC" -> "GOD MODE: ESCUDO ANC ACTIVADO - Fase inversa inyectada."
            "TALKTHRU" -> "GOD MODE: TALKTHRU ACTIVADO - Bajos cortados, vocales amplificadas."
            else -> "GOD MODE: DESACTIVADO (Modo Normal)"
        }
    }
}
