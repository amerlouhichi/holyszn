package com.holyszn.app.notify

import android.app.Notification
import android.content.ComponentName
import android.os.Bundle
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
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            Prefs.get(applicationContext).lastNote = "db error: ${e.javaClass.simpleName} ${e.message}"
        }
    )
    private val recent = LinkedHashMap<String, Long>()

    override fun onListenerConnected() {
        val p = Prefs.get(this)
        p.connected = true
        p.lastNote = "listener connected"
    }

    override fun onListenerDisconnected() {
        Prefs.get(this).connected = false
        try { requestRebind(ComponentName(this, HolyNotifyService::class.java)) } catch (_: Exception) {}
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val prefs = Prefs.get(this)
        try {
            prefs.seen = prefs.seen + 1
            prefs.lastSeen = System.currentTimeMillis()
            prefs.lastPkg = sbn.packageName
            handle(sbn, prefs)
        } catch (e: Exception) {
            prefs.lastNote = "error: ${e.javaClass.simpleName} ${e.message}"
        }
    }

    private fun handle(sbn: StatusBarNotification, prefs: Prefs) {
        if (sbn.packageName == packageName) { prefs.lastNote = "skipped: own notification"; return }
        val n = sbn.notification ?: return
        if (n.flags and Notification.FLAG_ONGOING_EVENT != 0) { prefs.lastNote = "skipped: ongoing"; return }

        val ex = n.extras
        val title = (ex.getCharSequence(Notification.EXTRA_TITLE)
            ?: ex.getCharSequence(Notification.EXTRA_TITLE_BIG)
            ?: ex.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)
            ?: n.tickerText)?.toString().orEmpty()
        val text = extractText(ex)
        if (title.isBlank() && text.isBlank()) { prefs.lastNote = "skipped: empty (${sbn.packageName})"; return }

        val key = "${sbn.packageName}|$title|$text"
        val now = System.currentTimeMillis()
        synchronized(recent) {
            val last = recent[key]
            if (last != null && now - last < 10_000) { prefs.lastNote = "skipped: duplicate"; return }
            recent[key] = now
            if (recent.size > 100) recent.remove(recent.keys.first())
        }

        val app = ex.getString("android.substName")
            ?: sbn.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        val verdict = HolyCore.analyze(this, sbn.packageName, title, text)
        prefs.lastNote = "saved ${sbn.packageName}: score ${verdict.score}" +
            if (verdict.urgent) " URGENT" else ""

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
        if (verdict.urgent && prefs.alertsOn) {
            HolyCall.fire(this, "$app: ${title.ifBlank { "New message" }}", text.take(300))
        }
    }

    @Suppress("DEPRECATION")
    private fun extractText(ex: Bundle): String {
        val parts = mutableListOf<String>()
        ex.getCharSequence(Notification.EXTRA_BIG_TEXT)?.let { parts += it.toString() }
        ex.getCharSequence(Notification.EXTRA_TEXT)?.let { parts += it.toString() }
        ex.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { parts += it.toString() }
        ex.getParcelableArray(Notification.EXTRA_MESSAGES)?.forEach { p ->
            (p as? Bundle)?.getCharSequence("text")?.let { parts += it.toString() }
        }
        ex.getCharSequence(Notification.EXTRA_SUB_TEXT)?.let { parts += it.toString() }
        return parts.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString("\n")
    }

    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
