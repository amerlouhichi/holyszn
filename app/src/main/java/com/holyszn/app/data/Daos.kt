package com.holyszn.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotifDao {
    @Insert suspend fun insert(n: NotifEntity)
    @Query("SELECT * FROM notifications ORDER BY time DESC LIMIT 200")
    fun recent(): Flow<List<NotifEntity>>
    @Query("SELECT * FROM notifications WHERE urgent = 1 ORDER BY time DESC LIMIT 5")
    fun recentUrgent(): Flow<List<NotifEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE time > :since")
    fun countSince(since: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM notifications WHERE urgent = 1 AND time > :since")
    fun urgentSince(since: Long): Flow<Int>
    @Query("DELETE FROM notifications") suspend fun clear()
    @Query("DELETE FROM notifications WHERE time < :before") suspend fun prune(before: Long)
}

@Dao
interface TaskDao {
    @Insert suspend fun insert(t: TaskEntity)
    @Update suspend fun update(t: TaskEntity)
    @Delete suspend fun delete(t: TaskEntity)
    @Query("SELECT * FROM tasks ORDER BY done ASC, created DESC")
    fun all(): Flow<List<TaskEntity>>
    @Query("SELECT COUNT(*) FROM tasks WHERE done = 0")
    fun openCount(): Flow<Int>
}

@Dao
interface MemoryDao {
    @Insert suspend fun insert(m: MemoryEntity)
    @Delete suspend fun delete(m: MemoryEntity)
    @Query("SELECT * FROM memories WHERE text LIKE '%' || :q || '%' ORDER BY created DESC")
    fun search(q: String): Flow<List<MemoryEntity>>
}
