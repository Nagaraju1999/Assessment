package com.nagaraju.stocktracker.di

import com.nagaraju.stocktracker.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

/**
 * Provides app-level bindings that depend on [BuildConfig] — which is only
 * available in the [app] module. This keeps [NetworkModule] free of any
 * direct [BuildConfig] reference, preserving module independence.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * The sole provider of the `@Named("finnhub_api_key")` binding that
     * [com.nagaraju.stocktracker.core.network.di.NetworkModule] consumes
     * (via [com.nagaraju.stocktracker.core.network.interceptor.AuthInterceptor]).
     *
     * This lives in [app] rather than [core-network] because [BuildConfig]
     * is only generated for the application module, not library modules —
     * keeping [core-network] free of any [app]-module dependency.
     */
    @Provides
    @Named("finnhub_api_key")
    fun provideApiKey(): String = BuildConfig.FINNHUB_API_KEY
}
