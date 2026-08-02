package com.example.ui.dialogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.TrackType
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioYellowAudio
import com.example.util.MediaImportHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMediaSheet(
    targetTrackName: String = "Video Track 1",
    targetTrackType: TrackType = TrackType.VIDEO_PRIMARY,
    targetTrackId: String? = null,
    targetTimeMs: Long? = null,
    onDismiss: () -> Unit,
    onImportSelected: (context: Context, uris: List<Uri>, trackType: TrackType, trackId: String?, atTimeMs: Long?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            pendingAction?.invoke()
            pendingAction = null
        } else {
            Toast.makeText(context, "Permission required to access media or camera", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndRun(permissions: Array<String>, action: () -> Unit) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            action()
        } else {
            pendingAction = action
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    // Launchers for media picking
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportSelected(context, uris, targetTrackType, targetTrackId, targetTimeMs)
            onDismiss()
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val audioTrackType = if (targetTrackType == TrackType.AUDIO_VOICEOVER) TrackType.AUDIO_VOICEOVER else TrackType.AUDIO_MUSIC
            onImportSelected(context, uris, audioTrackType, targetTrackId, targetTimeMs)
            onDismiss()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportSelected(context, uris, targetTrackType, targetTrackId, targetTimeMs)
            onDismiss()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && pendingCameraUri != null) {
            onImportSelected(context, listOf(pendingCameraUri!!), targetTrackType, targetTrackId, targetTimeMs)
            onDismiss()
        }
    }

    val mediaPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val cameraPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF131722),
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(StudioBorder, CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Import Media",
                        color = StudioTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Destination: $targetTrackName",
                        color = StudioCyanAI,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_import_sheet_btn")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = StudioTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Gallery / Photos (Videos)
                ImportOptionTile(
                    icon = Icons.Default.PhotoLibrary,
                    iconColor = StudioRedPrimary,
                    title = "Gallery / Photos",
                    subtitle = "Pick video clips or photos using Android visual picker",
                    testTag = "import_option_gallery",
                    onClick = {
                        checkAndRun(mediaPermissions) {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    }
                )

                // 2. Camera (Record New Video)
                ImportOptionTile(
                    icon = Icons.Default.Videocam,
                    iconColor = Color(0xFFFF9100),
                    title = "Camera (Record Video)",
                    subtitle = "Record a new live video clip directly into project",
                    testTag = "import_option_camera",
                    onClick = {
                        checkAndRun(cameraPermissions) {
                            try {
                                val (uri, _) = MediaImportHelper.createCameraVideoUri(context)
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error launching camera: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                // 3. Audio & Music Tracks
                ImportOptionTile(
                    icon = Icons.Default.LibraryMusic,
                    iconColor = StudioYellowAudio,
                    title = "Audio Files / Soundtracks",
                    subtitle = "Choose background music, sound effects, or voiceovers",
                    testTag = "import_option_audio",
                    onClick = {
                        checkAndRun(mediaPermissions) {
                            audioLauncher.launch("audio/*")
                        }
                    }
                )

                // 4. Files App (Documents / Storage)
                ImportOptionTile(
                    icon = Icons.Default.Folder,
                    iconColor = StudioCyanAI,
                    title = "Browse Files App",
                    subtitle = "Import from device storage, Downloads, or Drive",
                    testTag = "import_option_files",
                    onClick = {
                        checkAndRun(mediaPermissions) {
                            fileLauncher.launch(arrayOf("video/*", "audio/*", "image/*"))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImportOptionTile(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = StudioCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, iconColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = StudioTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = StudioTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ImportingLoadingDialog(
    message: String = "Processing media..."
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131722),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            shadowElevation = 8.dp,
            modifier = Modifier
                .width(280.dp)
                .testTag("import_loading_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = StudioRedPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Importing Media",
                    color = StudioTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message,
                    color = StudioTextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
