package com.github.walensaz.timetrackerflex.windows.components

import com.github.walensaz.timetrackerflex.state.Activity
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.containers.toArray
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.awt.BorderLayout
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.swing.table.TableRowSorter

class RawActivityLog(activities: List<Activity>) : JBPanel<RawActivityLog>(BorderLayout()) {
    private val columnNames = arrayOf("Type", "Project", "Branch", "File", "Duration (Seconds)", "Date")

    // The model to store and manage table data
    private val tableModel = object : DefaultTableModel(mapActivitiesToData(activities), columnNames) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
//            return column == 2
            return false
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return when (columnIndex) {
                4 -> Long::class.java
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
        val scrollPane = JBScrollPane(table)
        add(scrollPane, BorderLayout.CENTER)
    }

    // Helper method to convert the list of activities to a table-compatible data array
    private fun mapActivitiesToData(activities: List<Activity>): Array<Array<Any>> {
        return activities.map {
            toRow(it)
        }.toArray(emptyArray())
    }

    // Add an activity to the table
//    fun addActivity(activity: Activity) {
//        tableModel.addRow(arrayOf(activity.name, activity.duration, activity.isCompleted))
//    }

    fun clearTable() {
        tableModel.setNumRows(0)
    }

    fun addAll(activities: List<Activity>) {
        activities.forEach { activity ->
            tableModel.addRow(toRow(activity))
        }
    }

    // Remove an activity from the table by row index
    fun removeActivity(rowIndex: Int) {
        if (rowIndex >= 0 && rowIndex < tableModel.rowCount) {
            tableModel.removeRow(rowIndex)
        }
    }

    private fun toRow(activity: Activity): Array<Any> {
        val timeRange = activity.activeRange.to - activity.activeRange.from
        return arrayOf(activity.activityType.name, activity.projectName, activity.gitBranch,
            activity.fileName, TimeUnit.MILLISECONDS.toSeconds(timeRange),
            Instant.ofEpochMilli(activity.activeRange.from).atZone(ZoneId.systemDefault()).toLocalDateTime()
        )
    }
}
