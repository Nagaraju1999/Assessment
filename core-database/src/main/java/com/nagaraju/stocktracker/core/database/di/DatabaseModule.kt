package com.nagaraju.stocktracker.core.database.di

import android.content.Context
import androidx.room.Room
import com.nagaraju.stocktracker.core.database.StockDatabase
import com.nagaraju.stocktracker.core.database.dao.AlertDao
import com.nagaraju.stocktracker.core.database.dao.WatchlistDao
import com.nagaraju.stocktracker.core.database.migration.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStockDatabase(
        @ApplicationContext context: Context,
    ): StockDatabase = Room.databaseBuilder(
        context,
        StockDatabase::class.java,
        StockDatabase.DATABASE_NAME,
    )
        .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    @Singleton
    fun provideWatchlistDao(database: StockDatabase): WatchlistDao =
        database.watchlistDao()

    @Provides
    @Singleton
    fun provideAlertDao(database: StockDatabase): AlertDao =
        database.alertDao()
}
