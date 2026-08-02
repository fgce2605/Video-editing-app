package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AspectRatioPreset(val displayName: String, val ratioWidth: Int, val ratioHeight: Int) {
    RATIO_16_9("16:9 Landscape", 16, 9),
    RATIO_9_16("9:16 Portrait / Reel", 9, 16),
    RATIO_1_1("1:1 Square", 1, 1),
    RATIO_4_5("4:5 Post", 4, 5)
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    FADE("Cross Fade"),
    DISSOLVE("Dissolve"),
    WIPE("Wipe Right"),
    SLIDE("Slide Left"),
    ZOOM("Zoom In"),
    BLUR_FLASH("Blur Flash")
}

enum class FilterPreset(val displayName: String) {
    NONE("Original"),
    CINEMATIC("Teal & Orange"),
    VINTAGE("Vintage Film"),
    NOIR("Noir B&W"),
    CYBERPUNK("Cyberpunk Neon"),
    GOLDEN_HOUR("Golden Hour"),
    MOODY("Moody Dark"),
    VIVID("Vivid Pop")
}

data class ColorGrading(
    val brightness: Float = 0f,   // -100..100
    val contrast: Float = 0f,     // -100..100
    val saturation: Float = 0f,   // -100..100
    val exposure: Float = 0f,     // -100..100
    val warmth: Float = 0f,       // -100..100
    val vignette: Float = 0f,     // 0..100
    val filterPreset: FilterPreset = FilterPreset.NONE
)

data class CropConfig(
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)

data class Keyframe(
    val id: String,
    val timestampMs: Long,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1.0f
)

data class TextOverlay(
    val id: String,
    val text: String,
    val fontStyle: String = "SansSerif", // SansSerif, Serif, Monospace, Display, Cursive
    val colorHex: String = "#FFFFFF",
    val outlineHex: String = "#000000",
    val bgColorHex: String = "#CC000000",
    val startInTimelineMs: Long,
    val durationMs: Long,
    val posX: Float = 0.5f, // Normalized 0..1
    val posY: Float = 0.8f, // Normalized 0..1
    val scale: Float = 1.0f,
    val rotationDeg: Float = 0f,
    val entryAnimation: String = "Fade In" // Fade In, Typewriter, Pop, Slide Up
)

data class SubtitleSegment(
    val id: String,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val speaker: String = "Speaker 1"
)

data class Clip(
    val id: String,
    val mediaId: String,
    val title: String,
    val startInTimelineMs: Long,
    val durationMs: Long,
    val sourceTrimStartMs: Long = 0L,
    val sourceTrimEndMs: Long = durationMs,
    val speed: Float = 1.0f,
    val isReversed: Boolean = false,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val transitionIn: TransitionType = TransitionType.NONE,
    val colorGrading: ColorGrading = ColorGrading(),
    val cropConfig: CropConfig = CropConfig(),
    val keyframes: List<Keyframe> = emptyList()
)

data class AudioClip(
    val id: String,
    val title: String,
    val uri: String,
    val startInTimelineMs: Long,
    val durationMs: Long,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val isVoiceover: Boolean = false
)

enum class TrackType {
    VIDEO_PRIMARY,
    VIDEO_OVERLAY,
    TEXT_OVERLAY,
    AUDIO_MUSIC,
    AUDIO_VOICEOVER
}

data class Track(
    val id: String,
    val name: String,
    val type: TrackType,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val clips: List<Clip> = emptyList(),
    val audioClips: List<AudioClip> = emptyList()
)

data class MediaItem(
    val id: String,
    val name: String,
    val durationMs: Long,
    val type: String, // VIDEO, AUDIO, IMAGE
    val thumbnailUrl: String? = null,
    val resolution: String = "1080p",
    val colorAccentHex: String = "#3D7EFF"
)

enum class ExportResolution(val label: String, val width: Int, val height: Int) {
    RES_480P("480p SD", 854, 480),
    RES_720P("720p HD", 1280, 720),
    RES_1080P("1080p Full HD", 1920, 1080),
    RES_2K("2K QHD", 2560, 1440),
    RES_4K("4K Ultra HD", 3840, 2160)
}

enum class ExportBitrate(val label: String, val mbps: Float) {
    LOW("Low (Fast)", 4.0f),
    MEDIUM("Medium (Balanced)", 8.0f),
    HIGH("High (Lossless)", 16.0f),
    CUSTOM("Custom", 12.0f)
}

enum class ExportFormat(val extension: String, val mime: String) {
    MP4("mp4", "video/mp4"),
    WEBM("webm", "video/webm"),
    MOV("mov", "video/quicktime"),
    GIF("gif", "image/gif")
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val bitrate: ExportBitrate = ExportBitrate.MEDIUM,
    val format: ExportFormat = ExportFormat.MP4,
    val fps: Int = 30,
    val customMbps: Float = 10.0f
)

data class ExportJob(
    val id: String,
    val projectName: String,
    val config: ExportConfig,
    val progressPercent: Int = 0,
    val currentFrame: Int = 0,
    val totalFrames: Int = 300,
    val estimatedTimeRemainingSec: Int = 15,
    val status: String = "Queued", // Queued, Rendering, Completed, Failed
    val outputUri: String? = null
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val aspectRatio: String = "RATIO_16_9",
    val durationMs: Long = 15000L,
    val fps: Int = 30,
    val updatedAt: Long = System.currentTimeMillis(),
    val projectJson: String // Serialized project state
)
