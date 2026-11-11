package com.holyszn.app.notify

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.holyszn.app.call.HolyCall
import com.holyszn.app.core.HolyCore
import com.holyszn.app.core.Prefs
import com.holyszn.app.data.HolyDb
import com.holyszn.app.data.NotifEntity
import kotlinx.coroutines.*

/** HOLYNOTIFY: reads notifications the user has allowed via Android's Notification Access. */
class HolyNotifyService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val recent = LinkedHashMap<String, Long>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val ex = n.extras
        val title = ex.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (ex.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: ex.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val key = "${sbn.packageName}|$title|$text"
        val now = System.currentTimeMillis()
        synchronized(recent) {
            val last = recent[key]
            if (last != null && now - last < 10_000) return
            recent[key] = now
            if (recent.size > 100) recent.remove(recent.keys.first())
        }

        val app = ex.getString("android.substName")
            ?: sbn.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        val verdict = HolyCore.analyze(this, sbn.packageName, title, text)

        scope.launch {
            val db = HolyDb.get(applicationContext)
            db.notifDao().insert(
                NotifEntity(
                    pkg = sbn.packageName, app = app, title = title, text = text,
                    score = verdict.score, urgent = verdict.urgent, time = now
                )
            )
            db.notifDao().prune(now - 30L * 24 * 3600 * 1000)
        }
        if (verdict.urgent && Prefs.get(this).alertsOn) {
            HolyCall.fire(this, "$app: ${title.ifBlank { "New message" }}", text)
        }
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
