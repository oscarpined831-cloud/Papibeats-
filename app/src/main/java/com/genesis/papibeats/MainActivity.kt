package com.genesis.papibeats

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class PlayerViewModel(private val spotifyRemote: SpotifyAppRemoteController) : ViewModel() {
    private val audioEngine = PapibeatsAudioEngine()
    private val spatialEQ = SubjectiveSpatialEQ()

    private val playlist = SpotifyMediaController.DEFAULT_TRACKS
    var currentTrackIndex by mutableStateOf(0)
    val currentTrack: Track get() = playlist[currentTrackIndex]

    var isPlaying by mutableStateOf(true)
    var currentPositionSeconds by mutableStateOf(225) // 3:45
    var isShuffle by mutableStateOf(false)
    var isRepeat by mutableStateOf(false)
    var isSaved by mutableStateOf(true)
    var isSpotifyConnected by mutableStateOf(false)

    // Papibeats Modes & Sound state
    var isAutoNativeMode by mutableStateOf(true)
    var activeSpecialMode by mutableStateOf("NORMAL") // ANC, TALKTHRU, NORMAL
    var spatialStatusText by mutableStateOf("Centro Neutral (0dB)")

    // Dynamic Gains
    var subBassGain by mutableStateOf(14f)
    var midGain by mutableStateOf(2f)
    var trebleGain by mutableStateOf(10f)

    val currentNativeProfile: SoundProfile
        get() = audioEngine.getNativeProfileForTrack(currentTrack)

    init {
        applyNativeCalibration()
        connectSpotifyRemote()
    }

    private fun connectSpotifyRemote() {
        spotifyRemote.connect(
            onConnected = { isSpotifyConnected = true },
            onError = { isSpotifyConnected = false }
        )
    }

    fun applyNativeCalibration() {
        val profile = currentNativeProfile
        subBassGain = profile.subBassGain
        midGain = profile.midPunchGain
        trebleGain = profile.trebleShimmerGain
        spatialStatusText = "Alineado a ritmo: ${currentTrack.genre} (${currentTrack.bpm} BPM)"
    }

    fun togglePlayPause() {
        isPlaying = !isPlaying
        if (isPlaying) {
            spotifyRemote.play()
        } else {
            spotifyRemote.pause()
        }
    }

    fun nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size
        currentPositionSeconds = 0
        if (isAutoNativeMode) applyNativeCalibration()
        spotifyRemote.skipNext()
    }

    fun previousTrack() {
        if (currentPositionSeconds > 3) {
            currentPositionSeconds = 0
        } else {
            currentTrackIndex = if (currentTrackIndex - 1 < 0) playlist.size - 1 else currentTrackIndex - 1
            currentPositionSeconds = 0
        }
        if (isAutoNativeMode) applyNativeCalibration()
        spotifyRemote.skipPrevious()
    }

    fun seekTo(seconds: Int) {
        currentPositionSeconds = seconds.coerceIn(0, currentTrack.durationSeconds)
    }

    fun toggleSpecialMode(mode: String) {
        val newMode = if (activeSpecialMode == mode) "NORMAL" else mode
        activeSpecialMode = newMode
        spatialStatusText = spatialEQ.setSpecialMode(newMode)
    }

    fun updateSpatialCoordinates(yPercent: Float) {
        val yAxis = when {
            yPercent < 0.35f -> "HIGH_TOP"
            yPercent > 0.65f -> "LOW_BOTTOM"
            else -> "CENTER"
        }
        val freq = if (yAxis == "HIGH_TOP") 10000f else if (yAxis == "LOW_BOTTOM") 60f else 1000f
        spatialEQ.mapFrequencyToSpace(yAxis, "CENTER", freq)

        when (yAxis) {
            "HIGH_TOP" -> {
                trebleGain = 15f
                spatialStatusText = "AGUDOS EXTREMOS (+15dB)"
            }
            "LOW_BOTTOM" -> {
                subBassGain = 16f
                spatialStatusText = "BASS BOOST (+12dB)"
            }
            else -> {
                spatialStatusText = "CENTRO (0dB)"
            }
        }
        isAutoNativeMode = false
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var spotifyRemote: SpotifyAppRemoteController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, SovereignDSPService::class.java))
        spotifyRemote = SpotifyAppRemoteController(this)

        setContent {
            val viewModel: PlayerViewModel = viewModel { PlayerViewModel(spotifyRemote) }
            PapibeatsSpotifyScreen(viewModel)
        }
    }

    override fun onStart() {
        super.onStart()
        spotifyRemote.connect({}, {})
    }

    override fun onStop() {
        super.onStop()
        spotifyRemote.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PapibeatsSpotifyScreen(viewModel: PlayerViewModel) {
    val track = viewModel.currentTrack
    val scrollState = rememberScrollState()

    LaunchedEffect(viewModel.isPlaying, track.id) {
        while (viewModel.isPlaying) {
            delay(1000)
            if (viewModel.currentPositionSeconds < track.durationSeconds) {
                viewModel.currentPositionSeconds += 1
            } else {
                viewModel.nextTrack()
            }
        }
    }

    val dominantColor = Color(track.dominantColorHex)
    val secondaryColor = Color(track.secondaryColorHex)
    val bottomBgColor = Color(0xFF0C1017)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            dominantColor,
            secondaryColor,
            bottomBgColor,
            Color(0xFF0A0A0A)
        )
    )

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                SpotifyTopBar(
                    playlistName = track.playlistName,
                    isRemoteConnected = viewModel.isSpotifyConnected
                )

                Spacer(Modifier.height(18.dp))

                AlbumArtwork(
                    track = track,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(vertical = 4.dp)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = track.lyricsSnippet,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = track.artist,
                            color = Color(0xFFB3B3B3),
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { viewModel.isSaved = !viewModel.isSaved },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isSaved) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                            contentDescription = "Guardado",
                            tint = if (viewModel.isSaved) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                SpotifyProgressBar(
                    currentSeconds = viewModel.currentPositionSeconds,
                    totalSeconds = track.durationSeconds,
                    onSeek = { viewModel.seekTo(it) }
                )

                Spacer(Modifier.height(8.dp))

                SpotifyTransportControls(
                    isPlaying = viewModel.isPlaying,
                    isShuffle = viewModel.isShuffle,
                    isRepeat = viewModel.isRepeat,
                    onShuffleClick = { viewModel.isShuffle = !viewModel.isShuffle },
                    onPrevClick = { viewModel.previousTrack() },
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.nextTrack() },
                    onRepeatClick = { viewModel.isRepeat = !viewModel.isRepeat }
                )

                Spacer(Modifier.height(12.dp))

                AudioDeviceBar(deviceName = "1HORA-AUT211 (Spotify Remote)")

                Spacer(Modifier.height(20.dp))

                PapibeatsControlPanel(viewModel)

                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
fun SpotifyTopBar(playlistName: String, isRemoteConnected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimizar", tint = Color.White, modifier = Modifier.size(28.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isRemoteConnected) "SPOTIFY APP REMOTE ⚡" else "REPRODUCIENDO DESDE PLAYLIST",
                color = if (isRemoteConnected) Color(0xFF1DB954) else Color(0xFFB3B3B3),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = playlistName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun SpotifyProgressBar(currentSeconds: Int, totalSeconds: Int, onSeek: (Int) -> Unit) {
    val progress = if (totalSeconds > 0) (currentSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f) else 0f
    val remainingSeconds = totalSeconds - currentSeconds

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { onSeek((it * totalSeconds).toInt()) },
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().height(20.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(currentSeconds), color = Color(0xFFB3B3B3), fontSize = 12.sp)
            Text("-${formatTime(remainingSeconds)}", color = Color(0xFFB3B3B3), fontSize = 12.sp)
        }
    }
}

fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
fun SpotifyTransportControls(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onShuffleClick: () -> Unit,
    onPrevClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onShuffleClick) {
            Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = if (isShuffle) Color(0xFF1DB954) else Color(0xFFB3B3B3), modifier = Modifier.size(24.dp))
        }
        IconButton(onClick = onPrevClick) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(38.dp))
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onPlayPauseClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(38.dp))
        }
        IconButton(onClick = onRepeatClick) {
            Icon(Icons.Default.Repeat, contentDescription = "Repetir", tint = if (isRepeat) Color(0xFF1DB954) else Color(0xFFB3B3B3), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun AudioDeviceBar(deviceName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = deviceName, color = Color(0xFF1DB954), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = {}, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Cola", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun PapibeatsControlPanel(viewModel: PlayerViewModel) {
    val green = Color(0xFF1DB954)
    val ancColor = Color(0xFF00F2FE)
    val talkColor = Color(0xFFF2A900)
    val cardBg = Color(0xFF121824)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("PAPIBEATS GOD MODE", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Spotify Remote & Sovereign DSP", color = green, fontSize = 11.sp)
                    }
                }

                FilterChip(
                    selected = viewModel.isAutoNativeMode,
                    onClick = {
                        viewModel.isAutoNativeMode = !viewModel.isAutoNativeMode
                        if (viewModel.isAutoNativeMode) viewModel.applyNativeCalibration()
                    },
                    label = { Text(if (viewModel.isAutoNativeMode) "✓ Auto Nativo" else "Modo Libre", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = green,
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF222B3A),
                        labelColor = Color.White
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0E141E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (viewModel.isAutoNativeMode) "🎯 CALIBRACIÓN RÍTMICA NATIVA" else "🛠️ MODO MANUAL",
                            color = if (viewModel.isAutoNativeMode) green else Color(0xFFFFA726),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${viewModel.currentTrack.bpm} BPM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(viewModel.currentNativeProfile.explanation, color = Color(0xFFD0D7DE), fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    AnimatedRhythmVisualizer(isPlaying = viewModel.isPlaying, color = green)
                }
            }

            Text("📍 Mapeador Espacial 3D (Toca y arrastra)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            InteractiveSpatialSpherePad(
                statusText = viewModel.spatialStatusText,
                specialMode = viewModel.activeSpecialMode,
                green = green,
                ancColor = ancColor,
                talkColor = talkColor,
                onPositionChanged = { viewModel.updateSpatialCoordinates(it) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.toggleSpecialMode("ANC") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.activeSpecialMode == "ANC") ancColor else Color(0xFF1E2636),
                        contentColor = if (viewModel.activeSpecialMode == "ANC") Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🛡️ ANC Escudo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { viewModel.toggleSpecialMode("TALKTHRU") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.activeSpecialMode == "TALKTHRU") talkColor else Color(0xFF1E2636),
                        contentColor = if (viewModel.activeSpecialMode == "TALKTHRU") Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🗣️ TalkThru", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Text("🎛️ Ganancia DSP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sub-Bass (45Hz)", color = Color.White, fontSize = 12.sp)
                    Text("${viewModel.subBassGain.toInt()} dB", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = viewModel.subBassGain,
                    onValueChange = { viewModel.subBassGain = it; viewModel.isAutoNativeMode = false },
                    valueRange = -10f..20f,
                    colors = SliderDefaults.colors(thumbColor = green, activeTrackColor = green)
                )
            }
        }
    }
}

@Composable
fun InteractiveSpatialSpherePad(
    statusText: String,
    specialMode: String,
    green: Color,
    ancColor: Color,
    talkColor: Color,
    onPositionChanged: (Float) -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF090E16))
    ) {
        Text(
            text = statusText,
            color = when (specialMode) { "ANC" -> ancColor; "TALKTHRU" -> talkColor; else -> green },
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(when (specialMode) { "ANC" -> ancColor; "TALKTHRU" -> talkColor; else -> green }, Color(0xFF141A22))
                    )
                )
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetY = (offsetY + dragAmount.y).coerceIn(-100f, 100f)
                        onPositionChanged((offsetY + 100f) / 200f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⚡", fontSize = 18.sp)
        }
    }
}

@Composable
fun AnimatedRhythmVisualizer(isPlaying: Boolean, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val heights = listOf(
        infiniteTransition.animateFloat(0.3f, 0.9f, infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse), label = "b1"),
        infiniteTransition.animateFloat(0.5f, 1.0f, infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse), label = "b2"),
        infiniteTransition.animateFloat(0.2f, 0.8f, infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse), label = "b3"),
        infiniteTransition.animateFloat(0.6f, 0.95f, infiniteRepeatable(tween(560, easing = LinearEasing), RepeatMode.Reverse), label = "b4")
    )

    Row(
        modifier = Modifier.fillMaxWidth().height(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { animVal ->
            val scale = if (isPlaying) animVal.value else 0.2f
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.85f))
            )
        }
    }
}
