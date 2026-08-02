package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.db.AppDatabase
import com.example.data.db.ProjectRepository
import com.example.data.model.AspectRatioPreset
import com.example.ui.MainEditorScreen
import com.example.ui.ProjectListScreen
import com.example.ui.theme.ProEditStudioTheme
import com.example.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = ProjectRepository(AppDatabase.getDatabase(applicationContext).projectDao())

        setContent {
            ProEditStudioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val savedProjects by repository.allProjects.collectAsState(initial = emptyList())
                    var currentScreen by remember { mutableStateOf("EDITOR") } // Default straight to EDITOR for fast studio access!

                    if (currentScreen == "PROJECTS") {
                        ProjectListScreen(
                            savedProjects = savedProjects,
                            onOpenProject = { project ->
                                viewModel.setProjectName(project.name)
                                currentScreen = "EDITOR"
                            },
                            onCreateNewProject = { name, ratio ->
                                viewModel.setProjectName(name)
                                viewModel.setAspectRatio(ratio)
                                currentScreen = "EDITOR"
                            }
                        )
                    } else {
                        MainEditorScreen(
                            viewModel = viewModel,
                            onBackToProjects = {
                                currentScreen = "PROJECTS"
                            }
                        )
                    }
                }
            }
        }
    }
}

