package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ForwardRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM forward_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<ForwardRule>>

    @Query("SELECT * FROM forward_rules WHERE isEnabled = 1 ORDER BY id ASC")
    suspend fun getActiveRules(): List<ForwardRule>

    @Query("SELECT * FROM forward_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): ForwardRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ForwardRule): Long

    @Update
    suspend fun updateRule(rule: ForwardRule)

    @Delete
    suspend fun deleteRule(rule: ForwardRule)

    @Query("UPDATE forward_rules SET matchesCount = matchesCount + 1, lastMatchedAt = :timestamp WHERE id = :ruleId")
    suspend fun incrementRuleMatchCount(ruleId: Long, timestamp: Long)

    @Query("UPDATE forward_rules SET isEnabled = :isEnabled WHERE id = :ruleId")
    suspend fun setRuleEnabled(ruleId: Long, isEnabled: Boolean)
}
