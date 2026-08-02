package com.example.data.db

import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProject(id: String): ProjectEntity? {
        return projectDao.getProjectById(id)
    }

    suspend fun saveProject(project: ProjectEntity) {
        projectDao.insertOrUpdateProject(project)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }
}
