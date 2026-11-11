package com.holyszn.app.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HoloCard(
    modifier: Modifier = Modifier,
    hot: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val border = if (hot) Brush.linearGradient(listOf(Red, DeepRed))
    else Brush.linearGradient(listOf(Chrome1, ChromeDark, Chrome2))
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier.fillMaxWidth().clip(shape).background(Panel)
            .border(1.dp, border, shape).padding(14.dp),
        content = content
    )
}

@Composable
fun ScreenTitle(title: String, sub: String) {
    Column(Modifier.padding(bottom = 12.dp)) {
        Text(
            title,
            style = TextStyle(
                brush = ChromeBrush, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace, letterSpacing = 4.sp
            )
        )
        Text(sub, color = Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp)
    }
}

/** Returns a counter that changes every time the app returns to the foreground. */
@Composable
fun rememberResumeTick(): Int {
    var tick by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tick++ }
        owner.lifecycle.addObserver(o)
        onDispose { owner.lifecycle.removeObserver(o) }
    }
    return tick
}

/** HOLYVOICE input: uses the phone's speech recognizer (Google app) if installed. */
@Composable
fun MicButton(onText: (String) -> Unit) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val t = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (r.resultCode == Activity.RESULT_OK && !t.isNullOrBlank()) onText(t)
    }
    OutlinedButton(onClick = {
        try {
            launcher.launch(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(ctx, "No speech recognizer installed", Toast.LENGTH_SHORT).show()
        }
    }) { Text("VOICE") }
}

fun fmtTime(t: Long): String =
    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(t))

fun android.content.Context.findActivity(): Activity? {
    var c: android.content.Context = this
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
