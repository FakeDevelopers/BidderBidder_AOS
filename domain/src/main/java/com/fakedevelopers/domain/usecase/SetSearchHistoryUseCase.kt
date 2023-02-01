package com.fakedevelopers.domain.usecase

import com.fakedevelopers.domain.repository.LocalStorageRepository
import javax.inject.Inject

class SetSearchHistoryUseCase @Inject constructor(
    private val repository: LocalStorageRepository
) {
    suspend operator fun invoke(searchHistory: List<String>): Boolean =
        repository.setSearchHistory(searchHistory)
}
