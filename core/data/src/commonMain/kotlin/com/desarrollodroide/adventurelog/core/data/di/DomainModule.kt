package com.desarrollodroide.adventurelog.core.data.di

import com.desarrollodroide.adventurelog.core.common.di.commonModule
import com.desarrollodroide.adventurelog.core.network.di.networkModule
import org.koin.dsl.module

val dataModule = module {
    includes(commonModule, networkModule)

}