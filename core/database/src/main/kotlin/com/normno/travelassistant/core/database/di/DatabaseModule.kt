package com.normno.travelassistant.core.database.di

import android.content.Context
import androidx.room.Room
import com.normno.travelassistant.core.database.TravelDatabase
import com.normno.travelassistant.core.database.dao.DayDao
import com.normno.travelassistant.core.database.dao.ForkDao
import com.normno.travelassistant.core.database.dao.PlaceDao
import com.normno.travelassistant.core.database.dao.StopDao
import com.normno.travelassistant.core.database.dao.TripDao
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
    fun database(@ApplicationContext context: Context): TravelDatabase =
        Room.databaseBuilder(context, TravelDatabase::class.java, TravelDatabase.NAME)
            .build()

    @Provides fun tripDao(db: TravelDatabase): TripDao = db.tripDao()
    @Provides fun dayDao(db: TravelDatabase): DayDao = db.dayDao()
    @Provides fun stopDao(db: TravelDatabase): StopDao = db.stopDao()
    @Provides fun placeDao(db: TravelDatabase): PlaceDao = db.placeDao()
    @Provides fun forkDao(db: TravelDatabase): ForkDao = db.forkDao()
}
