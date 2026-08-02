package com.example.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Clip
import com.example.data.model.ColorGrading
import com.example.data.model.CropConfig
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportConfig
import com.example.data.model.ExportFormat
import com.example.data.model.ExportResolution
import com.example.data.model.FilterPreset
import com.example.data.model.Track
import com.example.data.model.TransitionType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioPurpleKeyframe
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.EditorTab

@Composable
fun EditingPanelContainer(
    activeTab: EditorTab,
    onTabSelected: (EditorTab) -> Unit,
    selectedClipPair: Pair<Track, Clip>?,
    exportConfig: ExportConfig,
    onUpdateExportConfig: (ExportConfig) -> Unit,
    onStartExport: () -> Unit,
    onSplitAtPlayhead: () -> Unit,
    onDeleteSelectedClip: () -> Unit,
    onSetClipSpeed: (Float) -> Unit,
    onToggleClipReverse: () -> Unit,
    onSetTransition: (TransitionType) -> Unit,
    onUpdateColorGrading: (ColorGrading) -> Unit,
    onApplyFilterPreset: (FilterPreset) -> Unit,
    onUpdateCropConfig: (CropConfig) -> Unit,
    onAddTextOverlay: (String) -> Unit,
    onAddKeyframe: () -> Unit,
    onAddBackgroundMusic: (String) -> Unit,
    onRecordVoiceover: () -> Unit,
    onTriggerAiSubtitles: () -> Unit,
    onTriggerAiAutoColor: () -> Unit,
    onTriggerAiSceneSplit: () -> Unit,
    onTriggerAiTtsVoiceover: (String) -> Unit,
    onTriggerAiHighlightReel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioCardBg)
            .border(1.dp, StudioBorder)
    ) {
        // Tab Bar Row
        ScrollableTabRow(
            selectedTabIndex = activeTab.ordinal,
            edgePadding = 8.dp,
            containerColor = Color(0xFF121520),
            contentColor = StudioTextPrimary,
            divider = {}
        ) {
            EditorTab.entries.forEach { tab ->
                val isSelected = activeTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.testTag("editor_tab_${tab.name.lowercase()}"),
                    text = {
                        Text(
                            text = tab.label,
                            color = if (isSelected) StudioRedPrimary else StudioTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Active Tab Panel Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(12.dp)
        ) {
            when (activeTab) {
                EditorTab.EDIT -> EditTabPanel(
                    selectedClip = selectedClipPair?.second,
                    onSplit = onSplitAtPlayhead,
                    onDelete = onDeleteSelectedClip,
                    onSpeedChange = onSetClipSpeed,
                    onToggleReverse = onToggleClipReverse,
                    onUpdateCrop = onUpdateCropConfig
                )
                EditorTab.TRANSITIONS -> TransitionsTabPanel(
                    selectedClip = selectedClipPair?.second,
                    onSetTransition = onSetTransition
                )
                EditorTab.COLOR -> ColorGradingTabPanel(
                    selectedClip = selectedClipPair?.second,
                    onUpdateGrading = onUpdateColorGrading,
                    onApplyPreset = onApplyFilterPreset
                )
                EditorTab.AUDIO -> AudioStudioTabPanel(
                    onAddBgm = onAddBackgroundMusic,
                    onRecordVo = onRecordVoiceover
                )
                EditorTab.TEXT -> TextOverlayTabPanel(
                    onAddText = onAddTextOverlay
                )
                EditorTab.AI -> AiToolsTabPanel(
                    onTriggerSubtitles = onTriggerAiSubtitles,
                    onTriggerAutoColor = onTriggerAiAutoColor,
                    onTriggerSceneSplit = onTriggerAiSceneSplit,
                    onTriggerTts = onTriggerAiTtsVoiceover,
                    onTriggerHighlight = onTriggerAiHighlightReel
                )
                EditorTab.KEYFRAMES -> KeyframesTabPanel(
                    selectedClip = selectedClipPair?.second,
                    onAddKeyframe = onAddKeyframe
                )
                EditorTab.EXPORT -> ExportTabPanel(
                    config = exportConfig,
                    onUpdateConfig = onUpdateExportConfig,
                    onStartExport = onStartExport
                )
            }
        }
    }
}

@Composable
private fun EditTabPanel(
    selectedClip: Clip?,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onToggleReverse: () -> Unit,
    onUpdateCrop: (CropConfig) -> Unit
) {
    if (selectedClip == null) {
        NoClipSelectedNotice("Select a clip on the timeline to trim, split, or scale.")
        return
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Action Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSplit,
                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("panel_split_btn")
            ) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Split Clip", fontSize = 12.sp)
            }

            Button(
                onClick = onDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("panel_delete_btn")
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", fontSize = 12.sp)
            }

            Button(
                onClick = onToggleReverse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedClip.isReversed) StudioCyanAI else Color(0xFF2C3246)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (selectedClip.isReversed) "Reversed" else "Reverse",
                    color = if (selectedClip.isReversed) StudioDarkBg else StudioTextPrimary,
                    fontSize = 12.sp
                )
            }
        }

        // Speed Control
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speed Multiplier: ${selectedClip.speed}x", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f, 4.0f).forEach { speed ->
                        Surface(
                            onClick = { onSpeedChange(speed) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (selectedClip.speed == speed) StudioRedPrimary else Color(0xFF202534)
                        ) {
                            Text(
                                text = "${speed}x",
                                color = StudioTextPrimary,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Slider(
                value = selectedClip.speed,
                onValueChange = onSpeedChange,
                valueRange = 0.25f..4.0f,
                colors = SliderDefaults.colors(
                    thumbColor = StudioRedPrimary,
                    activeTrackColor = StudioRedPrimary,
                    inactiveTrackColor = StudioBorder
                )
            )
        }
    }
}

@Composable
private fun TransitionsTabPanel(
    selectedClip: Clip?,
    onSetTransition: (TransitionType) -> Unit
) {
    if (selectedClip == null) {
        NoClipSelectedNotice("Select a clip on the timeline to apply a transition.")
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("In-Transition Effect:", color = StudioTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TransitionType.entries) { transition ->
                val isSelected = selectedClip.transitionIn == transition
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) StudioRedPrimary else Color(0xFF202534)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp)
                        .clickable { onSetTransition(transition) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = transition.displayName,
                            tint = if (isSelected) Color.White else StudioCyanAI,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = transition.displayName,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorGradingTabPanel(
    selectedClip: Clip?,
    onUpdateGrading: (ColorGrading) -> Unit,
    onApplyPreset: (FilterPreset) -> Unit
) {
    if (selectedClip == null) {
        NoClipSelectedNotice("Select a clip to apply color grading & filter presets.")
        return
    }

    val grading = selectedClip.colorGrading
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Filter Presets Cards
        Text("Aesthetic Filter Presets", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(FilterPreset.entries) { preset ->
                val isSelected = grading.filterPreset == preset
                Surface(
                    onClick = { onApplyPreset(preset) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) StudioRedPrimary else Color(0xFF202534),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                ) {
                    Text(
                        text = preset.displayName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Sliders
        GradingSlider("Brightness", grading.brightness, -50f..50f) {
            onUpdateGrading(grading.copy(brightness = it))
        }
        GradingSlider("Contrast", grading.contrast, -50f..50f) {
            onUpdateGrading(grading.copy(contrast = it))
        }
        GradingSlider("Saturation", grading.saturation, -50f..50f) {
            onUpdateGrading(grading.copy(saturation = it))
        }
    }
}

@Composable
private fun GradingSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("$label: ${value.toInt()}", color = StudioTextSecondary, fontSize = 11.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.width(200.dp),
            colors = SliderDefaults.colors(
                thumbColor = StudioCyanAI,
                activeTrackColor = StudioCyanAI,
                inactiveTrackColor = StudioBorder
            )
        )
    }
}

@Composable
private fun AudioStudioTabPanel(
    onAddBgm: (String) -> Unit,
    onRecordVo: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Background Music & Voiceover Studio", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Button(
                onClick = onRecordVo,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("record_voiceover_btn")
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Mic", fontSize = 11.sp)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val stockTracks = listOf("Upbeat Energetic Synth", "Lo-Fi Midnight Chill", "Cinematic Trailer Build", "Acoustic Sunburst")
            items(stockTracks) { trackName ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF202534)),
                    modifier = Modifier.clickable { onAddBgm(trackName) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Audiotrack, contentDescription = null, tint = StudioCyanAI, modifier = Modifier.size(16.dp))
                        Text(text = trackName, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TextOverlayTabPanel(
    onAddText: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Add Custom Title / Text Overlay", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Enter title text...", color = StudioTextMuted) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("text_overlay_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StudioCyanAI,
                    unfocusedBorderColor = StudioBorder,
                    focusedTextColor = StudioTextPrimary
                )
            )

            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onAddText(textInput)
                        textInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("add_text_overlay_btn")
            ) {
                Text("Add Text", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AiToolsTabPanel(
    onTriggerSubtitles: () -> Unit,
    onTriggerAutoColor: () -> Unit,
    onTriggerSceneSplit: () -> Unit,
    onTriggerTts: (String) -> Unit,
    onTriggerHighlight: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AiActionButton("Auto Subtitles", Icons.Default.AutoAwesome, onTriggerSubtitles, "ai_subtitles_btn")
            AiActionButton("AI Auto Color", Icons.Default.ColorLens, onTriggerAutoColor, "ai_auto_color_btn")
            AiActionButton("Scene Split", Icons.Default.MovieFilter, onTriggerSceneSplit, "ai_scene_split_btn")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AiActionButton("AI Voice TTS", Icons.Default.Mic, { onTriggerTts("ProEdit Studio AI Voice") }, "ai_tts_btn")
            AiActionButton("Smart Highlight Reel", Icons.Default.Style, onTriggerHighlight, "ai_highlight_reel_btn")
        }
    }
}

@Composable
private fun AiActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1E2638),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCyanAI),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = StudioCyanAI, modifier = Modifier.size(14.dp))
            Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun KeyframesTabPanel(
    selectedClip: Clip?,
    onAddKeyframe: () -> Unit
) {
    if (selectedClip == null) {
        NoClipSelectedNotice("Select a clip on timeline to insert keyframe animation markers.")
        return
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Keyframe Markers (${selectedClip.keyframes.size} active)", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Button(
                onClick = onAddKeyframe,
                colors = ButtonDefaults.buttonColors(containerColor = StudioPurpleKeyframe),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("add_keyframe_btn")
            ) {
                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Keyframe", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ExportTabPanel(
    config: ExportConfig,
    onUpdateConfig: (ExportConfig) -> Unit,
    onStartExport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Export & Render Configurations", color = StudioTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Button(
                onClick = onStartExport,
                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("start_export_render_btn")
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Resolution Selection
            Column(modifier = Modifier.weight(1f)) {
                Text("Resolution", color = StudioTextMuted, fontSize = 10.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ExportResolution.entries) { res ->
                        val isSelected = config.resolution == res
                        Surface(
                            onClick = { onUpdateConfig(config.copy(resolution = res)) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) StudioRedPrimary else Color(0xFF202534)
                        ) {
                            Text(text = res.label, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }

            // Format Selection
            Column(modifier = Modifier.weight(1f)) {
                Text("Format", color = StudioTextMuted, fontSize = 10.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(ExportFormat.entries) { fmt ->
                        val isSelected = config.format == fmt
                        Surface(
                            onClick = { onUpdateConfig(config.copy(format = fmt)) },
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) StudioCyanAI else Color(0xFF202534)
                        ) {
                            Text(
                                text = fmt.name,
                                color = if (isSelected) StudioDarkBg else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoClipSelectedNotice(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = StudioTextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
