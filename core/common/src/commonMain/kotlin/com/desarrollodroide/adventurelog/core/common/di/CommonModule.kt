package com.desarrollodroide.adventurelog.core.common.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonModule = module {
    single(named(AdventureLogDispatchers.IO)) { provideIoDispatcher() }
    single(named(AdventureLogDispatchers.Default)) { Dispatchers.Default }
    single<CoroutineScope> {
        provideApplicationScope(get(named(AdventureLogDispatchers.Default)))
    }
}

expect fun provideIoDispatcher(): CoroutineDispatcher

fun provideApplicationScope(dispatcher: CoroutineDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + dispatcher)


enum class AdventureLogDispatchers {
    IO, Default
}