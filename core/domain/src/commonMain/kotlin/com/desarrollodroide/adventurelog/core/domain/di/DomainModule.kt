package com.desarrollodroide.adventurelog.core.domain.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

//    factoryOf(::LoginUseCase)
}