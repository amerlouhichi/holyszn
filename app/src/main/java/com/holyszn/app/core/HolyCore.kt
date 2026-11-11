package com.holyszn.app.core

import android.content.Context

/** HOLYCORE: on-device, rule-based notification analysis. No data leaves the phone. */
object HolyCore {
    data class Verdict(val score: Int, val urgent: Boolean, val reasons: List<String>)

    private val priorityApps = setOf(
        "com.whatsapp", "org.telegram.messenger", "com.google.android.apps.messaging",
        "com.android.messaging", "com.samsung.android.messaging", "com.facebook.orca",
        "com.instagram.android", "com.android.dialer", "com.google.android.dialer",
        "com.android.server.telecom", "com.samsung.android.dialer"
    )

    fun analyze(ctx: Context, pkg: String, title: String, text: String): Verdict {
        val prefs = Prefs.get(ctx)
        val hay = "$title $text".lowercase()
        val reasons = mutableListOf<String>()
        var score = 0

        val hits = prefs.keywords.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() && hay.contains(it) }
            .distinct()
        if (hits.isNotEmpty()) {
            score += minOf(hits.size * 35, 70)
            reasons += "keywords: " + hits.joinToString()
        }
        if (pkg in priorityApps) { score += 20; reasons += "priority app" }
        if (hay.contains("!")) { score += 10; reasons += "exclamation" }

        score = score.coerceIn(0, 100)
        return Verdict(score, score >= prefs.threshold, reasons)
    }
}
