package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetDashboardUseCase
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.testing.DashboardRepositoryStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDashboardUseCaseTest {

    private fun failingWith(error: ApiResponse) = GetDashboardUseCase(object : DashboardRepositoryStub() {
        override suspend fun getDashboard(): Either<ApiResponse, Dashboard> = Either.Left(error)
    })

    @Test
    fun `a server that cannot be reached is not called a missing connection`() = runTest {
        // A timeout reaches here as the same IOException as a phone with no network: the message
        // has to be true for both.
        assertEquals(
            Either.Left("Can't reach the server. Check your connection."),
            failingWith(ApiResponse.IOException)()
        )
    }
}
