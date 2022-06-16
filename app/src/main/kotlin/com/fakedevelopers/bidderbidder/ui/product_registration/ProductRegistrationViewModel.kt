package com.fakedevelopers.bidderbidder.ui.product_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dateFormatter
import com.fakedevelopers.bidderbidder.api.repository.ProductRegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import retrofit2.Response
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class ProductRegistrationViewModel @Inject constructor(
    private val repository: ProductRegistrationRepository
) : ViewModel() {

    val adapter = SelectedPictureListAdapter(
        deleteSelectedImage = { deleteSelectedImage(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }
    private val _urlList = MutableStateFlow<MutableList<String>>(mutableListOf())
    private val _productRegistrationResponse = MutableSharedFlow<Response<String>>()
    private val _condition = MutableStateFlow(false)

    val urlList: StateFlow<List<String>> get() = _urlList
    val productRegistrationResponse: SharedFlow<Response<String>> get() = _productRegistrationResponse
    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val hopePrice = MutableStateFlow("")
    val openingBid = MutableStateFlow("")
    val tick = MutableStateFlow("")
    val expiration = MutableStateFlow("")
    // 카테고리 목록을 받아오는 api 필요
    val category = mutableListOf(
        "카테고리를",
        "받아오는",
        "api가",
        "필요함",
        "카테고리 선택"
    )
    // 등록 조건 완료
    val condition: StateFlow<Boolean> get() = _condition

    private fun deleteSelectedImage(uri: String) {
        _urlList.value.remove(uri)
        adapter.submitList(_urlList.value.toList())
        // 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
        if (_urlList.value.isNotEmpty()) {
            adapter.notifyItemChanged(1)
        }
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(_urlList.value, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(_urlList.value, i, i - 1)
            }
        }
        adapter.notifyItemMoved(fromPosition, toPosition)
    }

    private fun findSelectedImageIndex(uri: String) = _urlList.value.indexOf(uri)

    // 게시글 등록 조건 검사
    fun checkRegistrationCondition() {
        viewModelScope.launch {
            // 희망가, 호가, 만료 시간은 0이 되면 안됨
            if (hopePrice.value == "0") {
                hopePrice.emit("")
            }
            if (tick.value == "0") {
                tick.emit("")
            }
            if (expiration.value == "0") {
                expiration.emit("")
            }
            _condition.emit(
                title.value.isNotEmpty() &&
                    (hopePrice.value.isEmpty() || hopePrice.value.replace(",", "").toLongOrNull() != null) &&
                    openingBid.value.replace(",", "").toLongOrNull() != null &&
                    tick.value.replace(",", "").toIntOrNull() != null &&
                    expiration.value.toIntOrNull() != null &&
                    content.value.isNotEmpty()
            )
        }
    }

    fun productRegistrationRequest(imageList: List<MultipartBody.Part>) {
        viewModelScope.launch {
            val date = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis() + expiration.value.toInt() * 3600000),
                ZoneId.systemDefault()
            )
            val map = hashMapOf<String, RequestBody>()
            map["productContent"] = content.value.toPlainRequestBody()
            map["productTitle"] = title.value.toPlainRequestBody()
            map["category"] = "0".toPlainRequestBody()
            map["expirationDate"] = dateFormatter.format(date).toPlainRequestBody()
            map["hopePrice"] = hopePrice.value.replace(",", "").toPlainRequestBody()
            map["openingBid"] = openingBid.value.replace(",", "").toPlainRequestBody()
            map["representPicture"] = "0".toPlainRequestBody()
            map["tick"] = tick.value.replace(",", "").toPlainRequestBody()
            _productRegistrationResponse.emit(repository.postProductRegistration(imageList, map))
        }
    }

    fun initState(state: ProductRegistrationDto) {
        viewModelScope.launch {
            _urlList.emit(state.urlList.toMutableList())
            adapter.submitList(_urlList.value.toList())
            title.emit(state.title)
            hopePrice.emit(state.hopePrice)
            openingBid.emit(state.openingBid)
            tick.emit(state.tick)
            expiration.emit(state.expiration)
            content.emit(state.content)
        }
    }

    fun getProductRegistrationDto() = ProductRegistrationDto(
        urlList.value,
        title.value,
        hopePrice.value,
        openingBid.value,
        tick.value,
        expiration.value,
        content.value
    )

    private fun String?.toPlainRequestBody() = requireNotNull(this).toRequestBody("text/plain".toMediaTypeOrNull())
}
