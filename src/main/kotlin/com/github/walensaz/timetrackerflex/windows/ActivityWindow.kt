package com.github.walensaz.timetrackerflex.windows

import com.github.walensaz.timetrackerflex.Logging
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.intellij.IntelliJUtils
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.github.walensaz.timetrackerflex.state.persistence.FileBasedPersister
import com.github.walensaz.timetrackerflex.windows.components.ActiveActivityLog
import com.github.walensaz.timetrackerflex.windows.components.RawActivityLog
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.concurrency.AppExecutorUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.util.concurrent.TimeUnit
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel


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
        private val rawActivityLog = RawActivityLog(emptyList()).apply {
            val addFunc: (Component) -> Unit = {
                add(it)
            }
            createButtonsAndAdd(addFunc)
        }
        private val fileChangeActivityWindow = ActiveActivityLog(emptyList(), TimeTrackerEventType.CHANGE_FILE).apply {
            val addFunc: (Component) -> Unit = {
                add(it, BorderLayout.SOUTH)
            }
            createButtonsAndAdd(addFunc)
        }
        private val branchChangeActivityWindow = ActiveActivityLog(emptyList(), TimeTrackerEventType.BRANCH_CHANGE).apply {
            val addFunc: (Component) -> Unit = {
                add(it, BorderLayout.SOUTH)
            }
            createButtonsAndAdd(addFunc)
        }

        private fun createButtonsAndAdd(addFunc: (Component) -> Unit) {
            val updateButton = JButton("Update").apply {
                addActionListener {
                    updateAll()
                }
            }
            val openFolderButton = JButton("Open Data Folder").apply {
                addActionListener {
                    IntelliJUtils.openInExplorer(FileBasedPersister.PLUGIN_DIRECTORY.toString())
                }
            }
            val panel = JPanel(FlowLayout()).apply {
                add(updateButton)
                add(openFolderButton)
            }
            addFunc(panel)
        }

        private val windowUpdaterFuture = AppExecutorUtil
            .getAppScheduledExecutorService()
            .scheduleWithFixedDelay({
                if (toolWindow.isVisible) updateAll()
            }, 5L, 30L, TimeUnit.SECONDS)

        private fun updateAll() {
            invokeLater {
                if (tabbedPane.selectedIndex == 0) update(branchChangeActivityWindow)
                else if (tabbedPane.selectedIndex == 1) update(fileChangeActivityWindow)
                else if (tabbedPane.selectedIndex == 2) {
                    val activities = service.timeTrackerEventHandler.stateHolder.allActivities()
                    rawActivityLog.clearTable()
                    rawActivityLog.addAll(activities)
                }
            }
        }

        private fun update(activeActivityLog: ActiveActivityLog) {
            activeActivityLog.clearTable()
            service.timeTrackerEventHandler.processEvents()
            activeActivityLog.addAll(service.timeTrackerEventHandler.stateHolder.activityLog)
        }

        fun getContent(): JComponent {
            invokeLater {
                tabbedPane.addTab("Branch Activity", branchChangeActivityWindow)
                tabbedPane.addTab("File Activity", fileChangeActivityWindow)
                tabbedPane.addTab("Raw Activity Events", rawActivityLog)
            }
            return tabbedPane
        }
    }
}