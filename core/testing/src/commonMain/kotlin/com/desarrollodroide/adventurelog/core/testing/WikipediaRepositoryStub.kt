package com.desarrollodroide.adventurelog.core.testing


// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a WikipediaRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class WikipediaRepositoryStub : WikipediaRepository {
    override suspend fun searchImage(query: String): Result<String?> = unused()
}
