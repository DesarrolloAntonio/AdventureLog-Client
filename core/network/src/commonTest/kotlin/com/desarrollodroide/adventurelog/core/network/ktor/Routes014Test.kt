package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals

/** The 0.13.1 → 0.14.0 renames, row by row from upstream's backend/server/API.md. */
class Routes014Test {

    private fun assertRoute(
        expected: ApiRoute,
        method: HttpMethod,
        path: String,
        query: List<Pair<String, String>> = emptyList(),
    ) = assertEquals(expected, ApiRoute(method, path, query).for014(), "$method $path")

    private val get = HttpMethod.Get
    private val post = HttpMethod.Post
    private val delete = HttpMethod.Delete

    @Test
    fun `sign-in moves under api and uses the app client`() {
        assertRoute(ApiRoute(post, "/api/auth/app/v1/auth/login"), post, "/auth/app/v1/auth/login")
        assertRoute(ApiRoute(delete, "/api/auth/app/v1/auth/session"), delete, "/auth/browser/v1/auth/session")
        assertRoute(ApiRoute(get, "/api/auth/app/v1/account/email"), get, "/auth/browser/v1/account/email")
        assertRoute(
            ApiRoute(post, "/api/auth/app/v1/account/password/change"), post, "/auth/browser/v1/account/password/change",
        )
    }

    @Test
    fun `the account and profiles`() {
        assertRoute(ApiRoute(get, "/api/account/"), get, "/auth/user-metadata/")
        assertRoute(ApiRoute(HttpMethod.Patch, "/api/account/"), HttpMethod.Patch, "/auth/update-user/")
        assertRoute(ApiRoute(get, "/api/account/media-usage/"), get, "/auth/user-media-usage/")
        assertRoute(ApiRoute(get, "/api/users/"), get, "/auth/users/")
        assertRoute(ApiRoute(get, "/api/users/john/stats/"), get, "/api/stats/counts/john")
        assertRoute(ApiRoute(get, "/api/dashboard/"), get, "/api/stats/dashboard/")
    }

    @Test
    fun `sharing actions become resources`() {
        assertRoute(ApiRoute(get, "/api/collections/invitations/"), get, "/api/collections/invites/")
        assertRoute(ApiRoute(post, "/api/collections/c1/invitation/accept/"), post, "/api/collections/c1/accept-invite/")
        assertRoute(ApiRoute(post, "/api/collections/c1/invitation/decline/"), post, "/api/collections/c1/decline-invite/")
        assertRoute(ApiRoute(delete, "/api/collections/c1/invites/u1/"), post, "/api/collections/c1/revoke-invite/u1/")
        assertRoute(ApiRoute(delete, "/api/collections/c1/members/u1/"), post, "/api/collections/c1/unshare/u1/")
        assertRoute(ApiRoute(get, "/api/collections/c1/pdf/"), get, "/api/collections/c1/export-pdf/")
        assertRoute(ApiRoute(get, "/api/collections/c1/archive/"), get, "/api/collections/c1/export/")
    }

    @Test
    fun `places, search and world travel`() {
        assertRoute(
            ApiRoute(get, "/api/places/search/", listOf("q" to "Lima")), get, "/api/reverse-geocode/search/", listOf("query" to "Lima"),
        )
        assertRoute(ApiRoute(get, "/api/search/", listOf("q" to "Lima")), get, "/api/search/", listOf("query" to "Lima"))
        assertRoute(ApiRoute(post, "/api/visited-regions/sync/"), post, "/api/reverse-geocode/mark_visited_region/")
        assertRoute(ApiRoute(get, "/api/lodgings/l1/"), get, "/api/lodging/l1/")
        assertRoute(ApiRoute(get, "/api/countries/PE/regions/"), get, "/api/PE/regions/")
        assertRoute(ApiRoute(delete, "/api/visited-regions/12/"), delete, "/api/visitedregion/12/")
        assertRoute(ApiRoute(post, "/api/images/i1/toggle-primary/"), post, "/api/images/i1/toggle_primary/")
    }

    @Test
    fun `filtered locations become the list with its new parameters`() {
        assertRoute(
            ApiRoute(
                get, "/api/locations/",
                listOf("page" to "2", "page_size" to "20", "category" to "Food,Museum", "is_visited" to "true",
                    "in_collection" to "false", "ordering" to "name"),
            ),
            get, "/api/locations/filtered/",
            listOf("page" to "2", "page_size" to "20", "types" to "Food,Museum", "order_by" to "name",
                "order_direction" to "asc", "is_visited" to "true", "include_collections" to "false"),
        )
        // The app's defaults: every category, visited or not, newest first.
        assertRoute(
            ApiRoute(get, "/api/locations/", listOf("page" to "1", "ordering" to "-updated_at")),
            get, "/api/locations/filtered/",
            listOf("page" to "1", "types" to "all", "is_visited" to "all", "include_collections" to "true"),
        )
        // 0.14 answers 400 to an ordering it does not know; the default order beats a failed list.
        assertRoute(
            ApiRoute(get, "/api/locations/"), get, "/api/locations/filtered/", listOf("order_by" to "type"),
        )
    }

    @Test
    fun `collection lists become filters of the paged list`() {
        assertRoute(ApiRoute(get, "/api/collections/", listOf("page_size" to "1000")), get, "/api/collections/all/")
        assertRoute(
            ApiRoute(get, "/api/collections/", listOf("archived" to "true", "page_size" to "1000")),
            get, "/api/collections/archived/",
        )
        assertRoute(
            ApiRoute(get, "/api/collections/", listOf("membership" to "shared", "page_size" to "1000")),
            get, "/api/collections/shared/",
        )
        assertRoute(ApiRoute(post, "/api/collections/c1/invites/"), post, "/api/collections/c1/share/u1/")
        assertRoute(ApiRoute(get, "/api/places/reverse/"), get, "/api/reverse-geocode/reverse_geocode/")
    }

    @Test
    fun `routes that did not move stay as they are`() {
        listOf("/api/locations/", "/api/locations/l1/", "/api/collections/c1/", "/api/visits/", "/api/countries/").forEach {
            assertRoute(ApiRoute(get, it), get, it)
        }
    }
}
