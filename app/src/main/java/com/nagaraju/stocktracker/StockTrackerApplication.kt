package com.nagaraju.stocktracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation,
 * creating the top-level [SingletonComponent] that every [@Provides]/[@Binds]
 * across [core-network], [core-database], [data], and the feature modules
 * attaches to.
 *
 * No business logic lives here — this class exists solely to host the
 * annotation.
 */
@HiltAndroidApp
class StockTrackerApplication : Application()
