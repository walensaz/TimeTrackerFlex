package com.github.walensaz.timetrackerflex.intellij

import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.factory.TimeTrackerEventFactory
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener

class MyFileEditorListener : FileEditorManagerListener {

    private val timeTrackerService = service<TimeTracker>()

    override fun selectionChanged(event: FileEditorManagerEvent) {
        timeTrackerService.timeTrackerEventHandler.handleEvent(
            TimeTrackerEventFactory.create(
                TimeTrackerEventType.CHANGE_FILE,
                event.manager.project,
                event.newFile?.name ?: ""
            ))
        super.selectionChanged(event)
    }
}