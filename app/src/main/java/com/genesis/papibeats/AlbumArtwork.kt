package com.genesis.papibeats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlbumArtwork(
    track: Track,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(24.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color(track.dominantColorHex))
    ) {
        if (!track.albumArtUrl.isNullOrEmpty()) {
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = "Carátula de ${track.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Procedural art fallback / high fidelity texture if offline or loading
        if (track.id == "innerbloom") {
            InnerbloomProceduralCover(track)
        }
    }
}

@Composable
fun InnerbloomProceduralCover(track: Track) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.52f, size.height * 0.48f)
            val maxR = size.minDimension * 0.55f

            // Base gradient
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE8B4B8), // Soft pink center
                        Color(0xFF6B4226), // Copper brown
                        Color(0xFF33202C), // Deep plum
                        Color(0xFF1E2837), // Dark slate blue
                        Color(0xFF141922)  // Deep outer edge
                    ),
                    center = center,
                    radius = maxR * 1.2f
                )
            )

            // Concentric vortex rings replicating the oil paint swirl
            val ringColors = listOf(
                Color(0xAAE5A9B4),
                Color(0xCC8C533C),
                Color(0xDD3B2433),
                Color(0xCCE8B7BE),
                Color(0xEE2A3545),
                Color(0xCCB36B51),
                Color(0xFFE0A3AF),
                Color(0xEE1E2430)
            )

            for (i in 1..24) {
                val r = (maxR / 24f) * i
                val color = ringColors[i % ringColors.size]
                val strokeWidth = (maxR / 18f) * (0.6f + (i % 3) * 0.3f)
                val distortionX = (cos(i.toDouble() * 0.8) * 6f).toFloat()
                val distortionY = (sin(i.toDouble() * 0.8) * 6f).toFloat()

                drawCircle(
                    color = color,
                    radius = r,
                    center = Offset(center.x + distortionX, center.y + distortionY),
                    style = Stroke(width = strokeWidth)
                )
            }
        }

        // Top-left branding typography as seen in original cover
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            Text(
                text = "RÜFÜS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            Text(
                text = "INNERBLOOM",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
        }
    }
}
