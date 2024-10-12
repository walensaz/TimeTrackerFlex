package com.github.walensaz.timetrackerflex.intellij

import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.factory.TimeTrackerEventFactory
import com.github.walensaz.timetrackerflex.services.BranchMonitorService
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit

class ProjectStartupActivity : ProjectActivity {

    private val timeTrackerService = service<TimeTracker>()

    override suspend fun execute(project: Project) {
        AppExecutorUtil
            .getAppScheduledExecutorService()
            .schedule({
                timeTrackerService.timeTrackerEventHandler.handleEvent(
                    TimeTrackerEventFactory.create(
                        TimeTrackerEventType.BRANCH_CHANGE,
                        project,
                        IntelliJUtils.getGitBranchOrEmpty(project)
                    )
                )
                project.service<BranchMonitorService>()
            }, 5L, TimeUnit.SECONDS)
    }
}