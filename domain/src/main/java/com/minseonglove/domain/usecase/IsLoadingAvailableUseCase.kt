package com.minseonglove.domain.usecase

import com.minseonglove.domain.repository.ProductListRepository
import javax.inject.Inject

class IsLoadingAvailableUseCase @Inject constructor(
    private val repository: ProductListRepository
) {
    operator fun invoke(): Boolean = repository.isLoadingAvailable()
}
