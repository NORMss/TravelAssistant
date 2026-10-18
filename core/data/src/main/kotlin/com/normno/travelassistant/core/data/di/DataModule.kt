package com.normno.travelassistant.core.data.di

import com.normno.travelassistant.core.common.time.SystemTripClock
import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.data.repository.ItineraryRepository
import com.normno.travelassistant.core.data.repository.OfflineFirstItineraryRepository
import com.normno.travelassistant.core.data.repository.BuiltInTemplateRepository
import com.normno.travelassistant.core.data.repository.OfflineFirstTripRepository
import com.normno.travelassistant.core.data.repository.TemplateRepository
import com.normno.travelassistant.core.data.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun tripRepository(impl: OfflineFirstTripRepository): TripRepository

    @Binds
    @Singleton
    fun itineraryRepository(impl: OfflineFirstItineraryRepository): ItineraryRepository

    @Binds
    @Singleton
    fun templateRepository(impl: BuiltInTemplateRepository): TemplateRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    /**
     * Часы инжектируются, а не берутся из `Clock.System`. Это не академизм: тест на
     * экран «Сейчас» обязан уметь сказать «сейчас 05:12 второго дня поездки», и
     * режим «предпросмотр времени» в настройках работает ровно через подмену часов.
     */
    @Provides
    @Singleton
    fun tripClock(): TripClock = SystemTripClock()
}
