package com.github.walensaz.timetrackerflex.windows.components

import com.github.walensaz.timetrackerflex.state.*
import com.github.walensaz.timetrackerflex.windows.components.renderer.FormattedDateRenderer
import com.github.walensaz.timetrackerflex.windows.components.renderer.FormattedDurationCellRenderer
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.containers.headTailOrNull
import com.intellij.util.containers.toArray
import java.awt.BorderLayout
import java.time.*
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter

class ActiveActivityLog(activities: List<Activity>) : JBPanel<ActiveActivityLog>(BorderLayout()) {
    private val columnNames = arrayOf("Project", "Branch", "File", "Active Time", "Inactive Time", "Date")

    // The model to store and manage table data
    private val tableModel = object : DefaultTableModel(mapActivitiesToData(activities), columnNames) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return false
        }

        override fun getValueAt(row: Int, column: Int): Any {
            return when (column) {
                else -> super.getValueAt(row, column)
            }
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return when (columnIndex) {
                3 -> Duration::class.java
                4 -> Duration::class.java
                5 -> LocalDateTime::class.java
                else -> String::class.java
            }
        }

    }

    private val table: JBTable = JBTable(tableModel)

    init {
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.fillsViewportHeight = true
        table.rowSorter = TableRowSorter(tableModel)
        table.columnModel.getColumn(3).setCellRenderer(FormattedDurationCellRenderer)
        table.columnModel.getColumn(4).setCellRenderer(FormattedDurationCellRenderer)
        table.columnModel.getColumn(5).setCellRenderer(FormattedDateRenderer)
        val scrollPane = JBScrollPane(table)
        add(scrollPane, BorderLayout.CENTER)
    }

    // Helper method to convert the list of activities to a table-compatible data array
    private fun mapActivitiesToData(activities: List<Activity>): Array<Array<Any>> {
        return activities.map {
            toRow(it, System.currentTimeMillis())
        }.toArray(emptyArray())
    }

    // Add an activity to the table
//    fun addActivity(activity: Activity) {
//        tableModel.addRow(arrayOf(activity.name, activity.duration, activity.isCompleted))
//    }

    fun clearTable() {
        tableModel.setNumRows(0)
    }

    fun addAll(activityLog: Map<TimeTrackerEventType, Map<ProjectName, Map<BranchName,
            Map<FileName, List<Activity>>>>>) {
        val changeFileEvents = activityLog.getOrDefault(TimeTrackerEventType.CHANGE_FILE, emptyMap())
        val allFilesChanges = changeFileEvents.values.flatMap { it.values }.flatMap { it.values }.flatten()
        recursionAll(allFilesChanges).forEach { row ->
            tableModel.addRow(row)
        }
    }

    private fun recursionAll(allFiles: List<Activity>): List<Array<Any>> {
        return allFiles.headTailOrNull()?.let { (head, tail) ->
            val row = toRow(head, tail.headTailOrNull()?.first?.activeRange?.to ?: System.currentTimeMillis())
            listOf(row) + recursionAll(tail)
        } ?: emptyList()
    }



    // Remove an activity from the table by row index
    fun removeActivity(rowIndex: Int) {
        if (rowIndex >= 0 && rowIndex < tableModel.rowCount) {
            tableModel.removeRow(rowIndex)
        }
    }

    private fun toRow(activity: Activity, endTime: Long): Array<Any> {
        val inactiveTimeRange = endTime - activity.activeRange.from
        return arrayOf(activity.projectName, activity.gitBranch,
            activity.fileName,
            Duration.ofMillis(inactiveTimeRange),
            Duration.ofMillis(inactiveTimeRange),
            Instant.ofEpochMilli(activity.activeRange.from).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }
}