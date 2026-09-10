package com.desarrollodroide.adventurelog.feature.settings.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.UsersViewModel
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupExporter
import com.desarrollodroide.adventurelog.feature.settings.domain.RealBackupExporter

val settingsModule = module {
    single<BackupExporter> { RealBackupExporter(downloader = get(), files = get()) }
    includes(domainModule)
    viewModel {
        SettingsViewModel(
            settingsRepository = get(),
            userRepository = get(),
            accountRepository = get(),
            refreshVisitedRegionsUseCase = get(),
            backupExporter = get()
        )
    }
    viewModelOf(::UsersViewModel)
}