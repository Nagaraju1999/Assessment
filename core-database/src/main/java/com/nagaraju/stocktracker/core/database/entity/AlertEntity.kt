package com.nagaraju.stocktracker.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted record of a price alert the user has configured for a stock.
 *
 * @param id           Auto-generated primary key.
 * @param symbol       Exchange symbol this alert watches. e.g. "AAPL"
 * @param condition    Stored as a string ("ABOVE" / "BELOW") via [AlertConditionConverter]
 *                     would be redundant here — Room maps the enum directly using
 *                     [com.nagaraju.stocktracker.core.database.converter.Converters].
 * @param targetPrice  The threshold price that triggers this alert.
 * @param isEnabled    Whether this alert is actively evaluated during polling.
 * @param isTriggered  Whether the condition has already fired — prevents
 *                      re-notifying on every poll cycle once triggered.
 * @param createdAt    Unix epoch seconds when the alert was created.
 */
@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val symbol: String,
    val condition: AlertConditionEntity,
    val targetPrice: Double,
    val isEnabled: Boolean,
    val isTriggered: Boolean,
    val createdAt: Long,
)

/**
 * Database-level representation of the alert condition.
 * Mirrors the domain's `AlertCondition` enum but lives in this module so
 * [core-database] has no dependency on [domain].
 */
enum class AlertConditionEntity {
    ABOVE,
    BELOW,
}
