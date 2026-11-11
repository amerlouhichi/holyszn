package com.holyszn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.brief.HolyBrief
import com.holyszn.app.call.HolyCall
import com.holyszn.app.data.HolyDb
import com.holyszn.app.util.Perms
import com.holyszn.app.voice.HolyVoice

@Composable
fun CoreScreen() {
    val ctx = LocalContext.current
    val db = remember { HolyDb.get(ctx) }
    val since = remember { System.currentTimeMillis() - 24L * 3600 * 1000 }
    val total by remember { db.notifDao().countSince(since) }.collectAsState(0)
    val urgent by remember { db.notifDao().urgentSince(since) }.collectAsState(0)
    val open by remember { db.taskDao().openCount() }.collectAsState(0)
    val urgentList by remember { db.notifDao().recentUrgent() }.collectAsState(emptyList())
    val tick = rememberResumeTick()
    val armed = remember(tick) { Perms.listenerEnabled(ctx) }

    val voice = remember { HolyVoice(ctx) }
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }
    val brief = HolyBrief.compose(total, urgent, open, urgentList.firstOrNull())

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("HOLYCORE", if (armed) "SYSTEM ARMED • LISTENING" else "NOT ARMED • GRANT NOTIFICATION ACCESS") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("24H", total.toString(), Modifier.weight(1f))
                Stat("URGENT", urgent.toString(), Modifier.weight(1f), hot = urgent > 0)
                Stat("TASKS", open.toString(), Modifier.weight(1f))
            }
        }
        item {
            HoloCard {
                Text("HOLYBRIEF", color = Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(brief, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { voice.speak(brief) },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("SPEAK BRIEF") }
            }
        }
        item {
            HoloCard {
                Text("HOLYCALL", color = Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Text("Test the urgent alert (alarm sound + full-screen + voice).",
                    fontSize = 13.sp, color = Chrome2)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    HolyCall.fire(ctx, "Test alert", "This is a HOLYSZN urgent alert test.")
                }) { Text("FIRE TEST ALERT") }
            }
        }
        item { Text("LATEST URGENT", color = Chrome2, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
        if (urgentList.isEmpty()) {
            item { Text("None yet.", color = Chrome2, fontSize = 13.sp) }
        }
        items(urgentList) { n ->
            HoloCard(hot = true) {
                Text("${n.app} • ${fmtTime(n.time)} • ${n.score}", color = Red, fontSize = 11.sp)
                Text(n.title, fontWeight = FontWeight.Bold)
                if (n.text.isNotBlank()) Text(n.text, fontSize = 13.sp, color = Chrome2, maxLines = 3)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, hot: Boolean = false) {
    HoloCard(modifier, hot) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, fontSize = 30.sp, fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace, color = if (hot) Red else Chrome1)
            Text(label, fontSize = 10.sp, color = Chrome2, fontFamily = FontFamily.Monospace)
        }
    }
}
