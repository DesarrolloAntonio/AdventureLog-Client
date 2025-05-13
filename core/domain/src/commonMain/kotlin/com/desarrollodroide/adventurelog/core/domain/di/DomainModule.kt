package com.desarrollodroide.adventurelog.core.domain.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase

val domainModule = module {
    includes(dataModule)

    factoryOf(::LoginUseCase)
    factoryOf(::GetAdventuresUseCase)
}