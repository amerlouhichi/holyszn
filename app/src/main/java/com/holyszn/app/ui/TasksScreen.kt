package com.holyszn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.holyszn.app.data.HolyDb
import com.holyszn.app.data.TaskEntity
import kotlinx.coroutines.launch

@Composable
fun TasksScreen() {
    val ctx = LocalContext.current
    val db = remember { HolyDb.get(ctx) }
    val tasks by remember { db.taskDao().all() }.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    fun add(t: String) {
        val s = t.trim()
        if (s.isNotEmpty()) scope.launch { db.taskDao().insert(TaskEntity(title = s)) }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("HOLYTASK", "MISSION LIST") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text("New task") }, singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                MicButton { add(it) }
            }
        }
        item {
            Button(
                onClick = { add(input); input = "" },
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) { Text("ADD TASK") }
        }
        items(tasks, key = { it.id }) { t ->
            HoloCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = t.done,
                        onCheckedChange = { scope.launch { db.taskDao().update(t.copy(done = it)) } },
                        colors = CheckboxDefaults.colors(checkedColor = Red)
                    )
                    Text(
                        t.title, Modifier.weight(1f),
                        textDecoration = if (t.done) TextDecoration.LineThrough else null,
                        color = if (t.done) Chrome2 else Chrome1
                    )
                    IconButton(onClick = { scope.launch { db.taskDao().delete(t) } }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Red)
                    }
                }
            }
        }
    }
}
