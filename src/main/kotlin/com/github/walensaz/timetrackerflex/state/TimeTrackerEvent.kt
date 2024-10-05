package com.github.walensaz.timetrackerflex.state

import com.github.walensaz.timetrackerflex.intellij.IntelliJUtils
import com.intellij.openapi.project.Project

enum class TimeTrackerEventType {
    CHANGE_FILE,
    TYPING
}

interface TimeTrackerEvent {
    val type: TimeTrackerEventType
    val timestamp: Long
    val project: Project
    val fileName: String

    fun canBeCombined(activity: Activity): Boolean {
        return activity.projectName == this.project.name &&
                activity.gitBranch == IntelliJUtils.getGitBranchOrEmpty(project) &&
                activity.fileName == this.fileName
    }
}

class TimeTrackerEventImpl(override val type: TimeTrackerEventType,
                           override val project: Project,
                           override val fileName: String
) : TimeTrackerEvent {
    override val timestamp = System.currentTimeMillis()
}