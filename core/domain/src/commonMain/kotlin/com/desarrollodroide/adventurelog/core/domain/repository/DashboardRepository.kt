package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Dashboard

/**
 * Reads the aggregate the home screen renders. Kept apart from [UserRepository] because this is
 * screen data, not identity or session state.
 */
interface DashboardRepository {
    suspend fun getDashboard(): Either<ApiResponse, Dashboard>
}
