package com.desarrollodroide.adventurelog.core.domain.usecase

/**
 * What every screen says when a request never reaches the server. The app had four wordings for it -
 * "No internet connection.", "Network unavailable", "Network error" and this one - and the first
 * three blame the phone's network, which is usually fine: a sleeping server, a VPN that is off or a
 * timeout all land here too (QA, owner's call 2026-10-06). Signing in keeps its own, which also
 * names the address that was typed.
 */
const val CANT_REACH_SERVER = "Can't reach the server. Check your connection."
