package com.normno.travelassistant.core.ai.di

import com.normno.travelassistant.core.ai.LlmProvider
import com.normno.travelassistant.core.ai.provider.OnDeviceLlmProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Провайдеры собираются в множество. Новый провайдер — одна строка `@Binds @IntoSet`;
 * ни реестр, ни экран настроек, ни ассистент при этом не меняются.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AiModule {

    @Binds
    @IntoSet
    fun onDevice(impl: OnDeviceLlmProvider): LlmProvider
}
