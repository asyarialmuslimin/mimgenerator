package com.saifur.mimgenerator.di

import android.app.Application
import com.saifur.mimgenerator.data.repository.memeRepoModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApp)
            modules(listOf(networkModule, viewModelModule, memeRepoModule))
        }
    }
}