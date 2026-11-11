package com.holyszn.app.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** HOLYVOICE: text-to-speech output using Android's built-in engine. */
class HolyVoice(ctx: Context) : TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(ctx.applicationContext, this)
    private var ready = false
    private var pending: String? = null

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts.language = Locale.getDefault()
            pending?.let { speak(it) }
            pending = null
        }
    }

    fun speak(text: String) {
        if (ready) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "holy") else pending = text
    }

    fun shutdown() { tts.stop(); tts.shutdown() }
}
