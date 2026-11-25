package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.http.HttpMethod

/** A request's method, path and query: the parts the 0.14.0 renames touch. */
internal data class ApiRoute(
    val method: HttpMethod,
    val path: String,
    val query: List<Pair<String, String>> = emptyList(),
)

private class Rule(
    val method: HttpMethod?,
    val pattern: Regex,
    val to: ApiRoute.(groups: List<String>) -> ApiRoute,
)

private fun rule(method: HttpMethod?, pattern: String, to: ApiRoute.(groups: List<String>) -> ApiRoute) =
    Rule(method, Regex(pattern), to)

private fun rename(pattern: String, replacement: String, method: HttpMethod? = null) =
    rule(method, pattern) { g -> copy(path = g.foldIndexed(replacement) { i, acc, v -> acc.replace("$${i + 1}", v) }) }

private fun ApiRoute.renameQuery(from: String, to: String) =
    copy(query = query.map { (k, v) -> (if (k == from) to else k) to v })

/**
 * The 0.13.1 → 0.14.0 route table, from upstream's `backend/server/API.md` ("Changes in this
 * version") and the v0.14.0 `urls.py` files. Only renames live here: a path, a method, a query
 * parameter. Calls whose bodies or answers changed shape ask [ServerApi] themselves.
 * The first rule whose method and pattern match wins; a route no rule matches is unchanged.
 */
private val rules014: List<Rule> = listOf(
    // Sign-in. The app client reads X-Session-Token; the browser one wants the cookie, which a
    // 0.14 request no longer carries. Its DELETE on the session is how upstream says to sign out.
    rename("^/auth/browser/v1/(.*)$", "/api/auth/app/v1/$1"),
    rename("^/auth/app/v1/(.*)$", "/api/auth/app/v1/$1"),
    // The account
    rename("^/auth/user-metadata/$", "/api/account/"),
    rename("^/auth/update-user/$", "/api/account/"),
    rename("^/auth/user-media-usage/$", "/api/account/media-usage/"),
    rename("^/auth/users/$", "/api/users/"),
    rename("^/api/stats/counts/([^/]+)/?$", "/api/users/$1/stats/"),
    rename("^/api/stats/dashboard/$", "/api/dashboard/"),
    // Locations: /filtered/ folded into the list, with new parameter names.
    rule(HttpMethod.Get, "^/api/locations/filtered/$") { copy(path = "/api/locations/", query = locationQuery014(query)) },
    // Collections and sharing. The lists are paged now; their callers read either shape.
    rule(HttpMethod.Get, "^/api/collections/all/$") { copy(path = "/api/collections/", query = query + LIST_ALL) },
    rule(HttpMethod.Get, "^/api/collections/archived/$") {
        copy(path = "/api/collections/", query = query + ("archived" to "true") + LIST_ALL)
    },
    rule(HttpMethod.Get, "^/api/collections/shared/$") {
        copy(path = "/api/collections/", query = query + ("membership" to "shared") + LIST_ALL)
    },
    rename("^/api/collections/([^/]+)/share/[^/]+/$", "/api/collections/$1/invites/", HttpMethod.Post),
    rename("^/api/collections/invites/$", "/api/collections/invitations/"),
    rename("^/api/collections/([^/]+)/accept-invite/$", "/api/collections/$1/invitation/accept/"),
    rename("^/api/collections/([^/]+)/decline-invite/$", "/api/collections/$1/invitation/decline/"),
    rule(HttpMethod.Post, "^/api/collections/([^/]+)/revoke-invite/([^/]+)/$") { g ->
        copy(method = HttpMethod.Delete, path = "/api/collections/${g[0]}/invites/${g[1]}/")
    },
    rule(HttpMethod.Post, "^/api/collections/([^/]+)/unshare/([^/]+)/$") { g ->
        copy(method = HttpMethod.Delete, path = "/api/collections/${g[0]}/members/${g[1]}/")
    },
    rename("^/api/collections/([^/]+)/export-pdf/$", "/api/collections/$1/pdf/"),
    rename("^/api/collections/([^/]+)/export/$", "/api/collections/$1/archive/"),
    // Media, places and the rest
    rename("^/api/images/([^/]+)/toggle_primary/$", "/api/images/$1/toggle-primary/"),
    rename("^/api/reverse-geocode/mark_visited_region/$", "/api/visited-regions/sync/"),
    rule(null, "^/api/reverse-geocode/search/$") { copy(path = "/api/places/search/").renameQuery("query", "q") },
    rename("^/api/reverse-geocode/reverse_geocode/$", "/api/places/reverse/"),
    rename("^/api/generate/desc/$", "/api/places/description/"),
    rename("^/api/recommendations/query/$", "/api/recommendations/"),
    rule(HttpMethod.Get, "^/api/search/$") { renameQuery("query", "q") },
    rename("^/api/lodging/(.*)$", "/api/lodgings/$1"),
    // World travel
    rename("^/api/visitedregion/(.*)$", "/api/visited-regions/$1"),
    rename("^/api/visitedcity/(.*)$", "/api/visited-cities/$1"),
    rename("^/api/([A-Za-z]{2,3})/regions/$", "/api/countries/$1/regions/"),
)

/** The largest page 0.14 serves: the old routes answered everything at once. */
private val LIST_ALL = "page_size" to "1000"

/**
 * `/api/locations/filtered/`'s parameters as 0.14's list takes them. "all" meant no filter;
 * `order_by` + `order_direction` became one `ordering`, descending with a `-`; the app's null
 * direction means descending (LocationsViewModel sends null for its default). 0.14 refuses an
 * ordering it does not know, and 0.13 took `type`, which it no longer does: that falls back to
 * the default order rather than failing the list.
 */
private fun locationQuery014(old: List<Pair<String, String>>): List<Pair<String, String>> {
    val get = { key: String -> old.firstOrNull { it.first == key }?.second }
    val out = old.filter { it.first == "page" || it.first == "page_size" }.toMutableList()
    get("types")?.takeIf { it != "all" && it.isNotBlank() }?.let { out += "category" to it }
    get("is_visited")?.takeIf { it == "true" || it == "false" }?.let { out += "is_visited" to it }
    if (get("include_collections") == "false") out += "in_collection" to "false"
    val field = get("order_by") ?: "updated_at"
    if (field in setOf("name", "updated_at", "date", "rating")) {
        out += "ordering" to (if (get("order_direction") == "asc") field else "-$field")
    }
    return out
}

internal fun ApiRoute.for014(): ApiRoute {
    for (rule in rules014) {
        if (rule.method != null && rule.method != method) continue
        val match = rule.pattern.matchEntire(path) ?: continue
        return rule.to(this, match.groupValues.drop(1))
    }
    return this
}
