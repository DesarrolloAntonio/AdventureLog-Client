package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TransportationRepository
import com.desarrollodroide.adventurelog.core.model.Transportation

class GetTransportationUseCase(
    private val transportationRepository: TransportationRepository
) {
    suspend operator fun invoke(transportationId: String): Either<String, Transportation> {
        return transportationRepository.getTransportation(transportationId)
    }
}
