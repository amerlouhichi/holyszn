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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.holyszn.app.data.HolyDb
import com.holyszn.app.data.MemoryEntity
import kotlinx.coroutines.launch

@Composable
fun MemoryScreen() {
    val ctx = LocalContext.current
    val db = remember { HolyDb.get(ctx) }
    var query by remember { mutableStateOf("") }
    val mems by remember(query) { db.memoryDao().search(query) }.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }

    fun save(t: String) {
        val s = t.trim()
        if (s.isNotEmpty()) scope.launch { db.memoryDao().insert(MemoryEntity(text = s)) }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { ScreenTitle("HOLYMEMORY", "LOCAL ENCRYPTED-BY-ANDROID STORAGE") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    placeholder = { Text("Remember this…") }, modifier = Modifier.weight(1f)
                )
                MicButton { save(it) }
            }
        }
        item {
            Button(
                onClick = { save(input); input = "" },
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) { Text("SAVE MEMORY") }
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Search memories") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(mems, key = { it.id }) { m ->
            HoloCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(m.text)
                        Text(fmtTime(m.created), color = Chrome2, fontSize = 11.sp)
                    }
                    IconButton(onClick = { scope.launch { db.memoryDao().delete(m) } }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Red)
                    }
                }
            }
        }
    }
}
