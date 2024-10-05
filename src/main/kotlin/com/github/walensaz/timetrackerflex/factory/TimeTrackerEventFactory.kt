package com.github.walensaz.timetrackerflex.factory

import com.github.walensaz.timetrackerflex.state.TimeTrackerEvent
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventImpl
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.openapi.project.Project

object TimeTrackerEventFactory {

    fun create(type: TimeTrackerEventType,
               project: Project,
               fileName: String): TimeTrackerEvent {
        return TimeTrackerEventImpl(type, project, fileName)
    }

}