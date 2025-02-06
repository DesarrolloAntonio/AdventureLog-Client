package com.desarrollodroide.adventurelog.feature.adventures.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import com.desarrollodroide.adventurelog.feature.adventures.adventures.AdventuresViewModel

import org.koin.dsl.module

val adventureModule = module {
    includes(domainModule)
    viewModelOf(::AdventuresViewModel)
}