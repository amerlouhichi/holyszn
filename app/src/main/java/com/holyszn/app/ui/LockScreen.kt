package com.holyszn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.util.Perms

@Composable
fun LockScreen(message: String, onAuth: () -> Unit) {
    val ctx = LocalContext.current
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "HOLYSZN",
            style = TextStyle(
                brush = ChromeBrush, fontSize = 40.sp, fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace, letterSpacing = 8.sp
            )
        )
        Text("IDENTITY LOCK", color = Red, fontSize = 12.sp,
            fontFamily = FontFamily.Monospace, letterSpacing = 4.sp)
        Spacer(Modifier.height(40.dp))
        Button(onClick = onAuth, colors = ButtonDefaults.buttonColors(containerColor = Red)) {
            Text("AUTHENTICATE", fontFamily = FontFamily.Monospace)
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Text(message, color = Chrome2, textAlign = TextAlign.Center, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { Perms.openSecuritySettings(ctx) }) { Text("SECURITY SETTINGS") }
        }
    }
}
