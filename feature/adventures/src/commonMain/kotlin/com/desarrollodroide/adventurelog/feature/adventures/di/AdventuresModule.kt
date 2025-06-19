package com.desarrollodroide.adventurelog.feature.adventures.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AdventuresViewModel
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditAdventureViewModel

import org.koin.dsl.module

val adventureModule = module {
    includes(domainModule)
    viewModelOf(::AdventuresViewModel)
    viewModelOf(::AddEditAdventureViewModel)
}