package com.desarrollodroide.adventurelog

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.CalendarViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditChecklistViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditLodgingViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditNoteViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionDetailViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.CollectionsViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsViewModel
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
import com.desarrollodroide.adventurelog.feature.home.viewmodel.GlobalSearchViewModel
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LocationsViewModel
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import com.desarrollodroide.adventurelog.feature.map.viewmodel.MapViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.UsersViewModel
import com.desarrollodroide.adventurelog.feature.world.viewmodel.CountryDetailViewModel
import com.desarrollodroide.adventurelog.feature.world.viewmodel.WorldViewModel
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatform
import org.koin.viewmodel.resolveViewModel
import kotlin.reflect.KClass

/**
 * Owns the ViewModels of one SwiftUI screen, as a navigation back-stack entry does on Android:
 * a [ViewModelStore] they live in and a [SavedStateRegistry], which the ViewModels that take a
 * SavedStateHandle need in order to be built at all. Swift holds one per screen in a
 * `@StateObject` and calls [clear] when the screen goes, which runs `onCleared()` and cancels each
 * ViewModel's viewModelScope.
 *
 * Swift cannot name a KClass and sees method generics erased, so each ViewModel has its own typed
 * accessor below. A ViewModel that takes parameters gets one when its screen is built.
 */
class IosViewModelOwner : ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val controller = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry
    override val viewModelStore = ViewModelStore()

    init {
        controller.performAttach()
        controller.performRestore(null)
        enableSavedStateHandles()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    private val extras: CreationExtras = MutableCreationExtras().apply {
        set(SAVED_STATE_REGISTRY_OWNER_KEY, this@IosViewModelOwner)
        set(VIEW_MODEL_STORE_OWNER_KEY, this@IosViewModelOwner)
    }

    @OptIn(KoinInternalApi::class)
    fun <T : ViewModel> get(kClass: KClass<T>): T = resolveViewModel(
        vmClass = kClass,
        viewModelStore = viewModelStore,
        extras = extras,
        scope = KoinPlatform.getKoin().scopeRegistry.rootScope,
    )

    /** Ends every ViewModel this screen made. */
    fun clear() {
        presenters.clear()
        viewModelStore.clear()
    }

    fun loginViewModel(): LoginViewModel = get(LoginViewModel::class)
    fun homeViewModel(): HomeViewModel = get(HomeViewModel::class)
    fun globalSearchViewModel(): GlobalSearchViewModel = get(GlobalSearchViewModel::class)
    fun locationsViewModel(): LocationsViewModel = get(LocationsViewModel::class)
    fun adventureDetailViewModel(): AdventureDetailViewModel = get(AdventureDetailViewModel::class)
    fun collectionsViewModel(): CollectionsViewModel = get(CollectionsViewModel::class)
    fun collectionDetailViewModel(): CollectionDetailViewModel = get(CollectionDetailViewModel::class)
    fun recommendationsViewModel(): RecommendationsViewModel = get(RecommendationsViewModel::class)
    fun addEditNoteViewModel(): AddEditNoteViewModel = get(AddEditNoteViewModel::class)
    fun addEditChecklistViewModel(): AddEditChecklistViewModel = get(AddEditChecklistViewModel::class)
    fun addEditLodgingViewModel(): AddEditLodgingViewModel = get(AddEditLodgingViewModel::class)
    fun mapViewModel(): MapViewModel = get(MapViewModel::class)
    fun calendarViewModel(): CalendarViewModel = get(CalendarViewModel::class)
    fun worldViewModel(): WorldViewModel = get(WorldViewModel::class)
    fun countryDetailViewModel(): CountryDetailViewModel = get(CountryDetailViewModel::class)
    fun settingsViewModel(): SettingsViewModel = get(SettingsViewModel::class)
    fun usersViewModel(): UsersViewModel = get(UsersViewModel::class)

    private val presenters = mutableMapOf<String, PagingListPresenter<*>>()

    /** The places list, paged; one per owner, collecting in the LocationsViewModel's scope. */
    @Suppress("UNCHECKED_CAST")
    fun locationsPresenter(): PagingListPresenter<Location> = presenters.getOrPut("locations") {
        val vm = locationsViewModel()
        PagingListPresenter(vm.adventuresPagingData, vm.viewModelScope)
    } as PagingListPresenter<Location>

    /** The signed-in user, or null once signed out or the session is lost: the app's root gate. */
    fun sessionFlow(): Flow<UserDetails?> = KoinPlatform.getKoin().get<UserRepository>().getUserSession()
}
