package com.github.walensaz.timetrackerflex.state

import com.github.walensaz.timetrackerflex.handlers.TimeTrackerEventHandler
import com.intellij.util.Range

typealias ProjectName = String
typealias BranchName = String
typealias FileName = String

data class TimeTrackerStateHolder(
    val activityLog:
        Map<ProjectName,
                Map<BranchName,
                        Map<FileName,
                                List<Activity>>
                >>
) {

    fun allActivities(): List<Activity> {
        return combineMaps(activityLog.values.flatMap { it.values }).flatMap { it.value }
    }

    fun combine(newActivities: List<Activity>): TimeTrackerStateHolder {
        return copy(activityLog = newActivities.fold(activityLog) { acc, activity ->
            val existingByProjectName = acc[activity.projectName] ?: emptyMap()
            val existingByBranchName = existingByProjectName[activity.gitBranch] ?: emptyMap()
            val existingByFileName = existingByBranchName[activity.fileName] ?: emptyList()
            val newActivities = compareEndAndNew(existingByFileName, activity)

            // Build the new structure, making sure to create new instances for immutability
            val updatedBranch = existingByBranchName + (activity.fileName to newActivities)
            val updatedProject = existingByProjectName + (activity.gitBranch to updatedBranch)

            // Combine with the accumulator
            acc + (activity.projectName to updatedProject)
        })
    }

    private fun compareEndAndNew(activityLog: List<Activity>, newActivity: Activity): List<Activity> {
        return if (activityLog.isEmpty()) listOf(newActivity)
        else {
            val lastEntry = activityLog.last()
            if (lastEntry.canCombine(newActivity)) {
                val diff = newActivity.activeRange.to - newActivity.activeRange.from
                val fixedLastEntry =
                    lastEntry.copy(activeRange = Range(lastEntry.activeRange.from, lastEntry.activeRange.to + diff))
                activityLog.dropLast(1) + fixedLastEntry
            } else activityLog + newActivity
        }
    }

    private fun <K, N> combineMaps(maps: Collection<Map<K, List<N>>>): Map<K, List<N>> {
        return maps.fold(emptyMap()) { acc, map ->
            map.entries.fold(acc) { acc, (key, value) ->
                val existing = acc[key] ?: listOf()
                acc + (key to (existing + value))
            }
        }
    }
}

data class Activity(
    val activeRange: Range<Long>,
    val projectName: String,
    val gitBranch: String,
    val fileName: String
) {
    fun canCombine(activity: Activity): Boolean {
        val sameProject = activity.projectName == this.projectName
        val sameBranch = activity.gitBranch == this.gitBranch
        val sameFile = activity.fileName == this.fileName

        val timeCloseEnough =
            if (this.activeRange.to < activity.activeRange.from)
                activity.activeRange.from - this.activeRange.to < TimeTrackerEventHandler.MILLIS_MAX_DIFF
            else
                this.activeRange.from - activity.activeRange.to < TimeTrackerEventHandler.MILLIS_MAX_DIFF


        return sameProject && sameBranch && sameFile && timeCloseEnough
    }
}


