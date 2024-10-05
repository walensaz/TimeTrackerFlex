package com.github.walensaz.timetrackerflex.handlers

import com.github.walensaz.timetrackerflex.Logging
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.intellij.IntelliJUtils
import com.github.walensaz.timetrackerflex.state.Activity
import com.github.walensaz.timetrackerflex.state.TimeTrackerEvent
import com.github.walensaz.timetrackerflex.state.TimeTrackerStateHolder
import com.github.walensaz.timetrackerflex.state.persistence.TimeTrackerStatePersister
import com.intellij.util.Range
import com.intellij.util.containers.headTail
import java.util.*

class TimeTrackerEventHandler : Logging {

    companion object {
        const val MILLIS_MAX_DIFF = TimeTracker.SECONDS_ACTIVITY_TIMEOUT * 1000
    }

    val eventQueue: Queue<TimeTrackerEvent> = LinkedList()

    var stateHolder: TimeTrackerStateHolder = TimeTrackerStatePersister.load()

    fun handleEvent(evt: TimeTrackerEvent) {
        synchronized(eventQueue) {
            eventQueue.add(evt)
        }
    }

    fun processEvents() {
        if (eventQueue.isNotEmpty()) {
            val queue = copyAndClearQueue()
            synchronized(stateHolder) {
                val (head, tail) = queue.headTail()
                val defaultActivity = createActivity(head)
                val pairOfNewActivitiesAndLastActivity = tail.fold(Pair(listOf<Activity>(), defaultActivity)) { accPair, event ->
                    val mostRecentActivity = accPair.second
                    val existingActivities = accPair.first
                    val timeDiff = event.timestamp - mostRecentActivity.activeRange.to

                    if (timeDiff < MILLIS_MAX_DIFF && event.canBeCombined(mostRecentActivity)) {
                        Pair(
                            existingActivities,
                            mostRecentActivity.copy(activeRange = Range(mostRecentActivity.activeRange.from, event.timestamp))
                        )
                    } else {
                        val endTime = Math.min(event.timestamp - 1, mostRecentActivity.activeRange.to + MILLIS_MAX_DIFF)
                        Pair(
                            existingActivities + mostRecentActivity.copy(activeRange = Range(mostRecentActivity.activeRange.from, endTime)),
                            createActivity(event)
                        )
                    }
                }
                val newActivities = pairOfNewActivitiesAndLastActivity.first + pairOfNewActivitiesAndLastActivity.second
                stateHolder = stateHolder.combine(newActivities)
            }
        }
    }

    private fun createActivity(event: TimeTrackerEvent): Activity {
        val branch = IntelliJUtils.getGitBranchOrEmpty(event.project)
        val projectName = event.project.name

        val defaultActivity = Activity(Range<Long>(event.timestamp, event.timestamp), projectName, branch, event.fileName)
        return defaultActivity
    }

    private fun copyAndClearQueue(): List<TimeTrackerEvent> {
        synchronized(eventQueue) {
            val copy = eventQueue.toList()
            eventQueue.clear()
            return copy
        }
    }

}