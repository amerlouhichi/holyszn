package com.holyszn.app.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.holyszn.app.R

/**
 * HOLYCALL: urgent-alert delivery. It does NOT place telephone calls.
 * It raises a loud alarm-style notification plus a full-screen alert with spoken text.
 */
object HolyCall {
    private const val CH = "holy_urgent"
    private const val NOTIF_ID = 4242

    fun ensureChannel(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH) != null) return
        val ch = NotificationChannel(CH, "HOLYCALL urgent alerts", NotificationManager.IMPORTANCE_HIGH)
        ch.description = "Loud full-screen alerts for urgent notifications"
        ch.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        )
        ch.enableVibration(true)
        ch.vibrationPattern = longArrayOf(0, 600, 300, 600, 300, 600)
        ch.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        nm.createNotificationChannel(ch)
    }

    fun fire(ctx: Context, title: String, text: String) {
        ensureChannel(ctx)
        com.holyszn.app.core.Prefs.get(ctx).lastAlertAt = System.currentTimeMillis()
        val i = Intent(ctx, AlertActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("title", title).putExtra("text", text)
        val pi = PendingIntent.getActivity(
            ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = NotificationCompat.Builder(ctx, CH)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle("HOLYCALL • $title")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pi, true)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        n.flags = n.flags or Notification.FLAG_INSISTENT
        try {
            ctx.getSystemService(NotificationManager::class.java).notify(NOTIF_ID, n)
        } catch (_: SecurityException) { /* notification permission not granted */ }
    }

    fun dismiss(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java).cancel(NOTIF_ID)
    }
}
