package com.desarrollodroide.adventurelog.core.domain.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.ClearSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.LogoutUseCase

val domainModule = module {
    includes(dataModule)

    factoryOf(::LoginUseCase)
    factoryOf(::GetAdventuresUseCase)
    factoryOf(::InitializeSessionUseCase)
    factoryOf(::ClearSessionUseCase)
    factoryOf(::SaveSessionUseCase)
    factoryOf(::LogoutUseCase)
}