package com.holyszn.app.core

import android.content.Context
import android.content.SharedPreferences

class Prefs private constructor(ctx: Context) {
    private val sp: SharedPreferences =
        ctx.applicationContext.getSharedPreferences("holyszn", Context.MODE_PRIVATE)

    var keywords: String
        get() = sp.getString("keywords", DEFAULT_KEYWORDS) ?: DEFAULT_KEYWORDS
        set(v) = sp.edit().putString("keywords", v).apply()
    var threshold: Int
        get() = sp.getInt("threshold", 55)
        set(v) = sp.edit().putInt("threshold", v).apply()
    var speak: Boolean
        get() = sp.getBoolean("speak", true)
        set(v) = sp.edit().putBoolean("speak", v).apply()
    var alertsOn: Boolean
        get() = sp.getBoolean("alertsOn", true)
        set(v) = sp.edit().putBoolean("alertsOn", v).apply()

    var connected: Boolean
        get() = sp.getBoolean("connected", false)
        set(v) = sp.edit().putBoolean("connected", v).apply()
    var seen: Int
        get() = sp.getInt("seen", 0)
        set(v) = sp.edit().putInt("seen", v).apply()
    var lastSeen: Long
        get() = sp.getLong("lastSeen", 0L)
        set(v) = sp.edit().putLong("lastSeen", v).apply()
    var lastPkg: String
        get() = sp.getString("lastPkg", "") ?: ""
        set(v) = sp.edit().putString("lastPkg", v).apply()
    var lastNote: String
        get() = sp.getString("lastNote", "") ?: ""
        set(v) = sp.edit().putString("lastNote", v).apply()

    var blockedApps: String
        get() = sp.getString("blockedApps", DEFAULT_BLOCKED) ?: DEFAULT_BLOCKED
        set(v) = sp.edit().putString("blockedApps", v).apply()
    var retentionDays: Int
        get() = sp.getInt("retentionDays", 7)
        set(v) = sp.edit().putInt("retentionDays", v).apply()
    var storeText: Boolean
        get() = sp.getBoolean("storeText", true)
        set(v) = sp.edit().putBoolean("storeText", v).apply()
    var lockSeconds: Int
        get() = sp.getInt("lockSeconds", 30)
        set(v) = sp.edit().putInt("lockSeconds", v).apply()

    companion object {
        const val DEFAULT_BLOCKED =
            "bank,banque,paypal,authenticator,authy,bitwarden,keepass,1password,lastpass,wallet,revolut,binance,payoneer,poste"

        const val DEFAULT_KEYWORDS =
            "urgent,emergency,asap,immediately,important,critical,security,fraud,suspicious,hospital,accident,deadline,عاجل,طارئ,urgence"
        @Volatile private var inst: Prefs? = null
        fun get(ctx: Context): Prefs = inst ?: synchronized(this) {
            inst ?: Prefs(ctx).also { inst = it }
        }
    }
}
