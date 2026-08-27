package com.desarrollodroide.adventurelog.feature.home.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.home.viewmodel.GlobalSearchViewModel

val homeModule = module {
    includes(domainModule)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GlobalSearchViewModel)
}