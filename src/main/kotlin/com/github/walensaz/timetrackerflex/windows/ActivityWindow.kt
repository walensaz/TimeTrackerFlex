package com.github.walensaz.timetrackerflex.windows

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.windows.components.ActivityTable
import com.intellij.openapi.application.invokeLater
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.TimeUnit
import javax.swing.JComponent


class ActivityWindow : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val tabbedPane = JBTabbedPane()
        private val service = service<TimeTracker>()
        private val activityTable = ActivityTable(emptyList())

        private val windowUpdaterFuture = AppExecutorUtil
            .getAppScheduledExecutorService()
            .scheduleWithFixedDelay({
                if (toolWindow.isVisible) addAll(tabbedPane)
            }, 10L, 20L, TimeUnit.SECONDS)

        private fun addAll(pane: JBTabbedPane) = invokeLater {
            val activities = service.timeTrackerEventHandler.stateHolder.allActivities()
//            println("Updating window with ${activities.size} activities.")
            activityTable.clearTable()
            activityTable.addAll(activities)
        }

        fun getContent(): JComponent {
            tabbedPane.addTab("Activity", activityTable)
            return tabbedPane
        }
    }
}