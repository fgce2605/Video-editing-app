package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioPreset
import com.example.data.model.Track
import com.example.data.model.TrackType
import com.example.ui.dialogs.ExportProgressDialog
import com.example.ui.dialogs.ImportMediaSheet
import com.example.ui.dialogs.ImportingLoadingDialog
import com.example.ui.panels.EditingPanelContainer
import com.example.ui.preview.VideoPreviewCanvas
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.timeline.MultiTrackTimeline
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel

@Composable
fun MainEditorScreen(
    viewModel: EditorViewModel,
    onBackToProjects: () -> Unit
) {
    val projectName by viewModel.projectName.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val currentTimeMs by viewModel.currentTimeMs.collectAsState()
    val totalDurationMs by viewModel.totalDurationMs.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val zoomFactor by viewModel.zoomFactor.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val textOverlays by viewModel.textOverlays.collectAsState()
    val subtitles by viewModel.subtitles.collectAsState()
    val selectedTrackId by viewModel.selectedTrackId.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val selectedTextOverlayId by viewModel.selectedTextOverlayId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val exportConfig by viewModel.exportConfig.collectAsState()
    val activeExportJob by viewModel.activeExportJob.collectAsState()
    val exportQueue by viewModel.exportQueue.collectAsState()
    val isAiProcessing by viewModel.isAiProcessing.collectAsState()
    val isImportingMedia by viewModel.isImportingMedia.collectAsState()
    val importProgressMessage by viewModel.importProgressMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showAspectDropdown by remember { mutableStateOf(false) }
    var showExportProgressDialog by remember { mutableStateOf(false) }

    var showImportSheet by remember { mutableStateOf(false) }
    var activeTargetTrack by remember { mutableStateOf<Track?>(null) }
    var activeTargetTimeMs by remember { mutableStateOf<Long?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(activeExportJob) {
        if (activeExportJob != null) {
            showExportProgressDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StudioDarkBg,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    activeTargetTrack = null
                    activeTargetTimeMs = currentTimeMs
                    showImportSheet = true
                },
                containerColor = StudioRedPrimary,
                contentColor = Color.White,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Import Media") },
                text = { Text("Import Media", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_import_media_btn")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Navigation & Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFF0F111A))
                    .border(1.dp, StudioBorder)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back button & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onBackToProjects,
                        modifier = Modifier.testTag("back_to_projects_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Projects", tint = StudioTextPrimary)
                    }

                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = StudioRedPrimary, modifier = Modifier.size(20.dp))
                    Text(
                        text = projectName,
                        color = StudioTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Middle: Aspect Ratio Dropdown Picker
                Box {
                    Surface(
                        onClick = { showAspectDropdown = true },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B1E2B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                        modifier = Modifier.testTag("aspect_ratio_picker_btn")
                    ) {
                        Text(
                            text = aspectRatio.displayName,
                            color = StudioCyanAI,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showAspectDropdown,
                        onDismissRequest = { showAspectDropdown = false }
                    ) {
                        AspectRatioPreset.entries.forEach { ratio ->
                            DropdownMenuItem(
                                text = { Text(ratio.displayName, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.setAspectRatio(ratio)
                                    showAspectDropdown = false
                                }
                            )
                        }
                    }
                }

                // Right: Undo, Redo, Save, Export Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.testTag("undo_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = "Undo", tint = StudioTextPrimary)
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier.testTag("redo_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Redo, contentDescription = "Redo", tint = StudioTextPrimary)
                    }

                    IconButton(
                        onClick = { viewModel.saveProject() },
                        modifier = Modifier.testTag("save_project_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = StudioCyanAI)
                    }

                    Button(
                        onClick = {
                            viewModel.setActiveTab(EditorTab.EXPORT)
                            viewModel.startExport()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("export_header_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Video Preview Canvas Area
            VideoPreviewCanvas(
                aspectRatioPreset = aspectRatio,
                currentTimeMs = currentTimeMs,
                totalDurationMs = totalDurationMs,
                isPlaying = isPlaying,
                tracks = tracks,
                textOverlays = textOverlays,
                subtitles = subtitles,
                selectedTextOverlayId = selectedTextOverlayId,
                onTogglePlay = { viewModel.togglePlay() },
                onSeek = { viewModel.seekTo(it) },
                onStepForward = { viewModel.stepFrameForward() },
                onStepBackward = { viewModel.stepFrameBackward() },
                formatTimestamp = { viewModel.formatTimestamp(it) },
                modifier = Modifier.weight(1f)
            )

            // Editing Control Tab Panel
            EditingPanelContainer(
                activeTab = activeTab,
                onTabSelected = { viewModel.setActiveTab(it) },
                selectedClipPair = viewModel.getSelectedClip(),
                exportConfig = exportConfig,
                onUpdateExportConfig = { viewModel.updateExportConfig(it) },
                onStartExport = { viewModel.startExport() },
                onSplitAtPlayhead = { viewModel.splitClipAtCurrentTime() },
                onDeleteSelectedClip = { viewModel.deleteSelectedClip() },
                onSetClipSpeed = { viewModel.setClipSpeed(selectedClipId ?: "", it) },
                onToggleClipReverse = { viewModel.toggleClipReverse(selectedClipId ?: "") },
                onSetTransition = { viewModel.setClipTransition(selectedClipId ?: "", it) },
                onUpdateColorGrading = { viewModel.updateColorGrading(it) },
                onApplyFilterPreset = { viewModel.applyFilterPreset(it) },
                onUpdateCropConfig = { viewModel.updateCropConfig(it) },
                onAddTextOverlay = { viewModel.addTextOverlay(it) },
                onAddKeyframe = { viewModel.addKeyframeToSelectedClip() },
                onAddBackgroundMusic = { viewModel.addBackgroundMusic(it) },
                onRecordVoiceover = { viewModel.recordVoiceover() },
                onTriggerAiSubtitles = { viewModel.triggerAiSubtitles() },
                onTriggerAiAutoColor = { viewModel.triggerAiAutoColor() },
                onTriggerAiSceneSplit = { viewModel.triggerAiSceneSplit() },
                onTriggerAiTtsVoiceover = { viewModel.triggerAiTtsVoiceover(it) },
                onTriggerAiHighlightReel = { viewModel.triggerAiHighlightReel() }
            )

            // Interactive Multi-Track Timeline
            MultiTrackTimeline(
                tracks = tracks,
                textOverlays = textOverlays,
                subtitles = subtitles,
                currentTimeMs = currentTimeMs,
                totalDurationMs = totalDurationMs,
                selectedTrackId = selectedTrackId,
                selectedClipId = selectedClipId,
                selectedTextOverlayId = selectedTextOverlayId,
                zoomFactor = zoomFactor,
                onSeek = { viewModel.seekTo(it) },
                onSelectClip = { tId, cId -> viewModel.selectClip(tId, cId) },
                onSelectTextOverlay = { viewModel.selectTextOverlay(it) },
                onSplitAtPlayhead = { viewModel.splitClipAtCurrentTime() },
                onZoomChange = { viewModel.setZoomFactor(it) },
                formatTimestamp = { viewModel.formatTimestamp(it) },
                onOpenImportMedia = { track, timeMs ->
                    activeTargetTrack = track
                    activeTargetTimeMs = timeMs
                    showImportSheet = true
                }
            )
        }

        // Import Media Bottom Sheet
        if (showImportSheet) {
            ImportMediaSheet(
                targetTrackName = activeTargetTrack?.name ?: "Video Track 1",
                targetTrackType = activeTargetTrack?.type ?: TrackType.VIDEO_PRIMARY,
                targetTrackId = activeTargetTrack?.id,
                targetTimeMs = activeTargetTimeMs ?: currentTimeMs,
                onDismiss = { showImportSheet = false },
                onImportSelected = { context, uris, trackType, trackId, atTimeMs ->
                    viewModel.importMediaFromUris(
                        context = context,
                        uris = uris,
                        targetTrackType = trackType,
                        targetTrackId = trackId,
                        atTimeMs = atTimeMs
                    )
                }
            )
        }

        // Importing Progress Loading Dialog
        if (isImportingMedia) {
            ImportingLoadingDialog(
                message = importProgressMessage ?: "Processing media..."
            )
        }

        // Active Export Dialog
        if (showExportProgressDialog && activeExportJob != null) {
            ExportProgressDialog(
                exportJob = activeExportJob!!,
                exportQueue = exportQueue,
                onCancel = { viewModel.cancelExport() },
                onDismiss = { showExportProgressDialog = false }
            )
        }
    }
}
