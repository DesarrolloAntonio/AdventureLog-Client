package com.desarrollodroide.adventurelog.core.domain.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAdventuresPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllAdventuresUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveUserStatsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ReverseGeocodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedCitiesUseCase

val domainModule = module {
    includes(dataModule)

    factoryOf(::LoginUseCase)
    factoryOf(::GetAdventuresUseCase)
    factoryOf(::GetAdventureUseCase)
    factoryOf(::GetAdventuresPagingUseCase)
    factoryOf(::GetAllAdventuresUseCase)
    factoryOf(::GetCollectionsUseCase)
    factoryOf(::GetCollectionsPagingUseCase)
    factoryOf(::GetCollectionDetailUseCase)
    factoryOf(::InitializeSessionUseCase)
    factoryOf(::SaveSessionUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::RememberMeCredentialsUseCase)
    factoryOf(::CreateAdventureUseCase)
    factoryOf(::UpdateAdventureUseCase)
    factoryOf(::DeleteAdventureUseCase)
    factoryOf(::CreateCollectionUseCase)
    factoryOf(::UpdateCollectionUseCase)
    factoryOf(::DeleteCollectionUseCase)
    factoryOf(::GetCategoriesUseCase)
    factoryOf(::CreateCategoryUseCase)
    factoryOf(::UpdateCategoryUseCase)
    factoryOf(::DeleteCategoryUseCase)
    factoryOf(::GenerateDescriptionUseCase)
    factoryOf(::SearchLocationsUseCase)
    factoryOf(::ReverseGeocodeUseCase)
    factoryOf(::GetUserStatsUseCase)
    factoryOf(::ObserveUserStatsUseCase)
    factoryOf(::SearchWikipediaImageUseCase)
    factoryOf(::GetVisitedRegionsUseCase)
    factoryOf(::GetCountriesUseCase)
    factoryOf(::RefreshCountriesUseCase)
    factoryOf(::GetVisitedCitiesUseCase)
}