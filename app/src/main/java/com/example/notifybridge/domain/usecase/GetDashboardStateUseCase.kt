package com.example.notifybridge.domain.usecase

import com.example.notifybridge.domain.model.DashboardState
import com.example.notifybridge.domain.repository.DeliveryLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStateUseCase @Inject constructor(
    private val deliveryLogRepository: DeliveryLogRepository,
) {
    operator fun invoke(): Flow<DashboardState> = deliveryLogRepository.observeDashboardState()
}
