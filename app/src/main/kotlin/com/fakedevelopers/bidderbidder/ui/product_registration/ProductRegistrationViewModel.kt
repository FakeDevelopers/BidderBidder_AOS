package com.fakedevelopers.bidderbidder.ui.product_registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.UserProductRegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductRegistrationViewModel @Inject constructor(private val repository: UserProductRegistrationRepository): ViewModel() {

    val imageList = mutableListOf<MultipartBody.Part>()

    private val _productRegistrationResponse = MutableLiveData<Response<String>>()

    val productRegistrationResponse: LiveData<Response<String>> get() = _productRegistrationResponse

    fun productRegistrationRequest() {
        viewModelScope.launch {
            // 테스트 목적으로 넣었읍니다
            val content = "콘텐트 내용".toRequestBody("text/plain".toMediaTypeOrNull())
            val title = "제목".toRequestBody("text/plain".toMediaTypeOrNull())
            val category = "1".toRequestBody("text/plain".toMediaTypeOrNull())
            val endDate = "2030-01-01 07:07".toRequestBody("text/plain".toMediaTypeOrNull())
            val hope = "100".toRequestBody("text/plain".toMediaTypeOrNull())
            val open = "50".toRequestBody("text/plain".toMediaTypeOrNull())
            val tick = "1".toRequestBody("text/plain".toMediaTypeOrNull())
            val map = hashMapOf<String, RequestBody>()
            map["board_content"] = content
            map["board_title"] = title
            map["category"] = category
            map["end_date"] = endDate
            map["hope_price"] = hope
            map["opening_bid"] = open
            map["tick"] = tick

            _productRegistrationResponse.value = repository.postProductRegistration(imageList, map)
        }
    }
}
