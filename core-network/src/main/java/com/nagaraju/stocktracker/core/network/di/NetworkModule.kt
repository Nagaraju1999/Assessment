package com.nagaraju.stocktracker.core.network.di

import com.nagaraju.stocktracker.core.network.api.FinnhubApi
import com.nagaraju.stocktracker.core.network.interceptor.AuthInterceptor
import com.nagaraju.stocktracker.core.network.interceptor.NetworkResultInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL      = "https://finnhub.io/api/v1/"
    private const val CONNECT_TIMEOUT_SEC = 15L
    private const val READ_TIMEOUT_SEC    = 30L

    /**
     * The API key itself is provided by [com.nagaraju.stocktracker.di.AppModule]
     * in the [app] module, which is the only module with access to
     * [BuildConfig.FINNHUB_API_KEY]. This module only *consumes* the
     * `@Named("finnhub_api_key")` binding — it must not also provide it,
     * or Hilt fails to compile with a duplicate-binding error.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @Named("finnhub_api_key") apiKey: String,
    ): AuthInterceptor = AuthInterceptor(apiKey)

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // Log full request + response bodies only in debug builds.
            // In release, OkHttp still attaches the interceptor but at NONE level,
            // so no sensitive data leaks to Logcat on production devices.
            level = if (isDebugBuild()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        // Auth must run first so the token is present before the result
        // interceptor checks the response code.
        .addInterceptor(authInterceptor)
        .addInterceptor(NetworkResultInterceptor())
        // Logging always last — captures the final request (with auth params).
        .addInterceptor(loggingInterceptor)
        .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideFinnhubApi(retrofit: Retrofit): FinnhubApi =
        retrofit.create(FinnhubApi::class.java)

    /** Detects debug builds without importing BuildConfig from the app module. */
    private fun isDebugBuild(): Boolean =
        runCatching {
            Class.forName("com.nagaraju.stocktracker.BuildConfig")
                .getField("DEBUG")
                .getBoolean(null)
        }.getOrDefault(false)
}
