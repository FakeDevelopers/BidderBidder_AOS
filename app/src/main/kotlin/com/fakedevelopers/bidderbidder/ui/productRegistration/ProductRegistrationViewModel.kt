package com.fakedevelopers.bidderbidder.ui.productRegistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.bidderbidder.api.data.Constants.Companion.dateFormatter
import com.fakedevelopers.bidderbidder.api.repository.ProductCategoryRepository
import com.fakedevelopers.bidderbidder.api.repository.ProductRegistrationRepository
import com.fakedevelopers.bidderbidder.ui.productRegistration.albumList.SelectedImageInfo
import com.fakedevelopers.bidderbidder.ui.util.ApiErrorHandler
import com.fakedevelopers.bidderbidder.ui.util.MutableEventFlow
import com.fakedevelopers.bidderbidder.ui.util.asEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val repository: ProductRegistrationRepository,
    private val categoryRepository: ProductCategoryRepository
) : ViewModel() {

    val adapter = SelectedPictureListAdapter(
        deleteSelectedImage = { deleteSelectedImage(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }
    private val _productRegistrationResponse = MutableEventFlow<Response<String>>()
    private val _productCategory = MutableStateFlow(listOf<ProductCategoryDto>())
    private val _condition = MutableStateFlow(false)
    private val _contentLengthVisible = MutableStateFlow(false)

    val selectedImageInfo = SelectedImageInfo()
    val contentLengthVisible: StateFlow<Boolean> get() = _contentLengthVisible
    val productRegistrationResponse = _productRegistrationResponse.asEventFlow()
    val productCategory = _productCategory.asStateFlow()
    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val hopePrice = MutableStateFlow("")
    val openingBid = MutableStateFlow("")
    val tick = MutableStateFlow("")
    val expiration = MutableStateFlow("")
    var category: List<String> = listOf()
        private set
    private var categoryID: Long = 0

    // 등록 조건 완료
    val condition: StateFlow<Boolean> get() = _condition

    private fun deleteSelectedImage(uri: String) {
        val list = selectedImageInfo.uris.filter { it != uri }
        adapter.submitList(list)
        // 첫번째 사진이 삭제 된다면 다음 사진에게 대표직을 물려줌
        if (selectedImageInfo.uris.isNotEmpty() && !list.contains(selectedImageInfo.uris[0])) {
            adapter.notifyItemChanged(1)
        }
        selectedImageInfo.uris = list.toMutableList()
    }

    private fun swapSelectedImage(fromPosition: Int, toPosition: Int) {
        val list = selectedImageInfo.uris.toMutableList()
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(list, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(list, i, i - 1)
            }
        }
        adapter.submitList(list)
        selectedImageInfo.uris = list.toMutableList()
    }

    private fun findSelectedImageIndex(uri: String) = selectedImageInfo.uris.indexOf(uri)

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

    fun requestProductRegistration(imageList: List<MultipartBody.Part>) {
        viewModelScope.launch {
            val date = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis() + expiration.value.toInt() * 3600000),
                ZoneId.of("Asia/Seoul")
            )
            val map = hashMapOf<String, RequestBody>()
            map["productContent"] = content.value.toPlainRequestBody()
            map["productTitle"] = title.value.toPlainRequestBody()
            map["expirationDate"] = dateFormatter.format(date).toPlainRequestBody()
            map["hopePrice"] = hopePrice.value.replace(",", "").toPlainRequestBody()
            map["openingBid"] = openingBid.value.replace(",", "").toPlainRequestBody()
            map["representPicture"] = "0".toPlainRequestBody()
            map["tick"] = tick.value.replace(",", "").toPlainRequestBody()
            map["category"] = categoryID.toString().toPlainRequestBody()
            repository.postProductRegistration(imageList, map).let {
                if (it.isSuccessful) {
                    _productRegistrationResponse.emit(it)
                } else {
                    ApiErrorHandler.handleError(it.errorBody())
                }
            }
        }
    }

    fun requestProductCategory() {
        viewModelScope.launch {
            categoryRepository.getProdectCategory().let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> _productCategory.emit(responseBody) }
                } else {
                    ApiErrorHandler.handleError(it.errorBody())
                }
            }
        }
    }

    fun initState(state: ProductRegistrationDto) {
        selectedImageInfo.uris = state.selectedImageInfo.uris
        selectedImageInfo.changeBitmaps.putAll(state.selectedImageInfo.changeBitmaps)
        viewModelScope.launch {
            adapter.submitList(selectedImageInfo.uris.toMutableList())
            title.emit(state.title)
            hopePrice.emit(state.hopePrice)
            openingBid.emit(state.openingBid)
            tick.emit(state.tick)
            expiration.emit(state.expiration)
            content.emit(state.content)
        }
    }

    fun getProductRegistrationDto() = ProductRegistrationDto(
        selectedImageInfo,
        title.value,
        hopePrice.value,
        openingBid.value,
        tick.value,
        expiration.value,
        content.value,
        categoryID
    )

    fun setUrlList(list: List<String>) {
        viewModelScope.launch {
            adapter.submitList(list)
            adapter.notifyDataSetChanged()
        }
        selectedImageInfo.uris = list.toMutableList()
    }

    fun setContentLengthVisibility(state: Boolean) {
        viewModelScope.launch {
            _contentLengthVisible.emit(state)
        }
    }

    fun setCategoryList(list: List<ProductCategoryDto>) {
        category = list.map { it.categoryName }
    }

    fun setCategoryID(index: Long) {
        categoryID = productCategory.value[index.toInt()].categoryId
    }

    private fun String?.toPlainRequestBody() = requireNotNull(this).toRequestBody("text/plain".toMediaTypeOrNull())
}
