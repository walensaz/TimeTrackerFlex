package com.github.walensaz.timetrackerflex

import com.github.walensaz.timetrackerflex.handlers.TimeTrackerEventHandler
import com.github.walensaz.timetrackerflex.state.persistence.TimeTrackerStatePersister
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class TimeTracker : Disposable, Logging {
    companion object {
        const val PERIODIC_DELAY = 30L
        const val SECONDS_ACTIVITY_TIMEOUT = 60L
    }

    init {
        logInfo("New time tracker service created.")
    }

    val timeTrackerEventHandler = TimeTrackerEventHandler()

    private val mainWorker: ScheduledFuture<*> = AppExecutorUtil
        .getAppScheduledExecutorService()
        .scheduleWithFixedDelay({tick()}, 1, PERIODIC_DELAY, TimeUnit.SECONDS)

    private fun tick() {
        timeTrackerEventHandler.processEvents()
    }

    override fun dispose() {
        logInfo("Disposing...")
        TimeTrackerStatePersister.save(timeTrackerEventHandler.stateHolder)
        mainWorker.cancel(true)
    }


}