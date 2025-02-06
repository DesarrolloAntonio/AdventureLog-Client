package com.desarrollodroide.adventurelog.core.network.ktor

import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel

private const val PS = "ps"
private const val PAGING_PAGE_SIZE = 100
const val ADVENTURELOG_HOST = "192.168.1.27"
const val ADVENTURELOG_PATH = "api/en/"
const val COLLECTION = "collection"
const val PAGE = "p"
const val HAS_IMAGE = "hasImage"

class KtorADVENTURELOGNetwork(
    private val ADVENTURELOGClient: HttpClient,
    private val client: HttpClient,
) : AdventureLogNetworkDataSource {

}