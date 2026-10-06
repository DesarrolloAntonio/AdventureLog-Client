package com.desarrollodroide.adventurelog.core.domain.repository

/**
 * Copies of the signed-in account's data that the app leaves on the device outside the session:
 * files written for another app to open or share (a backup zip, an attachment) and camera captures.
 * Sign-out deletes them - an 8.7 MB backup of every place and photo outlived it (measured).
 */
fun interface LocalAccountCopies {
    suspend fun delete()

    companion object {
        /** For a platform, or a test, with nothing to delete. */
        val None = LocalAccountCopies { }
    }
}
