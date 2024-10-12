package com.github.walensaz.timetrackerflex.services

import com.github.walensaz.timetrackerflex.Logging
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.factory.TimeTrackerEventFactory
import com.github.walensaz.timetrackerflex.intellij.IntelliJUtils
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class BranchMonitorService(val project: Project) : Logging {

    private val timeTrackerService = service<TimeTracker>()

    init {
        logInfo("Started branch monitoring service")
        scheduleBranchCheck()
    }

    private fun scheduleBranchCheck() {
        ApplicationManager.getApplication().executeOnPooledThread {
            if (IntelliJUtils.isGitEnabled(project)) {
                val branch = IntelliJUtils.getGitBranchOrEmpty(project)
                if (branch.isNotEmpty()) {
                    timeTrackerService.timeTrackerEventHandler.handleEvent(
                        TimeTrackerEventFactory.create(
                            TimeTrackerEventType.BRANCH_CHANGE,
                            project,
                            branch
                        ))
                }
            }
            // Schedule the next check
            scheduleNextCheck()
        }
    }

    private fun scheduleNextCheck() {
        ApplicationManager.getApplication().executeOnPooledThread {
            TimeUnit.SECONDS.sleep(5)
            scheduleBranchCheck()
        }
    }
}
