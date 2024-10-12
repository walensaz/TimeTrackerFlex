package com.github.walensaz.timetrackerflex.handlers

import com.github.walensaz.timetrackerflex.Logging
import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.intellij.IntelliJUtils
import com.github.walensaz.timetrackerflex.state.Activity
import com.github.walensaz.timetrackerflex.state.TimeTrackerEvent
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.github.walensaz.timetrackerflex.state.TimeTrackerStateHolder
import com.github.walensaz.timetrackerflex.state.persistence.TimeTrackerStatePersister
import com.intellij.util.Range
import com.intellij.util.containers.headTail
import java.util.*

class TimeTrackerEventHandler : Logging {

    companion object {
        const val MILLIS_MAX_DIFF = TimeTracker.SECONDS_ACTIVITY_TIMEOUT * 1000
    }

    init {
        logInfo("Initialized ${javaClass.simpleName}.")
    }

    var eventQueue: Map<TimeTrackerEventType, Queue<TimeTrackerEvent>> = emptyMap()

    var stateHolder: TimeTrackerStateHolder = TimeTrackerStatePersister.load()

    fun handleEvent(evt: TimeTrackerEvent) {
        synchronized(eventQueue) {
            val existingEvents = eventQueue.getOrDefault(evt.type, LinkedList())
            existingEvents.add(evt)
            eventQueue = eventQueue + (evt.type to existingEvents)
        }
    }

    fun processEvents() {
        if (eventQueue.isNotEmpty()) {
            val eventQueueCopy = copyAndClearQueues()
            synchronized(stateHolder) {
                eventQueueCopy.map { (eventType, queue) ->
                    when (eventType) {
                        TimeTrackerEventType.TYPING -> handleTypingQueue(queue.toList())
                        TimeTrackerEventType.CHANGE_FILE -> queue.map {
                            stateHolder = stateHolder.combine(queue.map { createActivity(it) })
                        }
                        TimeTrackerEventType.BRANCH_CHANGE -> queue.map {
                            stateHolder = stateHolder.combine(queue.map { createActivity(it) })
                        }
                    }
                }
            }
        }
    }

    private fun handleTypingQueue(queue: List<TimeTrackerEvent>) {
        val (head, tail) = queue.headTail()
        val defaultActivity = createActivity(head)
        val pairOfNewActivitiesAndLastActivity =
            tail.fold(Pair(listOf<Activity>(), defaultActivity)) { accPair, event ->
                val mostRecentActivity = accPair.second
                val existingActivities = accPair.first
                val timeDiff = event.timestamp - mostRecentActivity.activeRange.to

                if (timeDiff < MILLIS_MAX_DIFF && event.canBeCombined(mostRecentActivity)) {
                    Pair(
                        existingActivities,
                        mostRecentActivity.copy(
                            activeRange = Range(
                                mostRecentActivity.activeRange.from,
                                event.timestamp
                            )
                        )
                    )
                } else {
                    val endTime = Math.min(event.timestamp - 1, mostRecentActivity.activeRange.to + MILLIS_MAX_DIFF)
                    Pair(
                        existingActivities + mostRecentActivity.copy(
                            activeRange = Range(
                                mostRecentActivity.activeRange.from,
                                endTime
                            )
                        ),
                        createActivity(event)
                    )
                }
            }
        val newActivities = pairOfNewActivitiesAndLastActivity.first + pairOfNewActivitiesAndLastActivity.second
        stateHolder = stateHolder.combine(newActivities)
    }

    private fun createActivity(event: TimeTrackerEvent): Activity {
        return when(event.type) {
            TimeTrackerEventType.BRANCH_CHANGE -> {
                val projectName = event.project.name
                val defaultActivity = Activity(Range<Long>(event.timestamp, event.timestamp), event.type, projectName, event.fileName, "N/A")
                defaultActivity
            }
            else -> {
                val branch = IntelliJUtils.getGitBranchOrEmpty(event.project)
                val projectName = event.project.name

                val defaultActivity = Activity(Range<Long>(event.timestamp, event.timestamp), event.type, projectName, branch, event.fileName)
                defaultActivity
            }
        }

    }

    private fun copyAndClearQueues(): Map<TimeTrackerEventType, Queue<TimeTrackerEvent>> {
        synchronized(eventQueue) {
            val copy = eventQueue
            eventQueue = emptyMap()
            return copy
        }
    }

}