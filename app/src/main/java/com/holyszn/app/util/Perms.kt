package com.holyszn.app.util

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Perms {
    fun listenerEnabled(c: Context) =
        NotificationManagerCompat.getEnabledListenerPackages(c).contains(c.packageName)

    fun postGranted(c: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun fullScreenOk(c: Context) = Build.VERSION.SDK_INT < 34 ||
        c.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    private fun open(c: Context, i: Intent) {
        try { c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
    }

    fun openListenerSettings(c: Context) =
        open(c, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

    fun openAppDetails(c: Context) = open(
        c, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${c.packageName}"))
    )

    fun openFullScreenSettings(c: Context) {
        if (Build.VERSION.SDK_INT >= 34) open(
            c, Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:${c.packageName}"))
        ) else openAppDetails(c)
    }

    fun openSecuritySettings(c: Context) = open(c, Intent(Settings.ACTION_SECURITY_SETTINGS))
}
