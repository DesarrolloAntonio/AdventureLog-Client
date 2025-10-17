package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TransportationRepository

class DeleteTransportationUseCase(
    private val transportationRepository: TransportationRepository
) {
    suspend operator fun invoke(transportationId: String): Either<String, Unit> {
        return transportationRepository.deleteTransportation(transportationId)
    }
}
