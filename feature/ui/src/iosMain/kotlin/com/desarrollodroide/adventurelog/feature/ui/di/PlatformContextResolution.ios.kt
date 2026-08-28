package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.PlatformContext
import org.koin.core.scope.Scope

/** There is one of these on iOS and nothing to configure, so it needs no registration. */
actual fun Scope.resolvePlatformContext(): PlatformContext = PlatformContext.INSTANCE
