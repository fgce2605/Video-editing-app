package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiEngine
import com.example.data.db.AppDatabase
import com.example.data.db.ProjectRepository
import com.example.data.model.AspectRatioPreset
import com.example.data.model.AudioClip
import com.example.data.model.Clip
import com.example.data.model.ColorGrading
import com.example.data.model.CropConfig
import com.example.data.model.ExportBitrate
import com.example.data.model.ExportConfig
import com.example.data.model.ExportFormat
import com.example.data.model.ExportJob
import com.example.data.model.ExportResolution
import com.example.data.model.FilterPreset
import com.example.data.model.Keyframe
import com.example.data.model.ProjectEntity
import com.example.data.model.SubtitleSegment
import com.example.data.model.TextOverlay
import com.example.data.model.Track
import com.example.data.model.TrackType
import com.example.data.model.TransitionType
import com.example.data.engine.SampleMediaLibrary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

enum class EditorTab(val label: String) {
    EDIT("Trim & Split"),
    TRANSITIONS("Transitions"),
    COLOR("Color Grading"),
    AUDIO("Audio Studio"),
    TEXT("Text & Titles"),
    AI("AI Tools"),
    KEYFRAMES("Keyframes"),
    EXPORT("Export")
}

data class EditorStateSnapshot(
    val tracks: List<Track>,
    val textOverlays: List<TextOverlay>,
    val subtitles: List<SubtitleSegment>
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository by lazy {
        ProjectRepository(AppDatabase.getDatabase(application).projectDao())
    }

    // UI States
    private val _projectName = MutableStateFlow("My Master Cut")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatioPreset.RATIO_16_9)
    val aspectRatio: StateFlow<AspectRatioPreset> = _aspectRatio.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(25000L)
    val totalDurationMs: StateFlow<Long> = _totalDurationMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _zoomFactor = MutableStateFlow(1.0f) // 0.5x to 4.0x
    val zoomFactor: StateFlow<Float> = _zoomFactor.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(SampleMediaLibrary.createInitialProjectTracks())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _textOverlays = MutableStateFlow<List<TextOverlay>>(SampleMediaLibrary.initialTextOverlays)
    val textOverlays: StateFlow<List<TextOverlay>> = _textOverlays.asStateFlow()

    private val _subtitles = MutableStateFlow<List<SubtitleSegment>>(SampleMediaLibrary.initialSubtitles)
    val subtitles: StateFlow<List<SubtitleSegment>> = _subtitles.asStateFlow()

    private val _selectedTrackId = MutableStateFlow<String?>("track_v1")
    val selectedTrackId: StateFlow<String?> = _selectedTrackId.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>("clip_1")
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _selectedTextOverlayId = MutableStateFlow<String?>("text_1")
    val selectedTextOverlayId: StateFlow<String?> = _selectedTextOverlayId.asStateFlow()

    private val _activeTab = MutableStateFlow(EditorTab.EDIT)
    val activeTab: StateFlow<EditorTab> = _activeTab.asStateFlow()

    private val _exportConfig = MutableStateFlow(ExportConfig())
    val exportConfig: StateFlow<ExportConfig> = _exportConfig.asStateFlow()

    private val _activeExportJob = MutableStateFlow<ExportJob?>(null)
    val activeExportJob: StateFlow<ExportJob?> = _activeExportJob.asStateFlow()

    private val _exportQueue = MutableStateFlow<List<ExportJob>>(emptyList())
    val exportQueue: StateFlow<List<ExportJob>> = _exportQueue.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _isImportingMedia = MutableStateFlow(false)
    val isImportingMedia: StateFlow<Boolean> = _isImportingMedia.asStateFlow()

    private val _importProgressMessage = MutableStateFlow<String?>("Processing media...")
    val importProgressMessage: StateFlow<String?> = _importProgressMessage.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Undo / Redo
    private val undoStack = mutableListOf<EditorStateSnapshot>()
    private val redoStack = mutableListOf<EditorStateSnapshot>()

    private var playbackJob: Job? = null
    private var exportJobWorker: Job? = null

    init {
        recalculateTotalDuration()
    }

    private fun pushUndoState() {
        undoStack.add(
            EditorStateSnapshot(
                tracks = _tracks.value,
                textOverlays = _textOverlays.value,
                subtitles = _subtitles.value
            )
        )
        if (undoStack.size > 25) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentState = EditorStateSnapshot(
                tracks = _tracks.value,
                textOverlays = _textOverlays.value,
                subtitles = _subtitles.value
            )
            redoStack.add(currentState)

            val previousState = undoStack.removeAt(undoStack.lastIndex)
            _tracks.value = previousState.tracks
            _textOverlays.value = previousState.textOverlays
            _subtitles.value = previousState.subtitles
            recalculateTotalDuration()
            showStatus("Undo performed")
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentState = EditorStateSnapshot(
                tracks = _tracks.value,
                textOverlays = _textOverlays.value,
                subtitles = _subtitles.value
            )
            undoStack.add(currentState)

            val nextState = redoStack.removeAt(redoStack.lastIndex)
            _tracks.value = nextState.tracks
            _textOverlays.value = nextState.textOverlays
            _subtitles.value = nextState.subtitles
            recalculateTotalDuration()
            showStatus("Redo performed")
        }
    }

    fun setProjectName(name: String) {
        _projectName.value = name
    }

    fun setAspectRatio(ratio: AspectRatioPreset) {
        _aspectRatio.value = ratio
        showStatus("Canvas set to ${ratio.displayName}")
    }

    fun setActiveTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun setZoomFactor(factor: Float) {
        _zoomFactor.value = factor.coerceIn(0.5f, 4.0f)
    }

    // Playback controls
    fun togglePlay() {
        if (_isPlaying.value) pause() else play()
    }

    fun play() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(33L) // ~30 fps
                var nextTime = _currentTimeMs.value + 33L
                if (nextTime >= _totalDurationMs.value) {
                    nextTime = 0L
                    _isPlaying.value = false
                }
                _currentTimeMs.value = nextTime
            }
        }
    }

    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun seekTo(timeMs: Long) {
        _currentTimeMs.value = timeMs.coerceIn(0L, _totalDurationMs.value)
    }

    fun stepFrameForward() {
        pause()
        seekTo(_currentTimeMs.value + 33L)
    }

    fun stepFrameBackward() {
        pause()
        seekTo(_currentTimeMs.value - 33L)
    }

    // Clip Selection & Editing
    fun selectClip(trackId: String, clipId: String) {
        _selectedTrackId.value = trackId
        _selectedClipId.value = clipId
        _selectedTextOverlayId.value = null
    }

    fun selectTextOverlay(id: String) {
        _selectedTextOverlayId.value = id
        _selectedClipId.value = null
    }

    fun deselectAll() {
        _selectedClipId.value = null
        _selectedTextOverlayId.value = null
    }

    fun getSelectedClip(): Pair<Track, Clip>? {
        val tId = _selectedTrackId.value ?: return null
        val cId = _selectedClipId.value ?: return null
        val track = _tracks.value.find { it.id == tId } ?: return null
        val clip = track.clips.find { it.id == cId } ?: return null
        return Pair(track, clip)
    }

    fun splitClipAtCurrentTime() {
        val selected = getSelectedClip() ?: run {
            showStatus("Select a clip to split")
            return
        }
        val (track, clip) = selected
        val current = _currentTimeMs.value

        if (current <= clip.startInTimelineMs || current >= clip.startInTimelineMs + clip.durationMs) {
            showStatus("Position scrubber inside clip to split")
            return
        }

        pushUndoState()

        val splitOffset = current - clip.startInTimelineMs
        val clip1Duration = splitOffset
        val clip2Duration = clip.durationMs - splitOffset

        val updatedClip1 = clip.copy(
            durationMs = clip1Duration,
            sourceTrimEndMs = clip.sourceTrimStartMs + clip1Duration
        )

        val updatedClip2 = clip.copy(
            id = UUID.randomUUID().toString(),
            title = "${clip.title} (Part 2)",
            startInTimelineMs = current,
            durationMs = clip2Duration,
            sourceTrimStartMs = clip.sourceTrimStartMs + clip1Duration,
            sourceTrimEndMs = clip.sourceTrimEndMs
        )

        val newClips = mutableListOf<Clip>()
        for (c in track.clips) {
            if (c.id == clip.id) {
                newClips.add(updatedClip1)
                newClips.add(updatedClip2)
            } else {
                newClips.add(c)
            }
        }

        updateTrackClips(track.id, newClips)
        _selectedClipId.value = updatedClip2.id
        showStatus("Clip split into two segments")
    }

    fun deleteSelectedClip() {
        val selected = getSelectedClip() ?: return
        pushUndoState()
        val (track, clip) = selected
        val newClips = track.clips.filter { it.id != clip.id }
        updateTrackClips(track.id, newClips)
        _selectedClipId.value = null
        recalculateTotalDuration()
        showStatus("Clip removed")
    }

    fun setClipSpeed(clipId: String, speed: Float) {
        val selected = getSelectedClip() ?: return
        if (selected.second.id == clipId) {
            pushUndoState()
            val updatedClip = selected.second.copy(speed = speed)
            updateClipInTrack(selected.first.id, updatedClip)
            showStatus("Clip speed set to ${speed}x")
        }
    }

    fun toggleClipReverse(clipId: String) {
        val selected = getSelectedClip() ?: return
        if (selected.second.id == clipId) {
            pushUndoState()
            val updatedClip = selected.second.copy(isReversed = !selected.second.isReversed)
            updateClipInTrack(selected.first.id, updatedClip)
            showStatus(if (updatedClip.isReversed) "Clip reversed" else "Normal playback")
        }
    }

    fun setClipTransition(clipId: String, transition: TransitionType) {
        val selected = getSelectedClip() ?: return
        if (selected.second.id == clipId) {
            pushUndoState()
            val updatedClip = selected.second.copy(transitionIn = transition)
            updateClipInTrack(selected.first.id, updatedClip)
            showStatus("Transition set: ${transition.displayName}")
        }
    }

    fun updateColorGrading(colorGrading: ColorGrading) {
        val selected = getSelectedClip() ?: return
        val updatedClip = selected.second.copy(colorGrading = colorGrading)
        updateClipInTrack(selected.first.id, updatedClip)
    }

    fun applyFilterPreset(preset: FilterPreset) {
        val selected = getSelectedClip() ?: return
        val currentGrading = selected.second.colorGrading
        val updatedGrading = currentGrading.copy(filterPreset = preset)
        pushUndoState()
        updateColorGrading(updatedGrading)
        showStatus("Filter applied: ${preset.displayName}")
    }

    fun updateCropConfig(cropConfig: CropConfig) {
        val selected = getSelectedClip() ?: return
        val updatedClip = selected.second.copy(cropConfig = cropConfig)
        updateClipInTrack(selected.first.id, updatedClip)
    }

    fun addKeyframeToSelectedClip() {
        val selected = getSelectedClip() ?: return
        pushUndoState()
        val clip = selected.second
        val currentMs = _currentTimeMs.value
        val newKeyframe = Keyframe(
            id = UUID.randomUUID().toString(),
            timestampMs = currentMs,
            posX = clip.cropConfig.offsetX,
            posY = clip.cropConfig.offsetY,
            scale = clip.cropConfig.scale,
            rotationDeg = clip.cropConfig.rotationDeg,
            opacity = 1.0f
        )
        val updatedKeyframes = (clip.keyframes + newKeyframe).sortedBy { it.timestampMs }
        val updatedClip = clip.copy(keyframes = updatedKeyframes)
        updateClipInTrack(selected.first.id, updatedClip)
        showStatus("Keyframe added at ${formatTimestamp(currentMs)}")
    }

    // Text Overlay Management
    fun addTextOverlay(text: String) {
        pushUndoState()
        val newOverlay = TextOverlay(
            id = UUID.randomUUID().toString(),
            text = text,
            startInTimelineMs = _currentTimeMs.value,
            durationMs = 4000L
        )
        _textOverlays.value = _textOverlays.value + newOverlay
        _selectedTextOverlayId.value = newOverlay.id
        showStatus("Text overlay added")
    }

    fun updateTextOverlay(overlay: TextOverlay) {
        _textOverlays.value = _textOverlays.value.map {
            if (it.id == overlay.id) overlay else it
        }
    }

    fun deleteTextOverlay(id: String) {
        pushUndoState()
        _textOverlays.value = _textOverlays.value.filter { it.id != id }
        _selectedTextOverlayId.value = null
        showStatus("Text overlay deleted")
    }

    // Audio Track Management
    fun addBackgroundMusic(bgmTitle: String) {
        pushUndoState()
        val bgmTrack = _tracks.value.find { it.type == TrackType.AUDIO_MUSIC } ?: return
        val newAudio = AudioClip(
            id = UUID.randomUUID().toString(),
            title = bgmTitle,
            uri = bgmTitle.lowercase().replace(" ", "_"),
            startInTimelineMs = 0L,
            durationMs = _totalDurationMs.value,
            volume = 0.8f,
            fadeInMs = 1000L,
            fadeOutMs = 1000L
        )
        val updatedTracks = _tracks.value.map {
            if (it.id == bgmTrack.id) it.copy(audioClips = it.audioClips + newAudio) else it
        }
        _tracks.value = updatedTracks
        showStatus("Audio track added: $bgmTitle")
    }

    fun recordVoiceover(durationMs: Long = 5000L) {
        viewModelScope.launch {
            showStatus("Recording voiceover...")
            delay(1500)
            pushUndoState()
            val voTrack = _tracks.value.find { it.type == TrackType.AUDIO_VOICEOVER } ?: return@launch
            val newVo = AudioClip(
                id = UUID.randomUUID().toString(),
                title = "Live Mic Voiceover",
                uri = "voiceover_mic",
                startInTimelineMs = _currentTimeMs.value,
                durationMs = durationMs,
                volume = 1.0f,
                isVoiceover = true
            )
            val updatedTracks = _tracks.value.map {
                if (it.id == voTrack.id) it.copy(audioClips = it.audioClips + newVo) else it
            }
            _tracks.value = updatedTracks
            showStatus("Voiceover recorded and placed on timeline")
        }
    }

    // AI Features
    fun triggerAiSubtitles() {
        viewModelScope.launch {
            _isAiProcessing.value = true
            showStatus("AI generating subtitles from audio...")
            val newSubtitles = GeminiAiEngine.generateSubtitlesFromTopicOrAudio(_projectName.value)
            pushUndoState()
            _subtitles.value = newSubtitles
            _isAiProcessing.value = false
            showStatus("Generated ${newSubtitles.size} AI subtitle captions!")
        }
    }

    fun triggerAiAutoColor() {
        val selected = getSelectedClip() ?: run {
            showStatus("Select a clip for AI Color Correction")
            return
        }
        viewModelScope.launch {
            _isAiProcessing.value = true
            showStatus("AI analyzing clip frames for color balance...")
            val autoGrading = GeminiAiEngine.analyzeAutoColorGrading(selected.second.title)
            pushUndoState()
            updateColorGrading(autoGrading)
            _isAiProcessing.value = false
            showStatus("AI Auto Color enhancement applied!")
        }
    }

    fun triggerAiSceneSplit() {
        viewModelScope.launch {
            _isAiProcessing.value = true
            showStatus("AI detecting scene changes...")
            val splitPoints = GeminiAiEngine.detectScenesAndSplitPoints(_totalDurationMs.value)
            _isAiProcessing.value = false
            showStatus("AI detected ${splitPoints.size} scene transitions at ${splitPoints.map { formatTimestamp(it) }}")
        }
    }

    fun triggerAiTtsVoiceover(script: String) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            showStatus("AI generating Text-to-Speech voiceover...")
            val generatedScript = GeminiAiEngine.generateTextToSpeechScript(script)
            recordVoiceover(8000L)
            _isAiProcessing.value = false
            showStatus("AI Voiceover track created: '$generatedScript'")
        }
    }

    fun triggerAiHighlightReel() {
        viewModelScope.launch {
            _isAiProcessing.value = true
            showStatus("AI analyzing top action moments for Reel...")
            delay(1500)
            _isAiProcessing.value = false
            showStatus("AI Highlight Reel compiled (15s viral cut ready)")
        }
    }

    // Export Engine
    fun updateExportConfig(config: ExportConfig) {
        _exportConfig.value = config
    }

    fun startExport() {
        if (_activeExportJob.value != null) return

        val newJob = ExportJob(
            id = UUID.randomUUID().toString(),
            projectName = _projectName.value,
            config = _exportConfig.value,
            status = "Rendering"
        )
        _activeExportJob.value = newJob

        exportJobWorker?.cancel()
        exportJobWorker = viewModelScope.launch {
            val totalFrames = 300
            for (frame in 1..totalFrames) {
                delay(20L) // Fast render loop simulation
                val progress = (frame * 100) / totalFrames
                val remainingSec = ((totalFrames - frame) * 0.02f).toInt()
                _activeExportJob.value = _activeExportJob.value?.copy(
                    progressPercent = progress,
                    currentFrame = frame,
                    totalFrames = totalFrames,
                    estimatedTimeRemainingSec = remainingSec
                )
            }
            val completedJob = _activeExportJob.value?.copy(
                progressPercent = 100,
                status = "Completed",
                outputUri = "content://media/external/video/${System.currentTimeMillis()}"
            )
            if (completedJob != null) {
                _exportQueue.value = _exportQueue.value + completedJob
            }
            _activeExportJob.value = completedJob
            showStatus("Export completed successfully!")
        }
    }

    fun cancelExport() {
        exportJobWorker?.cancel()
        _activeExportJob.value = null
        showStatus("Export cancelled")
    }

    // Project Persistence
    fun saveProject() {
        viewModelScope.launch {
            val projectJson = JSONObject().apply {
                put("name", _projectName.value)
                put("aspectRatio", _aspectRatio.value.name)
                put("durationMs", _totalDurationMs.value)
            }.toString()

            val entity = ProjectEntity(
                id = UUID.randomUUID().toString(),
                name = _projectName.value,
                aspectRatio = _aspectRatio.value.name,
                durationMs = _totalDurationMs.value,
                updatedAt = System.currentTimeMillis(),
                projectJson = projectJson
            )
            repository.saveProject(entity)
            showStatus("Project saved locally to Room DB")
        }
    }

    // Media Import Engine
    fun importMediaFromUris(
        context: android.content.Context,
        uris: List<android.net.Uri>,
        targetTrackType: TrackType = TrackType.VIDEO_PRIMARY,
        targetTrackId: String? = null,
        atTimeMs: Long? = null
    ) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImportingMedia.value = true
            _importProgressMessage.value = "Importing ${uris.size} file(s)..."
            pushUndoState()

            var insertedCount = 0
            var insertionTime = atTimeMs ?: _currentTimeMs.value

            for (uri in uris) {
                _importProgressMessage.value = "Copying media file into project directory..."
                val result = com.example.util.MediaImportHelper.processImportedUri(context, uri)
                val isAudio = result.isAudio || targetTrackType == TrackType.AUDIO_MUSIC || targetTrackType == TrackType.AUDIO_VOICEOVER

                if (isAudio) {
                    val targetTrack = _tracks.value.find {
                        (targetTrackId != null && it.id == targetTrackId) ||
                        (targetTrackType == TrackType.AUDIO_VOICEOVER && it.type == TrackType.AUDIO_VOICEOVER) ||
                        (it.type == TrackType.AUDIO_MUSIC)
                    } ?: _tracks.value.find { it.type == TrackType.AUDIO_MUSIC || it.type == TrackType.AUDIO_VOICEOVER }

                    if (targetTrack != null) {
                        val newAudio = AudioClip(
                            id = UUID.randomUUID().toString(),
                            title = result.title,
                            uri = result.localFilePath,
                            startInTimelineMs = insertionTime,
                            durationMs = result.durationMs,
                            volume = 0.9f,
                            isVoiceover = (targetTrack.type == TrackType.AUDIO_VOICEOVER)
                        )
                        _tracks.value = _tracks.value.map {
                            if (it.id == targetTrack.id) it.copy(audioClips = it.audioClips + newAudio) else it
                        }
                        insertionTime += result.durationMs
                        insertedCount++
                    }
                } else {
                    val targetTrack = _tracks.value.find {
                        (targetTrackId != null && it.id == targetTrackId) ||
                        (it.type == targetTrackType) ||
                        (it.type == TrackType.VIDEO_PRIMARY)
                    } ?: _tracks.value.find { it.type == TrackType.VIDEO_PRIMARY || it.type == TrackType.VIDEO_OVERLAY }

                    if (targetTrack != null) {
                        val newClip = Clip(
                            id = UUID.randomUUID().toString(),
                            mediaId = result.localFilePath,
                            title = result.title,
                            startInTimelineMs = insertionTime,
                            durationMs = result.durationMs,
                            sourceTrimStartMs = 0L,
                            sourceTrimEndMs = result.durationMs
                        )
                        _tracks.value = _tracks.value.map {
                            if (it.id == targetTrack.id) it.copy(clips = it.clips + newClip) else it
                        }
                        _selectedTrackId.value = targetTrack.id
                        _selectedClipId.value = newClip.id
                        insertionTime += result.durationMs
                        insertedCount++
                    }
                }
            }

            recalculateTotalDuration()
            _isImportingMedia.value = false
            showStatus("Imported $insertedCount clip(s) to timeline")
        }
    }

    // Helper utilities
    private fun updateTrackClips(trackId: String, newClips: List<Clip>) {
        _tracks.value = _tracks.value.map {
            if (it.id == trackId) it.copy(clips = newClips) else it
        }
        recalculateTotalDuration()
    }

    private fun updateClipInTrack(trackId: String, updatedClip: Clip) {
        val track = _tracks.value.find { it.id == trackId } ?: return
        val newClips = track.clips.map { if (it.id == updatedClip.id) updatedClip else it }
        updateTrackClips(trackId, newClips)
    }

    private fun recalculateTotalDuration() {
        var maxMs = 15000L
        for (t in _tracks.value) {
            for (c in t.clips) {
                val endMs = c.startInTimelineMs + c.durationMs
                if (endMs > maxMs) maxMs = endMs
            }
            for (a in t.audioClips) {
                val endMs = a.startInTimelineMs + a.durationMs
                if (endMs > maxMs) maxMs = endMs
            }
        }
        _totalDurationMs.value = maxMs
    }

    private fun showStatus(msg: String) {
        viewModelScope.launch {
            _statusMessage.value = msg
            delay(3000)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    fun formatTimestamp(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val frames = ((ms % 1000) / 33).toInt()
        return String.format("%02d:%02d:%02d", min, sec, frames)
    }
}
