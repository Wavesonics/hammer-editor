package com.darkrockstudios.apps.hammer.common.data.projectrepository

import com.akuleshov7.ktoml.Toml
import com.darkrockstudios.apps.hammer.common.data.ProjectDef
import com.darkrockstudios.apps.hammer.common.data.projectrepository.projecteditorrepository.ProjectEditorRepository
import com.darkrockstudios.apps.hammer.common.data.projectrepository.projecteditorrepository.ProjectEditorRepositoryOkio
import com.darkrockstudios.apps.hammer.common.data.projectsrepository.ProjectsRepository
import okio.FileSystem

class ProjectRepositoryOkio(
    private val fileSystem: FileSystem,
    projectsRepository: ProjectsRepository,
    private val toml: Toml,
    private val projectEditorFactory: ProjectEditorFactory = Factory(),
) : ProjectRepository(projectsRepository) {

    override fun createEditor(projectDef: ProjectDef): ProjectEditorRepository {
        return projectEditorFactory.createEditor(projectDef, projectsRepository, fileSystem, toml)
    }

    companion object {
        private class Factory : ProjectEditorFactory {
            override fun createEditor(
                projectDef: ProjectDef,
                projectsRepository: ProjectsRepository,
                fileSystem: FileSystem,
                toml: Toml
            ): ProjectEditorRepository {
                return ProjectEditorRepositoryOkio(projectDef, projectsRepository, fileSystem, toml)
            }
        }
    }
}

interface ProjectEditorFactory {
    fun createEditor(
        projectDef: ProjectDef,
        projectsRepository: ProjectsRepository,
        fileSystem: FileSystem,
        toml: Toml
    ): ProjectEditorRepository
}