package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class GetSearchHistoryUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    suspend operator fun invoke(): List<String> =
        repository.getSearchHistory()
}
