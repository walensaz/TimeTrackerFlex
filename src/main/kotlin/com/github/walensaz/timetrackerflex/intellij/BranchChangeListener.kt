package com.github.walensaz.timetrackerflex.intellij

import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.factory.TimeTrackerEventFactory
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.openapi.command.impl.DummyProject
import com.intellij.openapi.components.service
import com.intellij.openapi.vcs.BranchChangeListener

class GitBranchChangeListener: BranchChangeListener {

    private val timeTrackerService = service<TimeTracker>()

    // old branch probably?
    override fun branchWillChange(branch: String) {}

    override fun branchHasChanged(branch: String) {
        timeTrackerService.timeTrackerEventHandler.handleEvent(
            TimeTrackerEventFactory.create(
                TimeTrackerEventType.BRANCH_CHANGE,
                IntelliJUtils.getMostLikelyActiveProject() ?: DummyProject.getInstance(),
                branch
            ))
    }
}
