package com.holyszn.app.ui

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import com.holyszn.app.notify.HolyNotifyService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.call.HolyCall
import com.holyszn.app.core.Prefs
import com.holyszn.app.data.HolyDb
import com.holyszn.app.util.Perms
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs.get(ctx) }
    val scope = rememberCoroutineScope()
    val tick = rememberResumeTick()
    val listener = remember(tick) { Perms.listenerEnabled(ctx) }
    val post = remember(tick) { Perms.postGranted(ctx) }
    val full = remember(tick) { Perms.fullScreenOk(ctx) }

    var keywords by remember { mutableStateOf(prefs.keywords) }
    var threshold by remember { mutableFloatStateOf(prefs.threshold.toFloat()) }
    var speak by remember { mutableStateOf(prefs.speak) }
    var alertsOn by remember { mutableStateOf(prefs.alertsOn) }
    var storeText by remember { mutableStateOf(prefs.storeText) }
    var secure by remember { mutableStateOf(prefs.secureScreen) }
    var retention by remember { mutableFloatStateOf(prefs.retentionDays.toFloat()) }
    var lockSec by remember { mutableFloatStateOf(prefs.lockSeconds.toFloat()) }
    var blocked by remember { mutableStateOf(prefs.blockedApps) }

    val postLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("SETTINGS", "PERMISSIONS • ALERTS • DATA") }

        item {
            HoloCard {
                Label("PERMISSIONS")
                PermRow("Notification access", listener) { Perms.openListenerSettings(ctx) }
                PermRow("Post notifications", post) {
                    if (Build.VERSION.SDK_INT >= 33)
                        postLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                PermRow("Full-screen alerts", full) { Perms.openFullScreenSettings(ctx) }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Installed from outside the Play Store? Android may grey out Notification access. " +
                        "Open App info, tap the ⋮ menu, choose 'Allow restricted settings', then try again.",
                    fontSize = 12.sp, color = Chrome2
                )
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { Perms.openAppDetails(ctx) }) { Text("OPEN APP INFO") }
            }
        }

        item {
            HoloCard {
                Label("LISTENER STATUS")
                val ok = listener && prefs.connected
                Text(
                    if (!listener) "Notification access is OFF"
                    else if (prefs.connected) "CONNECTED" else "ENABLED but NOT CONNECTED",
                    color = if (ok) Chrome1 else Red, fontFamily = FontFamily.Monospace
                )
                Text("Notifications seen: ${prefs.seen}", fontSize = 13.sp, color = Chrome2)
                Text(
                    "Last: " + (if (prefs.lastSeen > 0) fmtTime(prefs.lastSeen) else "never") +
                        " • " + prefs.lastPkg, fontSize = 12.sp, color = Chrome2
                )
                Text("Result: " + prefs.lastNote.ifBlank { "-" }, fontSize = 12.sp, color = Chrome2)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    try {
                        ctx.startActivity(
                            android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } catch (_: Exception) {}
                }) { Text("BATTERY SETTINGS (KEEP ALIVE)") }
                OutlinedButton(onClick = {
                    try { NotificationListenerService.requestRebind(ComponentName(ctx, HolyNotifyService::class.java)) }
                    catch (_: Exception) {}
                }) { Text("RECONNECT LISTENER") }
                Text("Leave this tab, then come back to refresh the numbers.", fontSize = 11.sp, color = Chrome2)
            }
        }

        item {
            HoloCard {
                Label("URGENT ALERTS")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Alerts enabled", Modifier.weight(1f))
                    Switch(checked = alertsOn, onCheckedChange = { alertsOn = it; prefs.alertsOn = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Speak alerts aloud", Modifier.weight(1f))
                    Switch(checked = speak, onCheckedChange = { speak = it; prefs.speak = it })
                }
                Spacer(Modifier.height(8.dp))
                Text("Urgency threshold: ${threshold.toInt()}", fontSize = 13.sp)
                Slider(
                    value = threshold, onValueChange = { threshold = it },
                    onValueChangeFinished = { prefs.threshold = threshold.toInt() },
                    valueRange = 20f..100f
                )
                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it; prefs.keywords = it },
                    label = { Text("Urgent keywords (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    keywords = Prefs.DEFAULT_KEYWORDS; prefs.keywords = keywords
                }) { Text("RESET KEYWORDS") }
            }
        }

        item {
            HoloCard {
                Label("PRIVACY")
                Text(
                    "This app only uses: notification access, posting its own alerts, full-screen alerts " +
                        "and your screen lock. It has no internet, microphone, camera, contacts, SMS or " +
                        "location permission.",
                    fontSize = 12.sp, color = Chrome2
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Store message text (off = titles only)", Modifier.weight(1f))
                    Switch(checked = storeText, onCheckedChange = { storeText = it; prefs.storeText = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hide app from screenshots and recent apps", Modifier.weight(1f))
                    Switch(checked = secure, onCheckedChange = {
                        secure = it; prefs.secureScreen = it
                        (ctx.findActivity() as? com.holyszn.app.MainActivity)?.applyScreenSecurity(it)
                    })
                }
                Text("Keep notifications: ${retention.toInt()} days", fontSize = 13.sp)
                Slider(
                    value = retention, onValueChange = { retention = it },
                    onValueChangeFinished = { prefs.retentionDays = retention.toInt() },
                    valueRange = 1f..30f
                )
                Text("Lock after leaving the app: ${lockSec.toInt()} s", fontSize = 13.sp)
                Slider(
                    value = lockSec, onValueChange = { lockSec = it },
                    onValueChangeFinished = { prefs.lockSeconds = lockSec.toInt() },
                    valueRange = 0f..900f
                )
                OutlinedTextField(
                    value = blocked,
                    onValueChange = { blocked = it; prefs.blockedApps = it },
                    label = { Text("Ignored apps (part of the app id, comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Notifications from these apps are never read or stored. One-time codes are hidden as ••••.",
                    fontSize = 11.sp, color = Chrome2)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    blocked = Prefs.DEFAULT_BLOCKED; prefs.blockedApps = blocked
                }) { Text("RESET IGNORED APPS") }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) { HolyDb.get(ctx).clearAllTables() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("PANIC WIPE: DELETE ALL MY DATA") }
            }
        }

        item {
            HoloCard {
                Label("DATA")
                Text("All data stays on this phone in a private app database.", fontSize = 13.sp, color = Chrome2)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        HolyCall.fire(ctx, "Test alert", "This is a HOLYSZN urgent alert test.")
                    }) { Text("TEST ALERT") }
                    OutlinedButton(onClick = {
                        scope.launch { HolyDb.get(ctx).notifDao().clear() }
                    }) { Text("WIPE FEED") }
                }
            }
        }

        item {
            Text("HOLYSZN 0.1.0 • Phase 1. HOLYCALL raises alarm-style alerts; it does not place phone calls.",
                fontSize = 11.sp, color = Chrome2)
        }
    }
}

@Composable
private fun Label(t: String) {
    Text(t, color = Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun PermRow(name: String, ok: Boolean, onFix: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name)
            Text(if (ok) "GRANTED" else "NOT GRANTED", fontSize = 11.sp,
                color = if (ok) Chrome2 else Red, fontFamily = FontFamily.Monospace)
        }
        if (!ok) Button(onClick = onFix, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("GRANT") }
    }
}
