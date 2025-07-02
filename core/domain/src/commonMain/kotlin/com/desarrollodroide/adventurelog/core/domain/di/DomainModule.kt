package com.desarrollodroide.adventurelog.core.domain.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.GetAdventuresPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.LogoutUseCase
import com.desarrollodroide.adventurelog.core.domain.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.CreateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.UpdateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.CreateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.UpdateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.ReverseGeocodeUseCase

val domainModule = module {
    includes(dataModule)

    factoryOf(::LoginUseCase)
    factoryOf(::GetAdventuresUseCase)
    factoryOf(::GetAdventuresPagingUseCase)
    factoryOf(::GetCollectionsUseCase)
    factoryOf(::GetCollectionsPagingUseCase)
    factoryOf(::GetCollectionDetailUseCase)
    factoryOf(::InitializeSessionUseCase)
    factoryOf(::SaveSessionUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::RememberMeCredentialsUseCase)
    factoryOf(::CreateAdventureUseCase)
    factoryOf(::UpdateAdventureUseCase)
    factoryOf(::CreateCollectionUseCase)
    factoryOf(::UpdateCollectionUseCase)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::GenerateDescriptionUseCase)
    factoryOf(::SearchLocationsUseCase)
    factoryOf(::ReverseGeocodeUseCase)
}