package com.nagaraju.stocktracker.data.di

import android.content.Context
import com.nagaraju.stocktracker.core.common.dispatcher.DefaultDispatcherProvider
import com.nagaraju.stocktracker.core.common.dispatcher.DispatcherProvider
import com.nagaraju.stocktracker.core.common.network.NetworkMonitor
import com.nagaraju.stocktracker.data.repository.AlertRepositoryImpl
import com.nagaraju.stocktracker.data.repository.StockRepositoryImpl
import com.nagaraju.stocktracker.domain.repository.AlertRepository
import com.nagaraju.stocktracker.domain.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds each domain repository interface to its [data] module implementation.
 * [Binds] is preferred over [Provides] here since each implementation's
 * constructor is already [Inject]-annotated and Hilt only needs the
 * interface-to-implementation mapping, not a factory function.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindStockRepository(impl: StockRepositoryImpl): StockRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(impl: AlertRepositoryImpl): AlertRepository

    companion object {
        /**
         * [NetworkMonitor] takes a plain constructor (not [Inject]-annotated,
         * since [core-common] has no Hilt dependency by design) — provided
         * here instead of bound directly.
         */
        @Provides
        @Singleton
        fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
            NetworkMonitor(context)

        /**
         * [DispatcherProvider] is a plain interface (not [Inject]-annotated,
         * since [core-common] has no Hilt dependency by design) — bound to
         * its default implementation here.
         */
        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
    }
}
