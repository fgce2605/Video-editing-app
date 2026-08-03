package com.example.ui.preview

import android.net.Uri
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.AspectRatioPreset
import com.example.data.model.Clip
import com.example.data.model.ColorGrading
import com.example.data.model.FilterPreset
import com.example.data.model.SubtitleSegment
import com.example.data.model.TextOverlay
import com.example.data.model.Track
import com.example.data.model.TrackType
import com.example.data.model.TransitionType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import java.io.File
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
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }

    // Initialize ExoPlayer safely
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = (playbackState == Player.STATE_BUFFERING)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Find active clip on primary video track at current timestamp
    val activeClip = remember(tracks, currentTimeMs) {
        var found: Clip? = null
        for (track in tracks) {
            if (track.type == TrackType.VIDEO_PRIMARY) {
                for (clip in track.clips) {
                    if (currentTimeMs >= clip.startInTimelineMs && currentTimeMs <= clip.startInTimelineMs + clip.durationMs) {
                        found = clip
                        break
                    }
                }
            }
            if (found != null) break
        }
        // Fallback to any video clip if primary track clip not found
        if (found == null) {
            for (track in tracks) {
                for (clip in track.clips) {
                    if (currentTimeMs >= clip.startInTimelineMs && currentTimeMs <= clip.startInTimelineMs + clip.durationMs) {
                        found = clip
                        break
                    }
                }
                if (found != null) break
            }
        }
        found
    }

    // Find active clip on secondary video overlay track
    val activeOverlayClip = remember(tracks, currentTimeMs) {
        var found: Clip? = null
        for (track in tracks) {
            if (track.type == TrackType.VIDEO_OVERLAY) {
                for (clip in track.clips) {
                    if (currentTimeMs >= clip.startInTimelineMs && currentTimeMs <= clip.startInTimelineMs + clip.durationMs) {
                        found = clip
                        break
                    }
                }
            }
            if (found != null) break
        }
        found
    }

    // Determine if active clip has a valid local file / URI to play via ExoPlayer
    val isRealMediaFile = remember(activeClip?.mediaId) {
        activeClip != null && isMediaUriValid(activeClip.mediaId)
    }

    // Load media item into ExoPlayer when mediaUri changes
    LaunchedEffect(activeClip?.mediaId) {
        if (activeClip != null && isRealMediaFile) {
            try {
                val mediaUri = parseMediaUri(activeClip.mediaId)
                exoPlayer.setMediaItem(MediaItem.fromUri(mediaUri))
                exoPlayer.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    // Sync playback position & speed with ExoPlayer
    LaunchedEffect(currentTimeMs, activeClip?.id, isRealMediaFile) {
        if (activeClip != null && isRealMediaFile) {
            val offsetMs = (currentTimeMs - activeClip.startInTimelineMs)
            val clipInternalMs = (offsetMs * activeClip.speed + activeClip.sourceTrimStartMs).toLong()
            val boundedMs = clipInternalMs.coerceAtLeast(0L)
            if (kotlin.math.abs(exoPlayer.currentPosition - boundedMs) > 150) {
                exoPlayer.seekTo(boundedMs)
            }
            exoPlayer.playbackParameters = PlaybackParameters(activeClip.speed)
        }
    }

    // Play / Pause ExoPlayer sync
    LaunchedEffect(isPlaying, isRealMediaFile) {
        if (isRealMediaFile) {
            if (isPlaying) {
                exoPlayer.play()
            } else {
                exoPlayer.pause()
            }
        }
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
                    // Main Video Player / Canvas Container with crop & scale transforms
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                if (activeClip != null) {
                                    scaleX = activeClip.cropConfig.scale
                                    scaleY = activeClip.cropConfig.scale
                                    rotationZ = activeClip.cropConfig.rotationDeg
                                    translationX = activeClip.cropConfig.offsetX
                                    translationY = activeClip.cropConfig.offsetY
                                }
                            }
                    ) {
                        if (isRealMediaFile) {
                            // ExoPlayer View for imported media files
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        useController = false
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                        player = exoPlayer
                                    }
                                },
                                update = { view ->
                                    view.player = exoPlayer
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Stylized Video Frame Shader for virtual/sample clips
                            SimulatedVideoFrame(
                                clip = activeClip,
                                currentTimeMs = currentTimeMs,
                                isPlaying = isPlaying
                            )
                        }

                        // Live Color Grading & Filter Preset Overlay
                        activeClip?.colorGrading?.let { grading ->
                            if (grading.brightness != 0f || grading.contrast != 0f ||
                                grading.saturation != 0f || grading.warmth != 0f ||
                                grading.filterPreset != FilterPreset.NONE) {
                                ColorGradingFilterOverlay(colorGrading = grading)
                            }
                        }
                    }

                    // Multi-track Video Overlay (Picture-in-Picture)
                    activeOverlayClip?.let { overlayClip ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(110.dp, 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.5.dp, StudioCyanAI, RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            SimulatedVideoFrame(
                                clip = overlayClip,
                                currentTimeMs = currentTimeMs,
                                isPlaying = isPlaying
                            )
                            Text(
                                text = "B-Roll Overlay",
                                color = StudioCyanAI,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 2.dp)
                            )
                        }
                    }

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

                    // Buffering Indicator Overlay
                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x77000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = StudioCyanAI,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Buffering Preview...",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
private fun ColorGradingFilterOverlay(
    colorGrading: ColorGrading,
    modifier: Modifier = Modifier
) {
    val matrix = remember(colorGrading) {
        buildColorMatrixForGrading(colorGrading)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            color = Color.White.copy(alpha = 0.05f),
            colorFilter = ColorFilter.colorMatrix(matrix)
        )
    }
}

private fun buildColorMatrixForGrading(colorGrading: ColorGrading): ColorMatrix {
    val matrix = ColorMatrix()

    // Saturation
    val sat = (1.0f + colorGrading.saturation / 100f).coerceIn(0f, 3f)
    matrix.setToSaturation(sat)

    val vals = matrix.values
    val c = (1.0f + colorGrading.contrast / 100f).coerceIn(0.1f, 3f)
    val b = colorGrading.brightness * 2.55f

    // Adjust contrast and brightness offsets
    vals[0] *= c; vals[4] += b
    vals[6] *= c; vals[9] += b
    vals[12] *= c; vals[14] += b

    if (colorGrading.warmth != 0f) {
        val w = colorGrading.warmth * 1.5f
        vals[0] += w
        vals[12] -= w * 0.5f
    }

    // Apply Filter Preset styling
    when (colorGrading.filterPreset) {
        FilterPreset.CYBERPUNK -> {
            vals[0] *= 0.8f; vals[2] += 0.4f
            vals[6] *= 0.7f; vals[8] += 0.5f
            vals[10] *= 1.2f
        }
        FilterPreset.GOLDEN_HOUR -> {
            vals[0] *= 1.2f; vals[4] += 15f
            vals[6] *= 1.0f; vals[9] += 10f
            vals[12] *= 0.8f
        }
        FilterPreset.CINEMATIC -> {
            vals[0] *= 0.9f; vals[2] += 0.2f
            vals[6] *= 1.05f
            vals[10] *= 0.85f; vals[14] += 10f
        }
        FilterPreset.NOIR -> {
            matrix.setToSaturation(0f)
            val nVals = matrix.values
            nVals[0] *= 1.2f; nVals[6] *= 1.2f; nVals[12] *= 1.2f
        }
        FilterPreset.VINTAGE -> {
            vals[0] *= 1.1f; vals[4] += 10f
            vals[6] *= 0.95f
            vals[12] *= 0.85f
        }
        FilterPreset.VIVID -> {
            matrix.setToSaturation(1.5f)
        }
        FilterPreset.MOODY -> {
            matrix.setToSaturation(0.6f)
            vals[4] -= 15f; vals[9] -= 15f; vals[14] -= 5f
        }
        FilterPreset.NONE -> {}
    }

    return matrix
}

private fun isMediaUriValid(mediaId: String): Boolean {
    if (mediaId.startsWith("content://") || mediaId.startsWith("file://") || mediaId.startsWith("http://") || mediaId.startsWith("https://")) {
        return true
    }
    if (mediaId.startsWith("/")) {
        val file = File(mediaId)
        return file.exists() && file.length() > 0
    }
    return false
}

private fun parseMediaUri(mediaId: String): Uri {
    return if (mediaId.startsWith("/")) {
        Uri.fromFile(File(mediaId))
    } else {
        Uri.parse(mediaId)
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
