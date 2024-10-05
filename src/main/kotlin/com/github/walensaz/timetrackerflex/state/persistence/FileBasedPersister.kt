package com.github.walensaz.timetrackerflex.state.persistence

import com.intellij.openapi.application.PathManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

interface FileBasedPersister {
    companion object {
        private val DEFAULT_PLUGIN_DIRECTORY = PathManager.getPluginsPath()
        val PLUGIN_DIRECTORY = Paths.get(DEFAULT_PLUGIN_DIRECTORY, "TimeTrackerFlex")
        init {
            Files.createDirectories(PLUGIN_DIRECTORY)
        }

        fun getFilePath(fileName: String): String {
            return Paths.get(PLUGIN_DIRECTORY.toString(), fileName).toString()
        }
    }

    val filePath: String

    fun createFileIfNotExists() {
        File(filePath).createNewFile()
    }
}