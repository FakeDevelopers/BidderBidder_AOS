package com.fakedevelopers.ddangddangmarket.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.ddangddangmarket.api.repository.Repository
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val repository: Repository): ViewModel() {
    // LiveData는 LifeCycle 내에서만 동작하며 LifeCycle이 종료되면 같이 삭제된다.
    // 그래서 메모리 누출이 없고 수명주기에 따른 데이터 관리를 내가 따로 안해도 된다는 이점이 있다.
    val loginResponse: MutableLiveData<Response<String>> = MutableLiveData()

    // 코루틴으로 api를 호출해서 결과를 LiveData에 넣는다.
    fun loginRequest(email: String, passwd: String) {
        viewModelScope.launch {
            val response = repository.loginRequest(email, passwd)
            loginResponse.value = response
        }
    }
}
