package com.holyszn.app.call

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.core.Prefs
import com.holyszn.app.ui.*
import com.holyszn.app.voice.HolyVoice

class AlertActivity : ComponentActivity() {
    private var voice: HolyVoice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val title = intent.getStringExtra("title") ?: "URGENT"
        val text = intent.getStringExtra("text") ?: ""
        if (Prefs.get(this).speak) {
            voice = HolyVoice(this).also { it.speak("Urgent alert. $title. $text") }
        }
        setContent {
            HolyTheme { AlertScreen(title, text) { HolyCall.dismiss(this); finish() } }
        }
    }

    override fun onDestroy() { voice?.shutdown(); super.onDestroy() }
}

@Composable
private fun AlertScreen(title: String, text: String, onAck: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("URGENT", color = Red, fontSize = 44.sp, fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(20.dp))
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(text, color = Chrome1, fontSize = 17.sp)
        Spacer(Modifier.height(36.dp))
        Button(onClick = onAck, colors = ButtonDefaults.buttonColors(containerColor = Red)) {
            Text("ACKNOWLEDGE", fontFamily = FontFamily.Monospace)
        }
    }
}
