package com.desarrollodroide.adventurelog.feature.login.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel

val loginModule = module {
    includes(domainModule)
    // A ViewModel definition, not a factory: only this hands the ViewModel the SavedStateHandle
    // that carries a half-typed form through process death.
    viewModel {
        LoginViewModel(
            loginUseCase = get(),
            initializeSessionUseCase = get(),
            saveSessionUseCase = get(),
            rememberMeCredentialsUseCase = get(),
            savedStateHandle = get()
        )
    }
}