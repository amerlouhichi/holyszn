package com.holyszn.app.brief

import com.holyszn.app.data.NotifEntity
import java.util.Calendar

/** HOLYBRIEF: builds the daily briefing text from local data. */
object HolyBrief {
    fun compose(total: Int, urgent: Int, openTasks: Int, lastUrgent: NotifEntity?): String {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greet = when {
            h < 12 -> "Good morning"
            h < 18 -> "Good afternoon"
            else -> "Good evening"
        }
        val sb = StringBuilder("$greet. ")
        sb.append("In the last 24 hours I analysed $total notifications, $urgent flagged urgent. ")
        sb.append(if (openTasks == 0) "You have no open tasks. " else "You have $openTasks open tasks. ")
        if (lastUrgent != null) sb.append("Latest urgent: ${lastUrgent.app}, ${lastUrgent.title}.")
        return sb.toString().trim()
    }
}
