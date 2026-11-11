package com.holyszn.app.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.data.HolyDb
import com.holyszn.app.data.TaskEntity
import kotlinx.coroutines.launch

@Composable
fun FeedScreen() {
    val ctx = LocalContext.current
    val db = remember { HolyDb.get(ctx) }
    val feed by remember { db.notifDao().recent() }.collectAsState(emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("HOLYFEED", "NOTIFICATION STREAM • ANALYSED ON DEVICE") }
        item {
            OutlinedButton(onClick = { scope.launch { db.notifDao().clear() } }) { Text("CLEAR FEED") }
        }
        if (feed.isEmpty()) {
            item {
                Text("Nothing captured yet. Grant Notification Access in SETUP, then wait for a notification.",
                    color = Chrome2, fontSize = 13.sp)
            }
        }
        items(feed, key = { it.id }) { n ->
            HoloCard(hot = n.urgent) {
                Text(
                    "${n.app} • ${fmtTime(n.time)} • score ${n.score}" + if (n.urgent) " • URGENT" else "",
                    color = if (n.urgent) Red else Chrome2, fontSize = 11.sp
                )
                if (n.title.isNotBlank()) Text(n.title, fontWeight = FontWeight.Bold)
                if (n.text.isNotBlank()) Text(n.text, fontSize = 13.sp, maxLines = 4)
                TextButton(onClick = {
                    val t = n.title.ifBlank { n.app }.take(80)
                    scope.launch { db.taskDao().insert(TaskEntity(title = "Follow up: $t")) }
                    Toast.makeText(ctx, "Added to tasks", Toast.LENGTH_SHORT).show()
                }) { Text("ADD AS TASK", color = Red) }
            }
        }
    }
}
