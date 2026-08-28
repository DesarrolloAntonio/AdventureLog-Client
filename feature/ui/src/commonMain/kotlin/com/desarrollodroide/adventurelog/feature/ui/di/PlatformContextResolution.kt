package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

/**
 * Where Coil's [PlatformContext] comes from, which is not the same question on every platform.
 *
 * On Android it is the Android `Context`, and Koin already holds one because the application
 * registers it with `androidContext()`. Everywhere else it is a singleton object that nobody
 * registers, so asking Koin for it throws `NoDefinitionFoundException` - which is what killed
 * the app the moment any screen with a ViewModel that touches files came into view.
 *
 * So the module supplies it instead of asking for it, and each platform answers for itself.
 */
expect fun Scope.resolvePlatformContext(): PlatformContext
