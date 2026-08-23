package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TrailsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationTrailsUseCase
import com.desarrollodroide.adventurelog.core.model.Trail
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncLocationTrailsUseCaseTest {

    private class FakeTrailsRepository : TrailsRepository {
        val created = mutableListOf<TrailFormData>()
        val updated = mutableListOf<String>()
        val deleted = mutableListOf<String>()
        var failNext: ApiResponse? = null

        override suspend fun createTrail(locationId: String, trail: TrailFormData) =
            failNext?.let { Either.Left(it) } ?: run {
                created += trail
                Either.Right(trailOf("new"))
            }

        override suspend fun updateTrail(
            trailId: String,
            locationId: String,
            trail: TrailFormData
        ) = failNext?.let { Either.Left(it) } ?: run {
            updated += trailId
            Either.Right(trailOf(trailId))
        }

        override suspend fun deleteTrail(trailId: String): Either<ApiResponse, Unit> =
            failNext?.let { Either.Left(it) } ?: run {
                deleted += trailId
                Either.Right(Unit)
            }
    }

    private val repository = FakeTrailsRepository()
    private val useCase = SyncLocationTrailsUseCase(repository)

    @Test
    fun `a trail without an id is created`() = runTest {
        val result = useCase("loc-1", emptyList(), listOf(form()))

        assertTrue(result is Either.Right)
        assertEquals(1, repository.created.size)
    }

    @Test
    fun `an existing trail is updated in place`() = runTest {
        useCase("loc-1", listOf(trailOf("t1")), listOf(form(id = "t1", name = "Renamed")))

        assertEquals(listOf("t1"), repository.updated)
        assertTrue(repository.deleted.isEmpty())
    }

    @Test
    fun `a trail dropped from the form is deleted`() = runTest {
        useCase("loc-1", listOf(trailOf("t1"), trailOf("t2")), listOf(form(id = "t2")))

        assertEquals(listOf("t1"), repository.deleted)
    }

    @Test
    fun `a trail with no link is not sent`() = runTest {
        // The server rejects a trail that has neither a link nor a Wanderer id, so a half-filled
        // row is skipped rather than turned into a failed request.
        val result = useCase("loc-1", emptyList(), listOf(form(link = "")))

        assertTrue(result is Either.Right)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `a nameless trail is not sent`() = runTest {
        val result = useCase("loc-1", emptyList(), listOf(form(name = "")))

        assertTrue(result is Either.Right)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `a failure says the location was still saved`() = runTest {
        repository.failNext = ApiResponse.HttpError

        val result = useCase("loc-1", emptyList(), listOf(form()))

        assertTrue(result is Either.Left)
        assertTrue((result as Either.Left).value.startsWith("The location was saved"))
    }

    private fun form(
        id: String? = null,
        name: String = "Ruta del Cares",
        link: String = "https://www.alltrails.com/es/ruta/spain/asturias/ruta-del-cares"
    ) = TrailFormData(id = id, name = name, link = link)
}

private fun trailOf(id: String) = Trail(
    id = id,
    user = "user-1",
    name = "Ruta del Cares",
    location = "loc-1",
    createdAt = "2026-08-23T00:00:00Z",
    link = "https://www.alltrails.com/es/ruta/spain/asturias/ruta-del-cares"
)
