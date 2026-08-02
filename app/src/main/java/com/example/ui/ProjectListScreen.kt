package com.example.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AspectRatioPreset
import com.example.data.model.ProjectEntity
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCyanAI
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioRedPrimary
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun ProjectListScreen(
    savedProjects: List<ProjectEntity>,
    onOpenProject: (ProjectEntity) -> Unit,
    onCreateNewProject: (String, AspectRatioPreset) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("New Studio Project") }
    var selectedRatio by remember { mutableStateOf(AspectRatioPreset.RATIO_16_9) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "ProEdit Studio",
                        tint = StudioRedPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text("ProEdit Studio", color = StudioTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Multi-Track Video & Audio Editor", color = StudioTextSecondary, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_new_project_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text("Your Projects", color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)

            if (savedProjects.isEmpty()) {
                // Empty State
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { showCreateDialog = true },
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null, tint = StudioCyanAI, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No saved projects yet", color = StudioTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Tap 'New Project' to start editing multi-track videos", color = StudioTextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedProjects) { project ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProject(project) },
                            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StudioRedPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = Color.White)
                                    }

                                    Column {
                                        Text(project.name, color = StudioTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("Ratio: ${project.aspectRatio} • ${project.durationMs / 1000}s", color = StudioTextMuted, fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = { onOpenProject(project) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StudioCyanAI),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Open", color = StudioDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showCreateDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                    modifier = Modifier.fillMaxWidth().border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Create New Video Project", color = StudioTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = newProjectName,
                            onValueChange = { newProjectName = it },
                            label = { Text("Project Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StudioCyanAI,
                                unfocusedBorderColor = StudioBorder,
                                focusedTextColor = StudioTextPrimary
                            )
                        )

                        Text("Select Canvas Ratio", color = StudioTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AspectRatioPreset.entries.forEach { ratio ->
                                val isSelected = selectedRatio == ratio
                                Surface(
                                    onClick = { selectedRatio = ratio },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) StudioRedPrimary else StudioDarkBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                                ) {
                                    Text(
                                        text = ratio.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    onCreateNewProject(newProjectName, selectedRatio)
                                    showCreateDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudioRedPrimary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Create & Launch Editor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
