package com.holyszn.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.holyszn.app.MainActivity
import com.holyszn.app.R

/**
 * Keeps HOLYSZN's process alive so Android does not put the notification watcher to sleep.
 * It reads nothing and uses no network. It only shows one small ongoing notification.
 */
class KeepAliveService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CH) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CH, "HOLYSZN watching", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val open = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle("HOLYSZN is watching")
            .setContentText("Urgent alerts are active")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(open)
            .build()
        try {
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(ID, n)
            }
        } catch (_: Exception) { stopSelf() }
        return START_STICKY
    }

    companion object {
        private const val CH = "holy_keepalive"
        private const val ID = 4243
        fun start(ctx: Context) {
            try { ContextCompat.startForegroundService(ctx, Intent(ctx, KeepAliveService::class.java)) }
            catch (_: Exception) {}
        }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, KeepAliveService::class.java)) }
    }
}
