package com.github.walensaz.timetrackerflex.windows

import com.github.walensaz.timetrackerflex.Logging
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.windows.components.ActiveActivityLog
import com.github.walensaz.timetrackerflex.windows.components.RawActivityLog
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JComponent


class ActivityWindow : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = ActivityWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class ActivityWindow(toolWindow: ToolWindow) : Logging {

        private val tabbedPane = JBTabbedPane()
        private val service = ApplicationManager.getApplication().getService(TimeTracker::class.java)
        private val rawActivityLog = RawActivityLog(emptyList())
        private val activeActivityWindow = ActiveActivityLog(emptyList())
        private val updateButton = JButton("Update").apply {
            addActionListener {
                updateAll()
            }
        }

        private val windowUpdaterFuture = AppExecutorUtil
            .getAppScheduledExecutorService()
            .scheduleWithFixedDelay({
                if (toolWindow.isVisible) updateAll()
            }, 5L, 30L, TimeUnit.SECONDS)

        private fun updateAll() {
            invokeLater {
                if (tabbedPane.selectedIndex == 0) {
                    activeActivityWindow.clearTable()
                    activeActivityWindow.addAll(service.timeTrackerEventHandler.stateHolder.activityLog)
                } else if (tabbedPane.selectedIndex == 1) {
                    val activities = service.timeTrackerEventHandler.stateHolder.allActivities()
                    rawActivityLog.clearTable()
                    rawActivityLog.addAll(activities)
                }
            }
        }

        fun getContent(): JComponent {
            invokeLater {
                rawActivityLog.add(updateButton, BorderLayout.SOUTH)
                activeActivityWindow.add(updateButton, BorderLayout.SOUTH)
                tabbedPane.addTab("Activity", activeActivityWindow)
                tabbedPane.addTab("Raw Activity Events", rawActivityLog)
            }
            return tabbedPane
        }
    }
}