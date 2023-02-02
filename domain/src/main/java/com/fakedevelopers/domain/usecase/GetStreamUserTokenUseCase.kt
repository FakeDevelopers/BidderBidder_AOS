package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.ChatRepository
import javax.inject.Inject

class GetStreamUserTokenUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(id: Long): Result<String> =
        repository.getStreamUserToken(id)
}
