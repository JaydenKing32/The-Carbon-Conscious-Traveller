package com.mquniversity.tcct

import com.mquniversity.tcct.shared.TripSdk
import com.mquniversity.tcct.shared.TripViewModel
import com.mquniversity.tcct.shared.cache.AndroidDatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<TripSdk> { TripSdk(AndroidDatabaseDriverFactory(androidContext())) }
    viewModel { TripViewModel(sdk = get()) }
}
