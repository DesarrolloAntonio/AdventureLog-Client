package com.desarrollodroide.adventurelog

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CancellationException
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform
import platform.Foundation.NSData
import platform.Foundation.create

/**
 * Photos for SwiftUI, fetched with the app's own image client: media on the server answers 403
 * without the session, which `AsyncImage` cannot send, so Swift asks here and gets the bytes.
 * Null for anything that did not load; the caller shows its placeholder.
 */
object IosImages {
    private val client: HttpClient by lazy { KoinPlatform.getKoin().get(named("imageClient")) }

    suspend fun data(url: String): NSData? = try {
        val response = client.get(url)
        if (response.status.isSuccess()) response.readRawBytes().toNSData() else null
    } catch (e: CancellationException) {
        throw e
    } catch (_: Throwable) {
        null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }
}
