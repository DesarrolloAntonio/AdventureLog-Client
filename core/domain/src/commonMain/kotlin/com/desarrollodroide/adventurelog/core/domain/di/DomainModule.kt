package com.desarrollodroide.adventurelog.core.domain.di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetAllCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionsPagingUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.InitializeSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveSessionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.LogoutUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.RememberMeCredentialsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLocationUseCase
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
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteTransportationUseCase

val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::GetLocationsUseCase)
    factoryOf(::GetLocationUseCase)
    factoryOf(::GetLocationsPagingUseCase)
    factoryOf(::GetAllLocationsUseCase)
    factoryOf(::GetCollectionsUseCase)
    factoryOf(::GetAllCollectionsUseCase)
    factoryOf(::GetCollectionsPagingUseCase)
    factoryOf(::ObserveCollectionsUseCase)
    factoryOf(::GetCollectionDetailUseCase)
    factoryOf(::InitializeSessionUseCase)
    factoryOf(::SaveSessionUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::RememberMeCredentialsUseCase)
    factoryOf(::CreateLocationUseCase)
    factoryOf(::UpdateLocationUseCase)
    factoryOf(::UpdateLocationCollectionsUseCase)
    factoryOf(::DeleteLocationUseCase)
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
    factoryOf(::CreateTransportationUseCase)
    factoryOf(::UpdateTransportationUseCase)
    factoryOf(::GetTransportationUseCase)
    factoryOf(::DeleteTransportationUseCase)
}