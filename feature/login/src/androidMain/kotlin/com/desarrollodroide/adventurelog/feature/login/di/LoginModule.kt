package com.desarrollodroide.adventurelog.feature.login.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.login.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    includes(domainModule)
    viewModelOf(::LoginViewModel)
}