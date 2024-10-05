package com.github.walensaz.timetrackerflex.intellij

import com.github.walensaz.timetrackerflex.TimeTracker
import com.github.walensaz.timetrackerflex.factory.TimeTrackerEventFactory
import com.github.walensaz.timetrackerflex.state.TimeTrackerEventType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.components.serviceOrNull
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class KeyHandler : TypedHandlerDelegate() {

    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        IntelliJUtils.isVersionControlEnabled(project)
        serviceOrNull<TimeTracker>()?.let { timeTracker ->
            timeTracker.timeTrackerEventHandler.handleEvent(TimeTrackerEventFactory.create(
                TimeTrackerEventType.TYPING,
                project,
                file.name
            ))
        }

        return super.charTyped(c, project, editor, file)
    }
}