package com.github.walensaz.timetrackerflex.listeners

import com.github.walensaz.timetrackerflex.Logging
import com.github.walensaz.timetrackerflex.TimeTracker
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class MyApplicationActivationListener : ApplicationActivationListener, Logging {

    override fun applicationActivated(ideFrame: IdeFrame) {
        logInfo("TimeTrackerFlex has been started on ${ideFrame.project}")
    }
}
