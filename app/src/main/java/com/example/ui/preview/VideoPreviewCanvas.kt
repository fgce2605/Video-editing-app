package com.example.ui.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioPreset
import com.example.data.model.Clip
import com.example.data.model.ColorGrading
import com.example.data.model.FilterPreset
import com.example.data.model.SubtitleSegment
import com.example.data.model.TextOverlay
import com.example.data.model.Track
import com.example.data.model.TransitionType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlin.math.sin

@Composable
fun VideoPreviewCanvas(
    aspectRatioPreset: AspectRatioPreset,
    currentTimeMs: Long,
    totalDurationMs: Long,
    isPlaying: Boolean,
    tracks: List<Track>,
    textOverlays: List<TextOverlay>,
    subtitles: List<SubtitleSegment>,
    selectedTextOverlayId: String?,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    formatTimestamp: (Long) -> String,
    modifier: Modifier = Modifier
) {
    var showControls by remember { mutableStateOf(true) }

    // Find active clip at current timestamp
    val activeClip = remember(tracks, currentTimeMs) {
        var found: Clip? = null
        for (track in tracks) {
            for (clip in track.clips) {
                if (currentTimeMs >= clip.startInTimelineMs && currentTimeMs <= clip.startInTimelineMs + clip.durationMs) {
                    found = clip
                    break
                }
            }
            if (found != null) break
        }
        found
    }

    // Active subtitles
    val activeSubtitle = remember(subtitles, currentTimeMs) {
        subtitles.find { currentTimeMs in it.startMs..it.endMs }
    }

    // Active text overlays
    val activeOverlays = remember(textOverlays, currentTimeMs) {
        textOverlays.filter { currentTimeMs in it.startInTimelineMs..(it.startInTimelineMs + it.durationMs) }
    }

    val canvasRatio = remember(aspectRatioPreset) {
        aspectRatioPreset.ratioWidth.toFloat() / aspectRatioPreset.ratioHeight.toFloat()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Preview Canvas Frame Container
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF05060A))
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                    .clickable { showControls = !showControls },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(canvasRatio)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF222838), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Render Animated Simulated Frame Shader
                    SimulatedVideoFrame(
                        clip = activeClip,
                        currentTimeMs = currentTimeMs,
                        isPlaying = isPlaying
                    )

                    // Render Active Text Overlays
                    activeOverlays.forEach { overlay ->
                        val isSelected = overlay.id == selectedTextOverlayId
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .scale(overlay.scale)
                                .rotate(overlay.rotationDeg)
                                .clip(RoundedCornerShape(6.dp))
                                .background(parseColor(overlay.bgColorHex))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) StudioCyanAI else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = overlay.text,
                                color = parseColor(overlay.colorHex),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = when (overlay.fontStyle) {
                                    "Serif" -> FontFamily.Serif
                                    "Monospace" -> FontFamily.Monospace
                                    "Display" -> FontFamily.SansSerif
                                    else -> FontFamily.Default
                                }
                            )
                        }
                    }

                    // Render Subtitles
                    activeSubtitle?.let { sub ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xDD0B0D14))
                                .border(1.dp, Color(0x443D7EFF), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Subtitles",
                                    tint = StudioCyanAI,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = sub.text,
                                    color = StudioTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Transition Indicator Effect
                    if (activeClip != null && activeClip.transitionIn != TransitionType.NONE) {
                        val offsetMs = currentTimeMs - activeClip.startInTimelineMs
                        if (offsetMs in 0..1000) {
                            val alpha = 1.0f - (offsetMs / 1000f)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        when (activeClip.transitionIn) {
                                            TransitionType.FADE -> Color.Black.copy(alpha = alpha)
                                            TransitionType.BLUR_FLASH -> Color.White.copy(alpha = alpha * 0.8f)
                                            else -> Color(0xFF3D7EFF).copy(alpha = alpha * 0.4f)
                                        }
                                    )
                            )
                        }
                    }

                    // Top Status Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) StudioRedPrimary else Color.Gray)
                        )
                        Text(
                            text = aspectRatioPreset.displayName,
                            color = StudioTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Top Right Timecode
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatTimestamp(currentTimeMs),
                            color = StudioCyanAI,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Player Scrubber & Control Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(currentTimeMs),
                    color = StudioTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Slider(
                    value = if (totalDurationMs > 0) currentTimeMs.toFloat() / totalDurationMs.toFloat() else 0f,
                    onValueChange = { fraction ->
                        onSeek((fraction * totalDurationMs).toLong())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("player_scrubber_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioRedPrimary,
                        activeTrackColor = StudioRedPrimary,
                        inactiveTrackColor = StudioBorder
                    )
                )

                Text(
                    text = formatTimestamp(totalDurationMs),
                    color = StudioTextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Transport Control Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                IconButton(
                    onClick = onStepBackward,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("step_backward_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Step Back",
                        tint = StudioTextSecondary
                    )
                }

                Surface(
                    onClick = onTogglePlay,
                    shape = CircleShape,
                    color = StudioRedPrimary,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("play_pause_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onStepForward,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("step_forward_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Step Forward",
                        tint = StudioTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatedVideoFrame(
    clip: Clip?,
    currentTimeMs: Long,
    isPlaying: Boolean
) {
    if (clip == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F17)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("PROEDIT STUDIO", color = StudioTextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("No clip at current playhead position", color = Color(0xFF4A5168), fontSize = 11.sp)
            }
        }
        return
    }

    val animTime = (currentTimeMs / 30f)
    val colorGrading = clip.colorGrading

    val filterColors = when (colorGrading.filterPreset) {
        FilterPreset.CYBERPUNK -> listOf(Color(0xFF00E5FF), Color(0xFFD500F9), Color(0xFF0F111A))
        FilterPreset.GOLDEN_HOUR -> listOf(Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFF3E2723))
        FilterPreset.CINEMATIC -> listOf(Color(0xFF00B0FF), Color(0xFFFF3D00), Color(0xFF102027))
        FilterPreset.NOIR -> listOf(Color(0xFFE0E0E0), Color(0xFF616161), Color(0xFF121212))
        FilterPreset.VINTAGE -> listOf(Color(0xFFFFECB3), Color(0xFF8D6E63), Color(0xFF263238))
        FilterPreset.VIVID -> listOf(Color(0xFF00E676), Color(0xFFFF1744), Color(0xFF2979FF))
        FilterPreset.MOODY -> listOf(Color(0xFF37474F), Color(0xFF212121), Color(0xFF000000))
        FilterPreset.NONE -> listOf(Color(0xFF3D7EFF), Color(0xFFFF5252), Color(0xFF0B0D14))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(clip.cropConfig.scale)
            .rotate(clip.cropConfig.rotationDeg)
            .background(
                Brush.linearGradient(
                    colors = filterColors,
                    start = Offset(0f, sin(animTime * 0.05f) * 400f),
                    end = Offset(800f, 600f)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw simulated video motion waveforms / graphics
            val path = Path()
            path.moveTo(0f, h * 0.5f)
            for (x in 0..w.toInt() step 20) {
                val y = h * 0.5f + sin((x * 0.02f) + (animTime * 0.1f)) * 60f
                path.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.25f),
                style = Stroke(width = 3f)
            )

            // Draw center graphic badge
            drawCircle(
                color = filterColors[0].copy(alpha = 0.3f),
                radius = 120f + (sin(animTime * 0.1f) * 20f),
                center = Offset(w * 0.5f, h * 0.5f)
            )
        }

        Text(
            text = clip.title,
            color = StudioTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0x66000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorInt = cleaned.toLong(16)
        if (cleaned.length == 8) Color(colorInt) else Color(colorInt or 0xFF000000)
    } catch (e: Exception) {
        Color.White
    }
}
