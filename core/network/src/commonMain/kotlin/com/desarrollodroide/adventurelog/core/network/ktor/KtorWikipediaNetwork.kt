package com.desarrollodroide.adventurelog.core.network.ktor

import com.desarrollodroide.adventurelog.core.network.datasource.WikipediaNetworkDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class KtorWikipediaNetwork(
    private val httpClient: HttpClient
) : WikipediaNetworkDataSource {

    override suspend fun searchImage(query: String): String? {
        try {
            println("WikipediaDataSource: Searching for '$query'")

            // First, search for the page
            val searchUrl = "https://en.wikipedia.org/w/api.php"
            val searchParams = mapOf(
                "action" to "opensearch",
                "search" to query,
                "limit" to "5",  // Get more results to increase chances
                "format" to "json",
                "origin" to "*"
            )

            println("WikipediaDataSource: Making search request to $searchUrl")
            val searchResponse = httpClient.get(searchUrl) {
                searchParams.forEach { (key, value) ->
                    parameter(key, value)
                }
            }

            val searchText = searchResponse.body<String>()
            println("WikipediaDataSource: Search response: ${searchText.take(200)}...")

            val searchResult = Json.parseToJsonElement(searchText).jsonArray
            val titles = searchResult.getOrNull(1) as? JsonArray

            if (titles == null || titles.isEmpty()) {
                println("WikipediaDataSource: No pages found for query")
                return null
            }

            // Try each result until we find one with an image
            for (i in 0 until titles.size) {
                val pageTitle = titles[i].jsonPrimitive.content
                println("WikipediaDataSource: Trying page: $pageTitle")

                // Get the page info with images
                val pageParams = mapOf(
                    "action" to "query",
                    "format" to "json",
                    "prop" to "pageimages|images",  // Request both pageimages and images
                    "titles" to pageTitle,
                    "pithumbsize" to "500",
                    "pilimit" to "1",
                    "redirects" to "1",
                    "origin" to "*"
                )

                println("WikipediaDataSource: Getting page info for '$pageTitle'")
                val pageResponse = httpClient.get(searchUrl) {
                    pageParams.forEach { (key, value) ->
                        parameter(key, value)
                    }
                }

                val pageText = pageResponse.body<String>()
                println("WikipediaDataSource: Page response: ${pageText.take(300)}...")

                val jsonResponse = Json.parseToJsonElement(pageText).jsonObject
                val queryObj = jsonResponse["query"]?.jsonObject
                val pages = queryObj?.get("pages")?.jsonObject

                if (!pages.isNullOrEmpty()) {
                    val page = pages.values.firstOrNull()?.jsonObject
                    println("WikipediaDataSource: Page data: $page")

                    // Try to get thumbnail first
                    val thumbnail = page?.get("thumbnail")?.jsonObject
                    val thumbnailUrl = thumbnail?.get("source")?.jsonPrimitive?.content
                    if (!thumbnailUrl.isNullOrEmpty()) {
                        println("WikipediaDataSource: Found thumbnail URL: $thumbnailUrl")
                        return thumbnailUrl
                    }

                    // If no thumbnail, try original image
                    val original = page?.get("original")?.jsonObject
                    val originalUrl = original?.get("source")?.jsonPrimitive?.content
                    if (!originalUrl.isNullOrEmpty()) {
                        println("WikipediaDataSource: Found original URL: $originalUrl")
                        return originalUrl
                    }

                    // Try pageimage property
                    val pageImage = page?.get("pageimage")?.jsonPrimitive?.content
                    if (!pageImage.isNullOrEmpty()) {
                        // Construct Wikipedia image URL
                        val imageUrl = "https://en.wikipedia.org/wiki/Special:FilePath/$pageImage"
                        println("WikipediaDataSource: Constructed URL from pageimage: $imageUrl")
                        return imageUrl
                    }
                }
            }

            println("WikipediaDataSource: No image found in any result")
            return null
        } catch (e: Exception) {
            println("WikipediaDataSource: Error searching Wikipedia image: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}
