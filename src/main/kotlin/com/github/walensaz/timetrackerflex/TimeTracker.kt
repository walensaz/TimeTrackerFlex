package com.github.walensaz.timetrackerflex

import com.github.walensaz.timetrackerflex.handlers.TimeTrackerEventHandler
import com.github.walensaz.timetrackerflex.state.persistence.TimeTrackerStatePersister
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class TimeTracker : Disposable, Logging {
    companion object {
        const val PERIODIC_DELAY = 15L
        const val SECONDS_ACTIVITY_TIMEOUT = 60L
    }

    val timeTrackerEventHandler = TimeTrackerEventHandler()

    private val mainWorker: ScheduledFuture<*> = AppExecutorUtil
        .getAppScheduledExecutorService()
        .scheduleWithFixedDelay({tick()}, 1, PERIODIC_DELAY, TimeUnit.SECONDS)

    private fun tick() {
        timeTrackerEventHandler.processEvents()
        TimeTrackerStatePersister.save(timeTrackerEventHandler.stateHolder)
    }

    override fun dispose() {
        TimeTrackerStatePersister.save(timeTrackerEventHandler.stateHolder)
        mainWorker.cancel(true)
    }


}