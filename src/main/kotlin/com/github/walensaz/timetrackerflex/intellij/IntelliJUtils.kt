package com.github.walensaz.timetrackerflex.intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.ProjectLevelVcsManager
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
}