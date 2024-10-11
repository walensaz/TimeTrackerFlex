package com.github.walensaz.timetrackerflex.windows.components.renderer

import java.time.Duration
import javax.swing.table.DefaultTableCellRenderer

object FormattedDurationCellRenderer : DefaultTableCellRenderer() {
    override fun setValue(value: Any?) {
        if (value is Duration) {
            text = formatDuration(value)
        } else {
            super.setValue(value)
        }
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()

        return String.format("%d h %02d m %02d s", hours, minutes, seconds)
    }
}