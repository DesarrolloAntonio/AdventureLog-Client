package com.desarrollodroide.adventurelog.feature.calendar.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val calendarModule = module {
    includes(domainModule)
    viewModelOf(::CalendarViewModel)
}
