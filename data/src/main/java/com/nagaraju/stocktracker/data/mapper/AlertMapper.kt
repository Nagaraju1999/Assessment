package com.nagaraju.stocktracker.data.mapper

import com.nagaraju.stocktracker.core.database.entity.AlertConditionEntity
import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert

fun AlertEntity.toDomain(): StockAlert = StockAlert(
    id          = id,
    symbol      = symbol,
    condition   = condition.toDomain(),
    targetPrice = targetPrice,
    isEnabled   = isEnabled,
    isTriggered = isTriggered,
    createdAt   = createdAt,
)

fun StockAlert.toEntity(): AlertEntity = AlertEntity(
    id          = id,
    symbol      = symbol,
    condition   = condition.toEntity(),
    targetPrice = targetPrice,
    isEnabled   = isEnabled,
    isTriggered = isTriggered,
    createdAt   = createdAt,
)

fun AlertConditionEntity.toDomain(): AlertCondition = when (this) {
    AlertConditionEntity.ABOVE -> AlertCondition.ABOVE
    AlertConditionEntity.BELOW -> AlertCondition.BELOW
}

fun AlertCondition.toEntity(): AlertConditionEntity = when (this) {
    AlertCondition.ABOVE -> AlertConditionEntity.ABOVE
    AlertCondition.BELOW -> AlertConditionEntity.BELOW
}
