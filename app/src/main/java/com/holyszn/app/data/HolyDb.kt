package com.holyszn.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NotifEntity::class, TaskEntity::class, MemoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HolyDb : RoomDatabase() {
    abstract fun notifDao(): NotifDao
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile private var inst: HolyDb? = null
        fun get(ctx: Context): HolyDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, HolyDb::class.java, "holyszn.db")
                .build().also { inst = it }
        }
    }
}
