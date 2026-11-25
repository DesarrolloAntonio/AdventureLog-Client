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
)

internal fun ApiRoute.for014(): ApiRoute {
    for (rule in rules014) {
        if (rule.method != null && rule.method != method) continue
        val match = rule.pattern.matchEntire(path) ?: continue
        return rule.to(this, match.groupValues.drop(1))
    }
    return this
}
