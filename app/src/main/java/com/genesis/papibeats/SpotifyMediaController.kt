package com.genesis.papibeats

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val playlistName: String,
    val genre: String,
    val bpm: Int,
    val durationSeconds: Int,
    val initialPositionSeconds: Int,
    val lyricsSnippet: String,
    val albumArtUrl: String?,
    val dominantColorHex: Long,
    val secondaryColorHex: Long
)

class SpotifyMediaController(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun sendMediaKey(keyCode: Int) {
        val eventTime = SystemClock.uptimeMillis()
        try {
            audioManager.dispatchMediaKeyEvent(
                KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0)
            )
            audioManager.dispatchMediaKeyEvent(
                KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0)
            )
        } catch (_: Exception) {}
    }

    fun playPause() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    fun next() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun previous() = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    companion object {
        val DEFAULT_TRACKS = listOf(
            Track(
                id = "innerbloom",
                title = "Innerbloom - Radio Edit",
                artist = "RÜFÜS DU SOL",
                playlistName = "Electrónica y tecno",
                genre = "Electrónica y tecno",
                bpm = 124,
                durationSeconds = 237, // 3:57
                initialPositionSeconds = 225, // 3:45
                lyricsSnippet = "If you need me",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273b5a153d10034a7fb50da0592",
                dominantColorHex = 0xFF2A364F,
                secondaryColorHex = 0xFF1B2333
            ),
            Track(
                id = "titi",
                title = "Tití Me Preguntó",
                artist = "Bad Bunny",
                playlistName = "Éxitos Urbanos 2026",
                genre = "Urbano & Dembow",
                bpm = 111,
                durationSeconds = 243,
                initialPositionSeconds = 95,
                lyricsSnippet = "Ey, Tití me preguntó si tengo muchas novia'",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b27349d694203245f241a1bcaa70",
                dominantColorHex = 0xFF4A1E2B,
                secondaryColorHex = 0xFF281119
            ),
            Track(
                id = "starboy",
                title = "Starboy",
                artist = "The Weeknd ft. Daft Punk",
                playlistName = "Synthwave & Pop",
                genre = "Synthwave & Electro-Pop",
                bpm = 186,
                durationSeconds = 230,
                initialPositionSeconds = 80,
                lyricsSnippet = "Look what you've done, I'm a motherfuckin' starboy",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b2734718e2b124f79258be7bc452",
                dominantColorHex = 0xFF421C4F,
                secondaryColorHex = 0xFF1F0F29
            ),
            Track(
                id = "humble",
                title = "HUMBLE.",
                artist = "Kendrick Lamar",
                playlistName = "Hip-Hop Classics",
                genre = "Hip-Hop & Trap",
                bpm = 150,
                durationSeconds = 177,
                initialPositionSeconds = 64,
                lyricsSnippet = "Bitch, be humble, sit down",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b2738b52c6b9bc4e43d873869699",
                dominantColorHex = 0xFF4A1F1F,
                secondaryColorHex = 0xFF240F0F
            ),
            Track(
                id = "bohemian",
                title = "Bohemian Rhapsody",
                artist = "Queen",
                playlistName = "Rock Legendario",
                genre = "Rock Clásico & Progresivo",
                bpm = 72,
                durationSeconds = 354,
                initialPositionSeconds = 120,
                lyricsSnippet = "Is this the real life? Is this just fantasy?",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273e319baafd16e84f0408af2a0",
                dominantColorHex = 0xFF1F2E3D,
                secondaryColorHex = 0xFF0D1721
            )
        )
    }
}
