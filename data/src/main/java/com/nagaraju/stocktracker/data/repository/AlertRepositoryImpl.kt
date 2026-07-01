package com.nagaraju.stocktracker.data.repository

import com.nagaraju.stocktracker.core.database.entity.AlertEntity
import com.nagaraju.stocktracker.data.mapper.toDomain
import com.nagaraju.stocktracker.data.mapper.toEntity
import com.nagaraju.stocktracker.data.source.local.AlertLocalSource
import com.nagaraju.stocktracker.data.source.remote.StockRemoteSource
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.SmartPollingCalculator
import com.nagaraju.stocktracker.domain.model.StockAlert
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import com.nagaraju.stocktracker.domain.result.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val localSource: AlertLocalSource,
    private val remoteSource: StockRemoteSource,
) : AlertRepository {

    override fun observeAlerts(): Flow<List<StockAlert>> =
        localSource.observeAlerts().map { entities -> entities.map { it.toDomain() } }

    override suspend fun createAlert(
        symbol: String,
        condition: AlertCondition,
        targetPrice: Double,
    ): Result<Long> = runCatching {
        val entity = AlertEntity(
            symbol      = symbol,
            condition   = condition.toEntity(),
            targetPrice = targetPrice,
            isEnabled   = true,
            isTriggered = false,
            createdAt   = System.currentTimeMillis() / 1_000L,
        )
        Result.Success(localSource.insert(entity))
    }.getOrElse { Result.Error(it) }

    override suspend fun deleteAlert(alertId: Long): Result<Unit> =
        runCatching {
            localSource.deleteById(alertId)
            Result.Success(Unit)
        }.getOrElse { Result.Error(it) }

    override suspend fun setAlertEnabled(alertId: Long, isEnabled: Boolean): Result<Unit> =
        runCatching {
            localSource.setEnabled(alertId, isEnabled)
            Result.Success(Unit)
        }.getOrElse { Result.Error(it) }

    /**
     * Evaluates every active (enabled, not-yet-triggered) alert on its own
     * independent, adaptive schedule rather than checking the whole set on
     * one shared tick.
     *
     * Each alert gets its own coroutine loop: fetch a quote, check
     * [StockAlert.isSatisfiedBy], and — if not yet satisfied — sleep for
     * [SmartPollingCalculator.nextIntervalMillis] before checking again.
     * An alert whose price is far from its target sleeps up to five
     * minutes between checks; one whose price is close tightens down to
     * fifteen seconds. This is what keeps a watchlist of many alerts from
     * linearly multiplying network calls the way a single shared
     * [pollIntervalMillis] tick would.
     *
     * [pollIntervalMillis] is still accepted — used as the *initial* check
     * delay for a newly-active alert before its first real quote arrives —
     * so the public [AlertRepository.observeTriggeredAlerts] contract is
     * unchanged for callers that don't care about the adaptive behavior.
     *
     * New alerts becoming active, or existing ones being deleted/disabled,
     * are picked up automatically, since the outer collector launches a
     * new per-alert loop the moment an alert appears in the active set and
     * lets a loop exit naturally once its alert leaves that set.
     */
    override fun observeTriggeredAlerts(pollIntervalMillis: Long): Flow<StockAlert> = channelFlow {
        val runningAlertIds = mutableSetOf<Long>()

        suspend fun evaluateUntilTriggeredOrInactive(alertId: Long) {
            while (true) {
                val entity = localSource.observeActiveAlerts().first().find { it.id == alertId } ?: return
                val alert = entity.toDomain()

                val currentPrice = runCatching { remoteSource.getQuote(alert.symbol).currentPrice }.getOrNull()
                if (currentPrice == null) {
                    // Quote fetch failed (offline, rate-limited, etc.) — back
                    // off to the slowest cadence rather than retrying tightly.
                    delay(SmartPollingCalculator.MAX_INTERVAL_MS)
                    continue
                }

                if (alert.isSatisfiedBy(currentPrice)) {
                    localSource.markTriggered(alertId)
                    send(alert.copy(isTriggered = true))
                    return
                }

                delay(SmartPollingCalculator.nextIntervalMillis(alert, currentPrice))
            }
        }

        localSource.observeActiveAlerts().collect { activeEntities ->
            val activeIds = activeEntities.map { it.id }.toSet()
            runningAlertIds.retainAll(activeIds)

            for (entity in activeEntities) {
                if (entity.id in runningAlertIds) continue
                runningAlertIds += entity.id

                launch {
                    delay(pollIntervalMillis.coerceAtMost(SmartPollingCalculator.MIN_INTERVAL_MS))
                    evaluateUntilTriggeredOrInactive(entity.id)
                }
            }
        }
    }
}
