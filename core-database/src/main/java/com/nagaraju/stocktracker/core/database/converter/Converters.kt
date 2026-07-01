package com.nagaraju.stocktracker.core.database.converter

import androidx.room.TypeConverter
import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity

/**
 * Room cannot persist enum types natively — this converter stores
 * [AlertConditionEntity] as its String name and reconstructs it on read.
 */
class Converters {

    @TypeConverter
    fun fromAlertCondition(condition: AlertConditionEntity): String = condition.name

    @TypeConverter
    fun toAlertCondition(value: String): AlertConditionEntity =
        AlertConditionEntity.valueOf(value)
}
