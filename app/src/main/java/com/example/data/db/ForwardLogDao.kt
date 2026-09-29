package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ForwardLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardLogDao {
    @Query("SELECT * FROM forward_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ForwardLog>>

    @Query("SELECT * FROM forward_logs WHERE isSuccess = 1 ORDER BY timestamp DESC")
    fun getSuccessfulLogs(): Flow<List<ForwardLog>>

    @Query("SELECT * FROM forward_logs WHERE isSuccess = 0 ORDER BY timestamp DESC")
    fun getFailedLogs(): Flow<List<ForwardLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ForwardLog): Long

    @Query("DELETE FROM forward_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM forward_logs")
    fun getTotalLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM forward_logs WHERE isSuccess = 1")
    fun getSuccessLogsCount(): Flow<Int>
}
