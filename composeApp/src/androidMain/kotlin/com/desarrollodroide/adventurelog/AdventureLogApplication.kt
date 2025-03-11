package com.desarrollodroide.adventurelog

import android.app.Application
import com.desarrollodroide.adventurelog.di.appModule
import com.desarrollodroide.adventurelog.feature.settings.platform.AndroidPlatformActions
import com.desarrollodroide.adventurelog.feature.settings.platform.PlatformActionsProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AdventureLogApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        
        PlatformActionsProvider.setPlatformActions(AndroidPlatformActions(this))
        
        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(appModule)
        }
    }

}