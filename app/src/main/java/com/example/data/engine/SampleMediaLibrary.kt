package com.example.data.engine

import com.example.data.model.AudioClip
import com.example.data.model.Clip
import com.example.data.model.ColorGrading
import com.example.data.model.CropConfig
import com.example.data.model.FilterPreset
import com.example.data.model.MediaItem
import com.example.data.model.SubtitleSegment
import com.example.data.model.TextOverlay
import com.example.data.model.Track
import com.example.data.model.TrackType
import com.example.data.model.TransitionType

object SampleMediaLibrary {

    val sampleMediaItems = listOf(
        MediaItem(
            id = "media_1",
            name = "Cyberpunk Cityscape 4K",
            durationMs = 12000L,
            type = "VIDEO",
            thumbnailUrl = null,
            resolution = "4K",
            colorAccentHex = "#3D7EFF"
        ),
        MediaItem(
            id = "media_2",
            name = "Neon Sunset Drive",
            durationMs = 10000L,
            type = "VIDEO",
            thumbnailUrl = null,
            resolution = "1080p",
            colorAccentHex = "#FF5252"
        ),
        MediaItem(
            id = "media_3",
            name = "Alpine Mountain Drone",
            durationMs = 15000L,
            type = "VIDEO",
            thumbnailUrl = null,
            resolution = "4K",
            colorAccentHex = "#00E5FF"
        ),
        MediaItem(
            id = "media_4",
            name = "Golden Hour Ocean Waves",
            durationMs = 8000L,
            type = "VIDEO",
            thumbnailUrl = null,
            resolution = "1080p",
            colorAccentHex = "#FFD700"
        ),
        MediaItem(
            id = "audio_bgm_1",
            name = "Upbeat Energetic Synth.mp3",
            durationMs = 30000L,
            type = "AUDIO",
            colorAccentHex = "#FFD700"
        ),
        MediaItem(
            id = "audio_bgm_2",
            name = "Lo-Fi Midnight Chill.mp3",
            durationMs = 25000L,
            type = "AUDIO",
            colorAccentHex = "#B388FF"
        ),
        MediaItem(
            id = "audio_bgm_3",
            name = "Cinematic Trailer Build.mp3",
            durationMs = 20000L,
            type = "AUDIO",
            colorAccentHex = "#00E676"
        ),
        MediaItem(
            id = "watermark_logo",
            name = "ProEdit Studio Watermark",
            durationMs = 30000L,
            type = "IMAGE",
            colorAccentHex = "#00E5FF"
        )
    )

    fun createInitialProjectTracks(): List<Track> {
        val clip1 = Clip(
            id = "clip_1",
            mediaId = "media_1",
            title = "Cyberpunk Cityscape",
            startInTimelineMs = 0L,
            durationMs = 8000L,
            sourceTrimStartMs = 0L,
            sourceTrimEndMs = 8000L,
            colorGrading = ColorGrading(
                brightness = 5f,
                contrast = 15f,
                saturation = 20f,
                filterPreset = FilterPreset.CYBERPUNK
            ),
            transitionIn = TransitionType.FADE
        )

        val clip2 = Clip(
            id = "clip_2",
            mediaId = "media_2",
            title = "Neon Sunset Drive",
            startInTimelineMs = 8000L,
            durationMs = 7000L,
            sourceTrimStartMs = 0L,
            sourceTrimEndMs = 7000L,
            colorGrading = ColorGrading(
                brightness = 10f,
                warmth = 25f,
                filterPreset = FilterPreset.GOLDEN_HOUR
            ),
            transitionIn = TransitionType.DISSOLVE
        )

        val clip3 = Clip(
            id = "clip_3",
            mediaId = "media_3",
            title = "Alpine Mountain Drone",
            startInTimelineMs = 15000L,
            durationMs = 10000L,
            sourceTrimStartMs = 0L,
            sourceTrimEndMs = 10000L,
            colorGrading = ColorGrading(
                contrast = 10f,
                filterPreset = FilterPreset.CINEMATIC
            ),
            transitionIn = TransitionType.ZOOM
        )

        val bgmClip = AudioClip(
            id = "audio_clip_1",
            title = "Upbeat Energetic Synth",
            uri = "sample_bgm_energetic",
            startInTimelineMs = 0L,
            durationMs = 25000L,
            volume = 0.7f,
            fadeInMs = 1000L,
            fadeOutMs = 2000L
        )

        val voiceoverClip = AudioClip(
            id = "audio_clip_2",
            title = "AI Voiceover Narration",
            uri = "sample_voiceover",
            startInTimelineMs = 2000L,
            durationMs = 12000L,
            volume = 1.0f,
            isVoiceover = true
        )

        return listOf(
            Track(
                id = "track_v1",
                name = "Video Track 1",
                type = TrackType.VIDEO_PRIMARY,
                clips = listOf(clip1, clip2, clip3)
            ),
            Track(
                id = "track_v2",
                name = "Video Overlay / B-Roll",
                type = TrackType.VIDEO_OVERLAY,
                clips = emptyList()
            ),
            Track(
                id = "track_audio_bgm",
                name = "Background Music",
                type = TrackType.AUDIO_MUSIC,
                audioClips = listOf(bgmClip)
            ),
            Track(
                id = "track_audio_vo",
                name = "Voiceover Track",
                type = TrackType.AUDIO_VOICEOVER,
                audioClips = listOf(voiceoverClip)
            )
        )
    }

    val initialTextOverlays = listOf(
        TextOverlay(
            id = "text_1",
            text = "PROEDIT STUDIO",
            fontStyle = "Display",
            colorHex = "#00E5FF",
            outlineHex = "#0B0D14",
            bgColorHex = "#88000000",
            startInTimelineMs = 500L,
            durationMs = 4500L,
            posX = 0.5f,
            posY = 0.35f,
            scale = 1.3f,
            entryAnimation = "Typewriter"
        ),
        TextOverlay(
            id = "text_2",
            text = "AI Multi-Track Video Editor",
            fontStyle = "SansSerif",
            colorHex = "#FF5252",
            outlineHex = "#000000",
            bgColorHex = "#44000000",
            startInTimelineMs = 1500L,
            durationMs = 3500L,
            posX = 0.5f,
            posY = 0.48f,
            scale = 1.0f,
            entryAnimation = "Fade In"
        )
    )

    val initialSubtitles = listOf(
        SubtitleSegment("sub_1", "Welcome to ProEdit Studio video editor.", 500L, 3500L),
        SubtitleSegment("sub_2", "Edit multiple video and audio tracks seamlessly.", 4000L, 8500L),
        SubtitleSegment("sub_3", "Apply AI color grading and keyframe animations.", 9000L, 14000L),
        SubtitleSegment("sub_4", "Export in high resolution up to 4K 60fps.", 14500L, 20000L)
    )
}
