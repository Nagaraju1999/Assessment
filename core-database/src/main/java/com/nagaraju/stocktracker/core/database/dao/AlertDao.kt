package com.nagaraju.stocktracker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    /** Observes every configured alert, most recently created first. */
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    /**
     * Observes only enabled, not-yet-triggered alerts — exactly the set
     * the polling loop needs to evaluate on every price tick.
     */
    @Query("SELECT * FROM alerts WHERE isEnabled = 1 AND isTriggered = 0")
    fun observeActiveAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AlertEntity): Long

    @Update
    suspend fun update(entity: AlertEntity)

    @Delete
    suspend fun delete(entity: AlertEntity)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Flips [AlertEntity.isEnabled] without requiring the caller to fetch the full row first. */
    @Query("UPDATE alerts SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: Long, isEnabled: Boolean)

    /** Marks an alert as triggered so the polling loop stops re-firing it. */
    @Query("UPDATE alerts SET isTriggered = 1 WHERE id = :id")
    suspend fun markTriggered(id: Long)
}
