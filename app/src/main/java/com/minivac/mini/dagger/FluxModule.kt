package com.minivac.mini.dagger

import com.minivac.mini.flux.Dispatcher
import dagger.Module
import dagger.Provides

@Module
class FluxModule {

    @Provides @AppScope
    fun provideDispatcher() = Dispatcher()
}