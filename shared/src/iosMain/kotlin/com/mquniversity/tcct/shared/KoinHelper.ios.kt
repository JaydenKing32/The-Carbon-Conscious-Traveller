package com.mquniversity.tcct.shared

import com.mquniversity.tcct.shared.cache.IOSDatabaseDriverFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class KoinHelper : KoinComponent {
    private val sdk: TripSdk by inject<TripSdk>()
    val tripViewModel: TripViewModel by inject<TripViewModel>()
}

fun initKoin() {
    setup()
    startKoin {
        modules(module {
            single<TripSdk> { TripSdk(IOSDatabaseDriverFactory()) }
            single<TripViewModel> { TripViewModel(sdk = get()) }
        })
    }
}
