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
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ActivityTable(activities: List<Activity>) : JBPanel<ActivityTable>(BorderLayout()) {
    private val columnNames = arrayOf("Project", "Branch", "File", "Duration (Seconds)", "Date")

    // The model to store and manage table data
    private val tableModel = object : DefaultTableModel(mapActivitiesToData(activities), columnNames) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
//            return column == 2
            return false
        }

        override fun getColumnClass(columnIndex: Int): Class<*> {
            return when (columnIndex) {
                3 -> Long::class.java
                else -> String::class.java
            }
        }
    }

    val table: JBTable = JBTable(tableModel)

    init {
        table.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table.fillsViewportHeight = true
        val scrollPane = JBScrollPane(table)
        add(scrollPane, BorderLayout.CENTER)
    }

    // Helper method to convert the list of activities to a table-compatible data array
    private fun mapActivitiesToData(activities: List<Activity>): Array<Array<Any>> {
        return activities.map {
            toRow(it)
        }.toArray(emptyArray()) as Array<Array<Any>>
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

    // Update an activity in the table
//    fun updateActivity(rowIndex: Int, activity: Activity) {
//        if (rowIndex >= 0 && rowIndex < tableModel.rowCount) {
//            tableModel.setValueAt(activity.name, rowIndex, 0)
//            tableModel.setValueAt(activity.duration, rowIndex, 1)
//            tableModel.setValueAt(activity.isCompleted, rowIndex, 2)
//        }
//    }

    private fun toRow(activity: Activity): Array<Any> {
        val timeRange = activity.activeRange.to - activity.activeRange.from
        return arrayOf(activity.projectName, activity.gitBranch,
            activity.fileName, TimeUnit.MILLISECONDS.toSeconds(timeRange),
            Instant.ofEpochMilli(activity.activeRange.from).atZone(ZoneId.systemDefault()).toLocalDateTime().toString()
        )
    }
}
