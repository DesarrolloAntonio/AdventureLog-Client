package com.desarrollodroide.adventurelog.feature.collections.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val collectionsModule = module {
    includes(domainModule)
    viewModelOf(::CollectionsViewModel)
}