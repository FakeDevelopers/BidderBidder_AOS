package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImageObserverUseCase @Inject constructor(
    private val repository: ImageRepository
) {
    operator fun invoke(): Flow<String> = repository.getImageObserver()
}
