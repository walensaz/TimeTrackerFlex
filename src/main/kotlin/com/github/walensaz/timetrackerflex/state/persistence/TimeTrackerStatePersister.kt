package com.github.walensaz.timetrackerflex.state.persistence

import com.github.walensaz.timetrackerflex.Logging
import com.github.walensaz.timetrackerflex.state.TimeTrackerStateHolder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object TimeTrackerStatePersister : FileBasedPersister, Logging {
    override val filePath = FileBasedPersister.getFilePath("TimeTrackerState.json")

    private val gson = GsonBuilder().setPrettyPrinting().create()

    // Save the TimeTrackerStateHolder to the specified file
    fun save(stateHolder: TimeTrackerStateHolder) {
        logTime("Saving stateholder with size ${stateHolder.allActivities().size} tp $filePath") {
            createFileIfNotExists()
            val json = gson.toJson(stateHolder)
            Files.write(Paths.get(filePath), json.toByteArray())
        }
    }

    // Load the TimeTrackerStateHolder from the specified file, or return a default empty one if the file doesn't exist
    fun load(): TimeTrackerStateHolder {
        val file = File(filePath)
        return if (file.exists() && file.isFile) {
            logTime("Stateholder loading at $filePath") {
                val json = Files.readString(file.toPath())
                val stateHolder =
                    gson.fromJson<TimeTrackerStateHolder>(json, object : TypeToken<TimeTrackerStateHolder>() {}.type)
                logInfo("Loaded ${stateHolder.allActivities().size} active activities on startup from path $filePath")
                stateHolder
            }
        } else {
            // Return an empty state holder if the file doesn't exist
            logInfo("No existing state exists at $filePath, starting a new state holder.")
            TimeTrackerStateHolder(emptyMap())
        }
    }
}
