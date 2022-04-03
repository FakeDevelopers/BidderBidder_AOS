package com.fakedevelopers.bidderbidder.api.repository

import com.fakedevelopers.bidderbidder.api.instance.RetrofitInstance
import retrofit2.Response

// 데이터 통신을 하는 Repository
// 여기서 통신한 값을 뷰 모델에서 사용한다.
// 리턴값은 String
class Repository {
    suspend fun loginRequest(email: String, passwd: String) : Response<String> {
        return RetrofitInstance.RETROFIT_API.loginRequest(email, passwd)
    }
}
