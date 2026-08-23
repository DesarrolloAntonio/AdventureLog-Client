package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.VisitsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationVisitsUseCase
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncLocationVisitsUseCaseTest {

    private class FakeVisitsRepository : VisitsRepository {
        val created = mutableListOf<Pair<String, VisitFormData>>()
        val updated = mutableListOf<Pair<String, VisitFormData>>()
        val deleted = mutableListOf<String>()
        var failNext: ApiResponse? = null

        override suspend fun createVisit(locationId: String, visit: VisitFormData) =
            failNext?.let { Either.Left(it) } ?: run {
                created += locationId to visit
                Either.Right(visitOf("new-${created.size}"))
            }

        override suspend fun updateVisit(
            visitId: String,
            locationId: String,
            visit: VisitFormData
        ) = failNext?.let { Either.Left(it) } ?: run {
            updated += visitId to visit
            Either.Right(visitOf(visitId))
        }

        override suspend fun deleteVisit(visitId: String): Either<ApiResponse, Unit> =
            failNext?.let { Either.Left(it) } ?: run {
                deleted += visitId
                Either.Right(Unit)
            }
    }

    private val repository = FakeVisitsRepository()
    private val useCase = SyncLocationVisitsUseCase(repository)

    @Test
    fun `a visit without an id is created`() = runTest {
        val result = useCase("loc-1", existing = emptyList(), edited = listOf(form()))

        assertTrue(result is Either.Right)
        assertEquals(1, repository.created.size)
        assertEquals("loc-1", repository.created.single().first)
        assertTrue(repository.updated.isEmpty())
    }

    @Test
    fun `a visit that already exists is updated rather than replaced`() = runTest {
        // Activities hang off a visit, so deleting and recreating would take them with it.
        useCase(
            locationId = "loc-1",
            existing = listOf(visitOf("v1")),
            edited = listOf(form(id = "v1", notes = "changed"))
        )

        assertEquals(listOf("v1"), repository.updated.map { it.first })
        assertTrue(repository.deleted.isEmpty())
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `a visit dropped from the form is deleted`() = runTest {
        useCase(
            locationId = "loc-1",
            existing = listOf(visitOf("v1"), visitOf("v2")),
            edited = listOf(form(id = "v2"))
        )

        assertEquals(listOf("v1"), repository.deleted)
    }

    @Test
    fun `a blank visit is ignored rather than sent`() = runTest {
        // The form starts every new row empty; saving before filling one in must not post it.
        val result = useCase("loc-1", emptyList(), listOf(form(startDate = "")))

        assertTrue(result is Either.Right)
        assertTrue(repository.created.isEmpty())
    }

    @Test
    fun `nothing to do reports success`() = runTest {
        val result = useCase("loc-1", emptyList(), emptyList())

        assertTrue(result is Either.Right)
    }

    @Test
    fun `a failure is reported without claiming the location failed`() = runTest {
        repository.failNext = ApiResponse.HttpError

        val result = useCase("loc-1", emptyList(), listOf(form()))

        assertTrue(result is Either.Left)
        // The location itself was already saved by the time this runs, so the message must not
        // read as if the whole save was lost.
        assertTrue((result as Either.Left).value.startsWith("The location was saved"))
    }

    private fun form(
        id: String? = null,
        startDate: String = "2026-08-23",
        notes: String = ""
    ) = VisitFormData(id = id, startDate = startDate, notes = notes)
}

private fun visitOf(id: String) = Visit(
    id = id,
    location = "loc-1",
    startDate = "2026-08-23T00:00:00Z",
    endDate = "2026-08-23T00:00:00Z",
    createdAt = "2026-08-23T00:00:00Z",
    updatedAt = "2026-08-23T00:00:00Z"
)
