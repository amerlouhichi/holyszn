package com.holyszn.app.core

import android.content.Context

/** Privacy helpers: ignore sensitive apps and hide one-time codes before anything is stored. */
object Privacy {
    private val codeWords = listOf(
        "code", "otp", "pin", "password", "passcode", "verification", "codice",
        "mot de passe", "cvv", "رمز", "كلمة السر"
    )
    private val digits = Regex("\\b\\d{4,8}\\b")

    fun isPrivateApp(ctx: Context, pkg: String): Boolean {
        val p = pkg.lowercase()
        return Prefs.get(ctx).blockedApps.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .any { p.contains(it) }
    }

    fun redact(s: String): String {
        val l = s.lowercase()
        return if (codeWords.any { l.contains(it) }) digits.replace(s, "••••") else s
    }
}
