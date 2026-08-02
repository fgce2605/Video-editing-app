package com.example.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioClip
import com.example.data.model.Clip
import com.example.data.model.SubtitleSegment
import com.example.data.model.TextOverlay
import com.example.data.model.Track
import com.example.data.model.TrackType
import com.example.data.model.TransitionType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPurpleKeyframe
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTrackBg
import com.example.ui.theme.StudioYellowAudio

@Composable
fun MultiTrackTimeline(
    tracks: List<Track>,
    textOverlays: List<TextOverlay>,
    subtitles: List<SubtitleSegment>,
    currentTimeMs: Long,
    totalDurationMs: Long,
    selectedTrackId: String?,
    selectedClipId: String?,
    selectedTextOverlayId: String?,
    zoomFactor: Float,
    onSeek: (Long) -> Unit,
    onSelectClip: (String, String) -> Unit,
    onSelectTextOverlay: (String) -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onZoomChange: (Float) -> Unit,
    formatTimestamp: (Long) -> String,
    onOpenImportMedia: (Track?, Long?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val pxPerMs = 0.08f * zoomFactor

    val timelineWidthDp = (totalDurationMs * pxPerMs).dp.coerceAtLeast(600.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioDarkBg)
            .border(1.dp, StudioBorder)
    ) {
        // Timeline Header Controls (Split button + Import Media + Zoom Controls)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF12151F))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prominent Import Media Button
                Surface(
                    onClick = { onOpenImportMedia(null, currentTimeMs) },
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E2638),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyanAI),
                    modifier = Modifier.testTag("timeline_import_media_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import Media",
                            tint = StudioCyanAI,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+ Import Media",
                            color = StudioCyanAI,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    onClick = onSplitAtPlayhead,
                    shape = RoundedCornerShape(6.dp),
                    color = StudioRedPrimary,
                    modifier = Modifier.testTag("timeline_split_clip_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Split Clip",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Split",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Position: ${formatTimestamp(currentTimeMs)}",
                    color = StudioCyanAI,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Timeline Zoom Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onZoomChange((zoomFactor - 0.25f).coerceAtLeast(0.5f)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = StudioTextSecondary)
                }

                Slider(
                    value = zoomFactor,
                    onValueChange = onZoomChange,
                    valueRange = 0.5f..4.0f,
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("timeline_zoom_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = StudioCyanAI,
                        activeTrackColor = StudioCyanAI,
                        inactiveTrackColor = StudioBorder
                    )
                )

                IconButton(
                    onClick = { onZoomChange((zoomFactor + 0.25f).coerceAtMost(4.0f)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = StudioTextSecondary)
                }
            }
        }

        // Timeline Lanes Scroll Container
        Row(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            // Track Headers Sidebar (Fixed Left)
            Column(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF12151F))
                    .border(1.dp, StudioBorder)
            ) {
                // Header spacer for ruler
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Color(0xFF0F111A))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("TRACKS", color = StudioTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Track Title List
                tracks.forEach { track ->
                    TrackHeaderItem(
                        track = track,
                        onAddMediaToTrack = { trk -> onOpenImportMedia(trk, currentTimeMs) }
                    )
                }

                // Text Overlay Track Header
                TrackHeaderLabel(icon = Icons.Default.Title, label = "Text Overlays", color = StudioPurpleKeyframe)

                // Subtitles Track Header
                TrackHeaderLabel(icon = Icons.Default.Subtitles, label = "AI Subtitles", color = StudioCyanAI)
            }

            // Scrollable Timeline Content (Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(scrollState)
            ) {
                Column(
                    modifier = Modifier
                        .width(timelineWidthDp)
                        .fillMaxHeight()
                ) {
                    // Ruler Header
                    TimelineRuler(
                        totalDurationMs = totalDurationMs,
                        pxPerMs = pxPerMs,
                        onSeek = onSeek,
                        formatTimestamp = formatTimestamp
                    )

                    // Track Lanes
                    tracks.forEach { track ->
                        TrackLaneItem(
                            track = track,
                            pxPerMs = pxPerMs,
                            selectedClipId = selectedClipId,
                            onSelectClip = { clipId -> onSelectClip(track.id, clipId) },
                            onEmptyTrackClick = { clickedMs -> onOpenImportMedia(track, clickedMs) }
                        )
                    }

                    // Text Overlay Track Lane
                    TextOverlayLane(
                        textOverlays = textOverlays,
                        pxPerMs = pxPerMs,
                        selectedTextOverlayId = selectedTextOverlayId,
                        onSelectTextOverlay = onSelectTextOverlay
                    )

                    // Subtitles Track Lane
                    SubtitleLane(
                        subtitles = subtitles,
                        pxPerMs = pxPerMs
                    )
                }

                // Interactive Red Playhead Line
                val playheadOffsetDp = (currentTimeMs * pxPerMs).dp
                Box(
                    modifier = Modifier
                        .offset { IntOffset(playheadOffsetDp.roundToPx(), 0) }
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(StudioRedPrimary)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(StudioRedPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderItem(
    track: Track,
    onAddMediaToTrack: (Track) -> Unit = {}
) {
    val icon = when (track.type) {
        TrackType.VIDEO_PRIMARY -> Icons.Default.Movie
        TrackType.VIDEO_OVERLAY -> Icons.Default.Movie
        TrackType.AUDIO_MUSIC -> Icons.Default.Audiotrack
        TrackType.AUDIO_VOICEOVER -> Icons.Default.Mic
        TrackType.TEXT_OVERLAY -> Icons.Default.Title
    }

    val iconColor = when (track.type) {
        TrackType.VIDEO_PRIMARY -> StudioRedPrimary
        TrackType.VIDEO_OVERLAY -> Color(0xFF3D7EFF)
        TrackType.AUDIO_MUSIC -> StudioYellowAudio
        TrackType.AUDIO_VOICEOVER -> Color(0xFF00E676)
        TrackType.TEXT_OVERLAY -> StudioPurpleKeyframe
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(0xFF161924))
            .border(1.dp, Color(0xFF1E2230))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = icon, contentDescription = track.name, tint = iconColor, modifier = Modifier.size(14.dp))
            Text(
                text = track.name,
                color = StudioTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        IconButton(
            onClick = { onAddMediaToTrack(track) },
            modifier = Modifier
                .size(22.dp)
                .testTag("add_clip_to_track_${track.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add media to ${track.name}",
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TrackHeaderLabel(icon: ImageVector, label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(0xFF161924))
            .border(1.dp, Color(0xFF1E2230))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
        Text(text = label, color = StudioTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TimelineRuler(
    totalDurationMs: Long,
    pxPerMs: Float,
    onSeek: (Long) -> Unit,
    formatTimestamp: (Long) -> String
) {
    val intervalMs = 5000L
    val markCount = (totalDurationMs / intervalMs).toInt() + 1

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(Color(0xFF0F111A))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val clickedTimeMs = (offset.x / pxPerMs).toLong()
                    onSeek(clickedTimeMs)
                }
            }
    ) {
        Row {
            for (i in 0..markCount) {
                val timeMs = i * intervalMs
                val leftDp = (timeMs * pxPerMs).dp
                Box(
                    modifier = Modifier
                        .offset { IntOffset(leftDp.roundToPx(), 0) }
                        .padding(top = 4.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(8.dp)
                                .background(StudioTextMuted)
                        )
                        Text(
                            text = formatTimestamp(timeMs),
                            color = StudioTextMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackLaneItem(
    track: Track,
    pxPerMs: Float,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit,
    onEmptyTrackClick: (Long) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(StudioTrackBg)
            .border(1.dp, Color(0xFF1A1E2B))
            .pointerInput(track.id, pxPerMs) {
                detectTapGestures { offset ->
                    val clickedTimeMs = (offset.x / pxPerMs).toLong()
                    onEmptyTrackClick(clickedTimeMs)
                }
            }
    ) {
        // Video Clips
        track.clips.forEach { clip ->
            val startDp = (clip.startInTimelineMs * pxPerMs).dp
            val widthDp = (clip.durationMs * pxPerMs).dp.coerceAtLeast(24.dp)
            val isSelected = clip.id == selectedClipId

            Box(
                modifier = Modifier
                    .offset { IntOffset(startDp.roundToPx(), 0) }
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) StudioRedPrimary else Color(0xFF2C354D)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) StudioCyanAI else Color(0xFF4A5675),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = clip.title,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (clip.transitionIn != TransitionType.NONE) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(StudioCyanAI)
                                .padding(horizontal = 3.dp, vertical = 1.dp)
                        ) {
                            Text("T", color = StudioDarkBg, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Audio Clips
        track.audioClips.forEach { audio ->
            val startDp = (audio.startInTimelineMs * pxPerMs).dp
            val widthDp = (audio.durationMs * pxPerMs).dp.coerceAtLeast(24.dp)

            Box(
                modifier = Modifier
                    .offset { IntOffset(startDp.roundToPx(), 0) }
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (audio.isVoiceover) Color(0xFF1B5E20) else Color(0xFF827717)
                    )
                    .border(1.dp, StudioYellowAudio, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = audio.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TextOverlayLane(
    textOverlays: List<TextOverlay>,
    pxPerMs: Float,
    selectedTextOverlayId: String?,
    onSelectTextOverlay: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(StudioTrackBg)
            .border(1.dp, Color(0xFF1A1E2B))
    ) {
        textOverlays.forEach { textOverlay ->
            val startDp = (textOverlay.startInTimelineMs * pxPerMs).dp
            val widthDp = (textOverlay.durationMs * pxPerMs).dp.coerceAtLeast(24.dp)
            val isSelected = textOverlay.id == selectedTextOverlayId

            Box(
                modifier = Modifier
                    .offset { IntOffset(startDp.roundToPx(), 0) }
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF4A148C))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) StudioCyanAI else StudioPurpleKeyframe,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelectTextOverlay(textOverlay.id) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = textOverlay.text,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SubtitleLane(
    subtitles: List<SubtitleSegment>,
    pxPerMs: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(StudioTrackBg)
            .border(1.dp, Color(0xFF1A1E2B))
    ) {
        subtitles.forEach { sub ->
            val startDp = (sub.startMs * pxPerMs).dp
            val widthDp = ((sub.endMs - sub.startMs) * pxPerMs).dp.coerceAtLeast(24.dp)

            Box(
                modifier = Modifier
                    .offset { IntOffset(startDp.roundToPx(), 0) }
                    .width(widthDp)
                    .fillMaxHeight()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF006064))
                    .border(1.dp, StudioCyanAI, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = sub.text,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
