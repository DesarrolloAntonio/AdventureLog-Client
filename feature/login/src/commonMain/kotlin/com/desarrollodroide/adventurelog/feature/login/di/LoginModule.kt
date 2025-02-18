package com.desarrollodroide.adventurelog.feature.login.di


import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.login.LoginViewModel

val loginModule = module {
    includes(domainModule)
    viewModelOf(::LoginViewModel)
}