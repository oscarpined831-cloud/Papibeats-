package com.genesis.papibeats

data class SoundProfile(
    val genreName: String,
    val profileTitle: String,
    val subBassGain: Float,
    val midPunchGain: Float,
    val trebleShimmerGain: Float,
    val spatialWidthDeg: Int, // 60 to 360
    val dynamicLimiterEnabled: Boolean,
    val explanation: String
)

class PapibeatsAudioEngine {
    private val spatialEQ = SubjectiveSpatialEQ()
    private val roomAcoustics = RoomAcousticsEngine()

    fun getNativeProfileForTrack(track: Track): SoundProfile {
        return when {
            track.genre.contains("Electrónica", ignoreCase = true) || track.genre.contains("tecno", ignoreCase = true) -> {
                SoundProfile(
                    genreName = "Electrónica y tecno",
                    profileTitle = "Ultra Sub-Bass 45Hz + Air Shimmer 12kHz",
                    subBassGain = 14f,
                    midPunchGain = 2f,
                    trebleShimmerGain = 10f,
                    spatialWidthDeg = 360,
                    dynamicLimiterEnabled = true,
                    explanation = "Calibración nativa para 124 BPM: Subgraves profundos sin saturación, transitorios rápidos en kick/bassline y expansión espacial 3D inmersiva."
                )
            }
            track.genre.contains("Urbano", ignoreCase = true) || track.genre.contains("Dembow", ignoreCase = true) -> {
                SoundProfile(
                    genreName = "Urbano & Dembow",
                    profileTitle = "Dembow Sub-Kick 55Hz + Vocal Focus 3kHz",
                    subBassGain = 16f,
                    midPunchGain = 4f,
                    trebleShimmerGain = 6f,
                    spatialWidthDeg = 180,
                    dynamicLimiterEnabled = true,
                    explanation = "Alineación nativa para Dembow a 111 BPM: Golpe seco y contundente en 55Hz, presencia vocal aislada al frente."
                )
            }
            track.genre.contains("Synthwave", ignoreCase = true) || track.genre.contains("Pop", ignoreCase = true) -> {
                SoundProfile(
                    genreName = "Synthwave & Pop",
                    profileTitle = "Analog Warmth + Crisp Snare 4kHz",
                    subBassGain = 11f,
                    midPunchGain = 5f,
                    trebleShimmerGain = 12f,
                    spatialWidthDeg = 270,
                    dynamicLimiterEnabled = false,
                    explanation = "Calibración rítmica a 186 BPM: Bajos sintetizados cálidos, baterías nítidas y rango dinámico expansivo."
                )
            }
            track.genre.contains("Hip-Hop", ignoreCase = true) || track.genre.contains("Trap", ignoreCase = true) -> {
                SoundProfile(
                    genreName = "Hip-Hop & Trap",
                    profileTitle = "808 Heavy Saturation 35Hz + Hi-Hat Sizzle",
                    subBassGain = 18f,
                    midPunchGain = 1f,
                    trebleShimmerGain = 14f,
                    spatialWidthDeg = 240,
                    dynamicLimiterEnabled = true,
                    explanation = "Graves 808 ultra-pesados con anti-distorsión armónica Sovereign y brillo metálico en hi-hats."
                )
            }
            else -> {
                SoundProfile(
                    genreName = track.genre,
                    profileTitle = "Respuesta Dinámica Natural + Sala Sonora",
                    subBassGain = 7f,
                    midPunchGain = 8f,
                    trebleShimmerGain = 8f,
                    spatialWidthDeg = 180,
                    dynamicLimiterEnabled = false,
                    explanation = "Acústica orgánica fiel a la grabación: Medios detallados para voces e instrumentos con calidez en sala."
                )
            }
        }
    }
}
