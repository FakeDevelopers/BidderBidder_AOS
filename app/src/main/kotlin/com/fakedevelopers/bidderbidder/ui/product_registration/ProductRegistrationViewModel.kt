package com.fakedevelopers.bidderbidder.ui.product_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.repository.ProductRegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class ProductRegistrationViewModel @Inject constructor(
    private val repository: ProductRegistrationRepository
) : ViewModel() {

    val imageList = mutableListOf<MultipartBody.Part>()

    private val _productRegistrationResponse = MutableSharedFlow<Response<String>>()

    val productRegistrationResponse: SharedFlow<Response<String>> get() = _productRegistrationResponse

    fun productRegistrationRequest() {
        viewModelScope.launch {
            val map = hashMapOf<String, RequestBody>()
            map["board_content"] = "콘텐트 내용".toPlainRequestBody()
            map["board_title"] = "제목".toPlainRequestBody()
            map["category"] = "1".toPlainRequestBody()
            map["end_date"] = "2030-01-01 07:07".toPlainRequestBody()
            map["hope_price"] = "100".toPlainRequestBody()
            map["opening_bid"] = "50".toPlainRequestBody()
            map["tick"] = "1".toPlainRequestBody()
            _productRegistrationResponse.emit(repository.postProductRegistration(imageList, map))
        }
    }

    private fun String?.toPlainRequestBody() = requireNotNull(this).toRequestBody("text/plain".toMediaTypeOrNull())
}
