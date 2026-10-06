package com.desarrollodroide.adventurelog.feature.ui.platform

/**
 * Whether this is an Apple platform.
 *
 * Used to decide whether Apple Maps is worth offering: on Android the same link opens a web page,
 * which is a worse answer than not offering it at all.
 */
expect val isApplePlatform: Boolean
