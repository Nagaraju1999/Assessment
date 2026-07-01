package com.nagaraju.stocktracker.feature.alerts.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nagaraju.stocktracker.domain.model.AlertCondition
import com.nagaraju.stocktracker.domain.model.StockAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "price_alerts"
private const val CHANNEL_NAME = "Price Alerts"
private const val CHANNEL_DESCRIPTION = "Notifies you when a stock reaches your target price"

/**
 * Posts a system notification when a [StockAlert] triggers, deep-linking
 * back into the app's stock details screen for that symbol via the
 * `stocktracker://stock/{symbol}` URI registered in the manifest.
 *
 * Lives in [feature-alerts] (rather than [core-common]) since notification
 * content — title, body copy, the deep link target — is specific to this
 * feature's domain concept of an "alert," not a generic cross-app utility.
 */
@Singleton
class AlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    init {
        createNotificationChannelIfNeeded()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    /**
     * Posts the triggered-alert notification. Silently no-ops if the user
     * has not granted POST_NOTIFICATIONS (Android 13+) — the in-app
     * Snackbar event still fires regardless, so the user isn't left with
     * zero feedback.
     */
    fun notifyTriggered(alert: StockAlert) {
        val content = buildNotificationContent(alert)

        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("stocktracker://stock/${alert.symbol}"),
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.toInt(),
            deepLinkIntent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(alert.id.toInt(), notification)
        }
    }
}

/** Title and body text for a triggered-alert notification. */
data class AlertNotificationContent(val title: String, val body: String)

/**
 * Builds the notification's title and body text for [alert].
 *
 * Extracted as a pure top-level function (no [Context] dependency) so this
 * piece of the otherwise hard-to-unit-test [AlertNotifier] — the only part
 * with real conditional logic — can be tested directly without mocking
 * Android's notification framework.
 */
fun buildNotificationContent(alert: StockAlert): AlertNotificationContent {
    val conditionText = when (alert.condition) {
        AlertCondition.ABOVE -> "risen above"
        AlertCondition.BELOW -> "fallen below"
    }
    return AlertNotificationContent(
        title = "${alert.symbol} price alert",
        body = "${alert.symbol} has $conditionText $${alert.targetPrice}",
    )
}
