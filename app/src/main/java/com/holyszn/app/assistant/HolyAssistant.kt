package com.holyszn.app.assistant

import android.content.Context
import com.holyszn.app.brief.HolyBrief
import com.holyszn.app.data.HolyDb
import com.holyszn.app.data.MemoryEntity
import com.holyszn.app.data.TaskEntity
import kotlinx.coroutines.flow.first

/**
 * HOLY assistant: an on-device command interpreter (English, Italian, French, Arabic keywords).
 * It is rule-based, not a cloud AI model. Nothing is sent anywhere.
 */
object HolyAssistant {
    private val addPrefixes = listOf(
        "add a task", "add task", "new task", "create task", "remind me to", "aggiungi attività",
        "aggiungi", "ajoute tâche", "ajoute", "أضف مهمة", "أضف", "task", "to do"
    )
    private val rememberPrefixes = listOf(
        "remember that", "remember", "ricorda che", "ricorda", "souviens-toi de", "souviens-toi",
        "rappelle-moi", "تذكر"
    )
    private val donePrefixes = listOf("mark done", "done", "complete", "finish", "fatto", "terminé", "تم")

    private fun has(low: String, vararg w: String) = w.any { low.contains(it) }

    suspend fun handle(ctx: Context, db: HolyDb, raw: String): String {
        val input = raw.trim()
        val low = input.lowercase()
        if (low.isEmpty()) return "I didn't catch that."

        fun after(prefixes: List<String>): String? {
            for (p in prefixes) if (low.startsWith(p)) {
                return input.substring(p.length).trim().trimStart(':', ',', '-').trim()
            }
            return null
        }

        after(addPrefixes)?.let {
            if (it.isEmpty()) return "What should the task be?"
            db.taskDao().insert(TaskEntity(title = it))
            return "Task added: $it"
        }
        after(rememberPrefixes)?.let {
            if (it.isEmpty()) return "What should I remember?"
            db.memoryDao().insert(MemoryEntity(text = it))
            return "Saved to memory: $it"
        }
        after(donePrefixes)?.let { q ->
            if (q.isEmpty()) return "Which task is done?"
            val t = db.taskDao().all().first().firstOrNull { !it.done && it.title.contains(q, ignoreCase = true) }
                ?: return "I couldn't find an open task matching $q."
            db.taskDao().update(t.copy(done = true))
            return "Marked done: ${t.title}"
        }

        val since = System.currentTimeMillis() - 24L * 3600 * 1000
        val n = db.notifDao()
        return when {
            has(low, "brief", "summary", "riepilogo", "résumé", "resume", "ملخص") -> {
                val open = db.taskDao().openCount().first()
                HolyBrief.compose(
                    n.countSince(since).first(), n.urgentSince(since).first(), open,
                    n.recentUrgent().first().firstOrNull()
                )
            }
            has(low, "urgent", "urgenti", "عاجل") -> {
                val l = n.recentUrgent().first().take(3)
                if (l.isEmpty()) "No urgent notifications."
                else "Urgent: " + l.joinToString(". ") { "${it.app}, ${it.title}" }
            }
            has(low, "notification", "notifiche", "notifs", "إشعارات") -> {
                val l = n.recent().first().take(3)
                if (l.isEmpty()) "No notifications captured yet."
                else "Latest: " + l.joinToString(". ") { "${it.app}, ${it.title}" }
            }
            has(low, "task", "to do", "todo", "attività", "tâche", "مهام") -> {
                val l = db.taskDao().all().first().filter { !it.done }.take(5)
                if (l.isEmpty()) "You have no open tasks."
                else "You have ${l.size} open tasks: " + l.joinToString(". ") { it.title }
            }
            has(low, "hello", "hi ", "hey", "ciao", "salut", "bonjour", "مرحبا") || low == "hi" ->
                "Hello. I'm HOLY. Ask for your brief, urgent items or tasks, or say add task, followed by what to do."
            else -> "I can add tasks, save memories, mark tasks done, and read your brief, urgent items, notifications and tasks. Try: add task buy bread."
        }
    }
}
