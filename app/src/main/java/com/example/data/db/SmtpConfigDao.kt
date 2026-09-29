package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SmtpConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface SmtpConfigDao {
    @Query("SELECT * FROM smtp_config WHERE id = 1 LIMIT 1")
    fun getSmtpConfigFlow(): Flow<SmtpConfig?>

    @Query("SELECT * FROM smtp_config WHERE id = 1 LIMIT 1")
    suspend fun getSmtpConfig(): SmtpConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSmtpConfig(config: SmtpConfig)
}
