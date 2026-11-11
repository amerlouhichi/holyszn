package com.holyszn.app

import android.content.ComponentName
import android.os.Bundle
import android.service.notification.NotificationListenerService
import com.holyszn.app.notify.HolyNotifyService
import com.holyszn.app.util.Perms
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.holyszn.app.call.HolyCall
import com.holyszn.app.core.Prefs
import com.holyszn.app.ui.*

class MainActivity : FragmentActivity() {
    private var unlocked by mutableStateOf(false)
    private var authMsg by mutableStateOf("")
    private var prompting = false
    private var lastStop = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HolyCall.ensureChannel(this)
        setContent {
            HolyTheme {
                if (unlocked) HolyApp() else LockScreen(authMsg) { authenticate() }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val prefs = Prefs.get(this)
        applyScreenSecurity(prefs.secureScreen)
        val within = sessionStillValid(prefs)
        if (unlocked && !within) unlocked = false
        else if (!unlocked && within) unlocked = true
        if (Perms.listenerEnabled(this)) {
            try { NotificationListenerService.requestRebind(ComponentName(this, HolyNotifyService::class.java)) }
            catch (_: Exception) {}
        }
        if (!unlocked) authenticate()
    }

    override fun onStop() {
        super.onStop()
        lastStop = SystemClock.elapsedRealtime()
        Prefs.get(this).lastLeft = lastStop
    }

    /** True if the user verified recently and came back within the lock time (survives process death). */
    private fun sessionStillValid(p: Prefs): Boolean {
        val now = SystemClock.elapsedRealtime()
        val unlockedAt = p.unlockedAt
        if (unlockedAt < 0 || now < unlockedAt) return false // never unlocked, or phone rebooted
        val away = if (p.lastLeft >= unlockedAt) now - p.lastLeft else 0L
        return away <= p.lockSeconds * 1000L
    }

    fun applyScreenSecurity(on: Boolean) {
        if (on) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun authenticate() {
        if (prompting) return
        val allowed = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(allowed) != BiometricManager.BIOMETRIC_SUCCESS) {
            authMsg = "No screen lock found. Set a PIN, fingerprint or face unlock in Android settings, then return."
            return
        }
        prompting = true
        val prompt = BiometricPrompt(
            this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    prompting = false; authMsg = ""; unlocked = true
                    Prefs.get(this@MainActivity).unlockedAt = SystemClock.elapsedRealtime()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    prompting = false; authMsg = errString.toString()
                }
            }
        )
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("HOLYSZN")
                .setSubtitle("Verify your identity (face, fingerprint or PIN)")
                .setAllowedAuthenticators(allowed)
                .build()
        )
    }
}

private data class NavTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun HolyApp() {
    val tabs = listOf(
        NavTab("CORE", Icons.Filled.Home),
        NavTab("FEED", Icons.Filled.List),
        NavTab("TASKS", Icons.Filled.Done),
        NavTab("MEMORY", Icons.Filled.Favorite),
        NavTab("SETUP", Icons.Filled.Settings)
    )
    var sel by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        containerColor = Void,
        bottomBar = {
            NavigationBar(containerColor = Panel) {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = sel == i, onClick = { sel = i },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Red, selectedTextColor = Red,
                            indicatorColor = DeepRed.copy(alpha = 0.35f),
                            unselectedIconColor = Chrome2, unselectedTextColor = Chrome2
                        )
                    )
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (sel) {
                0 -> CoreScreen()
                1 -> FeedScreen()
                2 -> TasksScreen()
                3 -> MemoryScreen()
                else -> SettingsScreen()
            }
        }
    }
}
