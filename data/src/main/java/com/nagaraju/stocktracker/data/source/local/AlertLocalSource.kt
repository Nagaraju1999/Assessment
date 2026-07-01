package com.nagaraju.stocktracker.data.source.local

import com.nagaraju.stocktracker.core.database.dao.AlertDao
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlertLocalSource @Inject constructor(
    private val alertDao: AlertDao,
) {
    fun observeAlerts(): Flow<List<AlertEntity>> = alertDao.observeAll()

    fun observeActiveAlerts(): Flow<List<AlertEntity>> = alertDao.observeActiveAlerts()

    suspend fun insert(entity: AlertEntity): Long = alertDao.insert(entity)

    suspend fun delete(entity: AlertEntity) = alertDao.delete(entity)

    suspend fun deleteById(id: Long) = alertDao.deleteById(id)

    suspend fun setEnabled(id: Long, isEnabled: Boolean) = alertDao.setEnabled(id, isEnabled)

    suspend fun markTriggered(id: Long) = alertDao.markTriggered(id)
}
