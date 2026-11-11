package com.holyszn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.assistant.HolyAssistant
import com.holyszn.app.brief.HolyBrief
import com.holyszn.app.call.HolyCall
import com.holyszn.app.data.HolyDb
import com.holyszn.app.util.Perms
import com.holyszn.app.voice.HolyVoice
import kotlinx.coroutines.launch

@Composable
fun CoreScreen() {
    val ctx = LocalContext.current
    val db = remember { HolyDb.get(ctx) }
    val scope = rememberCoroutineScope()
    val since = remember { System.currentTimeMillis() - 24L * 3600 * 1000 }
    val total by remember { db.notifDao().countSince(since) }.collectAsState(0)
    val urgent by remember { db.notifDao().urgentSince(since) }.collectAsState(0)
    val open by remember { db.taskDao().openCount() }.collectAsState(0)
    val urgentList by remember { db.notifDao().recentUrgent() }.collectAsState(emptyList())
    val tasks by remember { db.taskDao().all() }.collectAsState(emptyList())
    val tick = rememberResumeTick()
    val armed = remember(tick) { Perms.listenerEnabled(ctx) }

    val voice = remember { HolyVoice(ctx) }
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }
    val brief = HolyBrief.compose(total, urgent, open, urgentList.firstOrNull())

    var heard by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("Say or type a command. Try: add task buy bread") }
    var typed by remember { mutableStateOf("") }
    fun run(cmd: String) {
        scope.launch {
            heard = cmd
            val r = HolyAssistant.handle(ctx, db, cmd)
            reply = r
            voice.speak(r)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                ScreenTitle("HOLYCORE", if (armed) "SYSTEM ARMED • LISTENING" else "NOT ARMED • GRANT NOTIFICATION ACCESS")
            }
        }
        item { ArcReactor(armed) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("24H", total.toString(), Modifier.weight(1f))
                Stat("URGENT", urgent.toString(), Modifier.weight(1f), hot = urgent > 0)
                Stat("TASKS", open.toString(), Modifier.weight(1f))
            }
        }
        item {
            HoloCard(hot = true) {
                Text("TALK TO HOLY", color = Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                if (heard.isNotBlank()) Text("You: $heard", color = Chrome2, fontSize = 13.sp)
                Text(reply, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = typed, onValueChange = { typed = it },
                        placeholder = { Text("Type a command") }, singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    MicButton { run(it) }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { if (typed.isNotBlank()) { run(typed); typed = "" } },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("SEND") }
            }
        }
        item {
            HoloCard {
                Text("TODAY'S MISSIONS", color = Red, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                val top = tasks.filter { !it.done }.take(3)
                if (top.isEmpty()) Text("No open tasks. Add one above or in TASKS.", color = Chrome2, fontSize = 13.sp)
                top.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { scope.launch { db.taskDao().update(t.copy(done = true)) } },
                            colors = CheckboxDefaults.colors(checkedColor = Red)
                        )
                        Text(t.title)
                    }
                }
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
                Text("Test the urgent alert (alarm sound + full-screen + voice).", fontSize = 13.sp, color = Chrome2)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    HolyCall.fire(ctx, "Test alert", "This is a HOLYSZN urgent alert test.")
                }) { Text("FIRE TEST ALERT") }
            }
        }
        item {
            Text("LATEST URGENT", color = Chrome2, fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth())
        }
        if (urgentList.isEmpty()) {
            item { Text("None yet.", color = Chrome2, fontSize = 13.sp, modifier = Modifier.fillMaxWidth()) }
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
