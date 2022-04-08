package com.fakedevelopers.bidderbidder.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fakedevelopers.bidderbidder.api.repository.LoginRepository

class MainViewModelFactory(private val loginRepository: LoginRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(loginRepository) as T
    }
}
