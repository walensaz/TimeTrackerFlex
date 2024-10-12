package com.github.walensaz.timetrackerflex.intellij

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepositoryManager

object IntelliJUtils {

    fun isVersionControlEnabled(project: Project): Boolean {
        val vcsManager = ProjectLevelVcsManager.getInstance(project)
        // Check if there is any active VCS
        return vcsManager.allActiveVcss.isNotEmpty()
    }

    fun isGitEnabled(project: Project): Boolean {
        val gitVcs = ProjectLevelVcsManager.getInstance(project).allActiveVcss.find {vcs -> vcs.name == "Git"}
        return gitVcs != null
    }

    fun getGitBranchOrEmpty(project: Project): String {
        return if (isGitEnabled(project))
            GitRepositoryManager.getInstance(project).repositories.firstOrNull()?.currentBranch?.name ?: ""
        else
            ""
    }

    fun openInExplorer(folderPath: String) {
        ApplicationManager.getApplication().invokeLater {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(folderPath)
            if (virtualFile != null && virtualFile.isDirectory) {
                // Use the file chooser to open the directory in the system file explorer
                FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), null, null, virtualFile)
            } else {
                println("The specified folder does not exist or is not a directory: $folderPath")
            }
        }
    }

    fun getMostLikelyActiveProject(): Project? {
        return ProjectManager.getInstance().openProjects.firstOrNull { project ->
            FileEditorManager.getInstance(project).selectedEditor != null
        } ?: ProjectManager.getInstance().openProjects.firstOrNull()
    }
}