package com.nagaraju.stocktracker.domain.model

/**
 * A user-configured price alert for a stock.
 *
 * @param id           Database-assigned identifier. `0L` for a not-yet-persisted alert.
 * @param symbol       Exchange symbol this alert watches.
 * @param condition    Whether the alert fires when price goes [AlertCondition.ABOVE]
 *                      or [AlertCondition.BELOW] [targetPrice].
 * @param targetPrice  The threshold price.
 * @param isEnabled    Whether this alert is actively evaluated during polling.
 * @param isTriggered  Whether the condition has already fired once.
 * @param createdAt    Unix epoch seconds when the alert was created.
 */
data class StockAlert(
    val id: Long = 0L,
    val symbol: String,
    val condition: AlertCondition,
    val targetPrice: Double,
    val isEnabled: Boolean,
    val isTriggered: Boolean,
    val createdAt: Long,
) {
    /**
     * Evaluates whether [currentPrice] satisfies this alert's condition.
     * Pure business logic — testable without any Android or database dependency.
     */
    fun isSatisfiedBy(currentPrice: Double): Boolean = when (condition) {
        AlertCondition.ABOVE -> currentPrice >= targetPrice
        AlertCondition.BELOW -> currentPrice <= targetPrice
    }
}

enum class AlertCondition {
    ABOVE,
    BELOW,
}
