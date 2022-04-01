package com.fakedevelopers.ddangddangmarket.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.ddangddangmarket.api.repository.Repository

class MainViewModelFactory(private val repository: Repository): ViewModelProvider.Factory {
    override fun <MainViewModal : ViewModel> create(modelClass: Class<MainViewModal>): MainViewModal {
        return MainViewModel(repository) as MainViewModal
    }
}
