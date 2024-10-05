package com.github.walensaz.timetrackerflex

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logging {

    // Get the current time in the format: yyyy-MM-dd HH:mm:ss,SSS
    private fun currentTime(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")
        return LocalDateTime.now().format(formatter)
    }

    // Get the current thread ID
    private fun currentThreadId(): String {
        return Thread.currentThread().id.toString().padStart(5)
    }

    // Get the class name of the caller
    private fun getCallerClassName(): String {
        val stackTrace = Thread.currentThread().stackTrace
        var foundLogger = false

        // Loop through stack trace to find the actual caller class, skipping logger-related entries
        for (element in stackTrace) {
            val className = element.className

            // Skip the logger class and any related classes
            if (className.startsWith("kotlin.") || className.startsWith("java.")) {
                continue // Skip JVM and standard library classes
            }

            // We need to check if the current method is part of the logger interface or its implementation
            if (className.contains("Logger") || className.contains("DefaultImpls")) {
                foundLogger = true // Indicate we have passed through the logger context
            } else if (foundLogger) {
                return className // First non-logger class after logger calls
            }
        }
        return "UnknownClass"
    }

    // Format the log message with a timestamp, thread ID, log level, and dynamic class name
    private fun formatLog(level: String, message: String): String {
        val callerClass = getCallerClassName()
        return "${currentTime()} [${currentThreadId()}]   $level - #$callerClass - $message"
    }

    fun logInfo(message: String) {
        println("\u001B[32m${formatLog("INFO ", message)}\u001B[0m")  // Green text
    }

    fun logDebug(message: String) {
        println("\u001B[34m${formatLog("DEBUG", message)}\u001B[0m")  // Blue text
    }

    fun logWarning(message: String) {
        println("\u001B[33m${formatLog("WARN ", message)}\u001B[0m")  // Yellow text
    }

    // Measure and log the execution time of a function
    fun <T> logTime(message: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        logInfo("$message started.")

        val result = block()  // Run the function

        val endTime = System.currentTimeMillis()
        logInfo("$message finished. Took ${endTime - startTime} ms.")

        return result
    }

}
