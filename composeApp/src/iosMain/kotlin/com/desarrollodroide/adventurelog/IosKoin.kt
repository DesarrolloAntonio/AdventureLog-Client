package com.desarrollodroide.adventurelog

import com.desarrollodroide.adventurelog.di.appModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Starts Koin for the iOS app with the shared [appModule]. Swift calls it once at launch; a second
 * call is a no-op, so it is safe wherever it ends up being called from.
 */
fun doInitKoin() {
    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin { modules(appModule) }
}
