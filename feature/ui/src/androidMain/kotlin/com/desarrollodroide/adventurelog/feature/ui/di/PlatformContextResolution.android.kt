package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

/** `PlatformContext` is the Android `Context`, already registered by `androidContext()`. */
actual fun Scope.resolvePlatformContext(): PlatformContext = get()
