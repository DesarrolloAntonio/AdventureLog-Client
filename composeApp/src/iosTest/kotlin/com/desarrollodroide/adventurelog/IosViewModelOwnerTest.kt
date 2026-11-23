package com.desarrollodroide.adventurelog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * One owner per SwiftUI screen: clearing it is what ends the screen's ViewModels (their
 * viewModelScope with them), and it must be able to build the ViewModels that take a
 * SavedStateHandle, which plain Koin lookups could not in the bridge spike.
 */
class IosViewModelOwnerTest {

    class Flagging : ViewModel() {
        var cleared = false
        override fun onCleared() { cleared = true }
    }

    class Saving(val handle: SavedStateHandle) : ViewModel()

    @BeforeTest
    fun start() {
        startKoin { modules(module { viewModel { Flagging() }; viewModel { Saving(get()) } }) }
    }

    @AfterTest
    fun stop() = stopKoin()

    @Test
    fun clearRunsOnCleared() {
        val owner = IosViewModelOwner()
        val vm = owner.get(Flagging::class)

        owner.clear()

        assertTrue(vm.cleared)
    }

    @Test
    fun theSameOwnerGivesTheSameViewModel() {
        val owner = IosViewModelOwner()
        assertTrue(owner.get(Flagging::class) === owner.get(Flagging::class))
        owner.clear()
    }

    @Test
    fun viewModelsThatTakeASavedStateHandleCanBeBuilt() {
        val owner = IosViewModelOwner()
        val vm = owner.get(Saving::class)

        vm.handle["k"] = "v"

        assertEquals("v", vm.handle.get<String>("k"))
        owner.clear()
    }
}
