package com.github.walensaz.timetrackerflex.windows.components.renderer

import java.time.LocalDateTime
import javax.swing.table.DefaultTableCellRenderer

object FormattedDateRenderer : DefaultTableCellRenderer() {
    override fun setValue(value: Any?) {
        if (value is LocalDateTime) {
            text = formatDate(value)
        } else {
            super.setValue(value)
        }
    }

    private fun formatDate(date: LocalDateTime): String {
        return String.format("%02d-%02d-%02d %02d:%02d:%02d", date.year, date.monthValue, date.dayOfMonth, date.hour, date.minute, date.second)
    }
}