package com.desarrollodroide.adventurelog.core.domain.repository

/**
 * A repository that keeps something about the signed-in account in memory.
 *
 * The repositories live as long as the app process, and a sign-out is not a new process: signing
 * in as someone else on the same phone showed them the previous account's World progress, visited
 * regions and collection count, straight from these caches (measured, account B after A). Sign-out
 * empties every one of them.
 */
interface AccountDataCache {
    fun clearAccountData()
}
