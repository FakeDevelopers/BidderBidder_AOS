package com.fakedevelopers.ddangddangmarket.api.instance

import com.fakedevelopers.ddangddangmarket.api.data.Constants.Companion.BASE_URL
import com.fakedevelopers.ddangddangmarket.api.service.RetrofitService
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    // BASE_URL과 Converter를 설정해준다.
    // by lazy로 늦은 초기화를 해준다.
    // 지금은 문자열을 받아오므로 스칼라 팩토리를 넣어준다
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
    }

    // by lazy로 늦은 초기화를 해준다.
    // loginRequestApi가 호출되면서 초기화 되고 그 안에서 retrofit도 사용하면서 걔도 초기화 해준다.
    val RETROFIT_API: RetrofitService by lazy {
        retrofit.create(RetrofitService::class.java)
    }
}
