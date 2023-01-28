package com.fakedevelopers.presentation.ui.productEditor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.usecase.GetBytesByUriUseCase
import com.fakedevelopers.domain.usecase.GetMediaInfoUseCase
import com.fakedevelopers.domain.usecase.GetRotateUseCase
import com.fakedevelopers.domain.usecase.GetValidUrisUseCase
import com.fakedevelopers.presentation.api.repository.ProductCategoryRepository
import com.fakedevelopers.presentation.api.repository.ProductEditRepository
import com.fakedevelopers.presentation.api.repository.ProductRegistrationRepository
import com.fakedevelopers.presentation.ui.productEditor.albumList.SelectedImageInfo
import com.fakedevelopers.presentation.ui.util.DATE_PATTERN
import com.fakedevelopers.presentation.ui.util.MutableEventFlow
import com.fakedevelopers.presentation.ui.util.asEventFlow
import com.fakedevelopers.presentation.ui.util.getMultipart
import com.fakedevelopers.presentation.ui.util.getRotatedBitmap
import com.fakedevelopers.presentation.ui.util.priceToInt
import com.fakedevelopers.presentation.ui.util.priceToLong
import com.fakedevelopers.presentation.ui.util.toBitmap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Response
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class ProductEditorViewModel @Inject constructor(
    private val repository: ProductRegistrationRepository,
    private val EditRepository: ProductEditRepository,
    private val categoryRepository: ProductCategoryRepository,
    private val getValidUrisUseCase: GetValidUrisUseCase,
    private val getBytesByUriUseCase: GetBytesByUriUseCase,
    private val getMediaInfoUseCase: GetMediaInfoUseCase,
    private val getRotateUseCase: GetRotateUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    val adapter = SelectedPictureListAdapter(
        deleteSelectedImage = { deleteSelectedImage(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }

    private val _categoryEvent = MutableEventFlow<Response<List<ProductCategoryDto>>>()
    val categoryEvent = _categoryEvent.asEventFlow()

    private val _productRegistrationResponse = MutableEventFlow<Response<String>>()
    val productRegistrationResponse = _productRegistrationResponse.asEventFlow()

    private val _contentLengthVisible = MutableStateFlow(false)
    val contentLengthVisible: StateFlow<Boolean> get() = _contentLengthVisible

    private val _condition = MutableStateFlow(false)
    val condition: StateFlow<Boolean> get() = _condition

    var category = listOf<ProductCategoryDto>()
    private var categoryID = 0L

    var productId = 0L

    val selectedImageInfo = SelectedImageInfo()
    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val hopePrice = MutableStateFlow("")
    val openingBid = MutableStateFlow("")
    val tick = MutableStateFlow("")
    val expiration = MutableStateFlow("")

    init {
        requestProductCategory()
    }

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
        Collections.swap(list, fromPosition, toPosition)
        adapter.submitList(list)
        selectedImageInfo.uris = list
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
                    (hopePrice.value.isEmpty() || hopePrice.value.priceToLong() != null) &&
                    openingBid.value.priceToLong() != null &&
                    tick.value.priceToInt() != null &&
                    expiration.value.toIntOrNull() != null &&
                    content.value.isNotEmpty()
            )
        }
    }

    fun requestProductRegistration() {
        viewModelScope.launch {
            val imageList = getMultipartList()
            _productRegistrationResponse.emit(repository.postProductRegistration(imageList, getHashMap()))
        }
    }

    fun requestProductModification(productId: Long) {
        viewModelScope.launch {
            val imageList = getMultipartList()
            _productRegistrationResponse.emit(EditRepository.postProductEdit(productId, imageList, getHashMap()))
        }
    }

    private suspend fun getMultipartList(): List<MultipartBody.Part> =
        selectedImageInfo.uris.mapNotNull { uri ->
            getBytesByUriUseCase(uri)
                ?.toBitmap()
                ?.getRotatedBitmap(getRotateUseCase(uri))
                ?.getEditedBitmap(uri)
                ?.getMultipart(getMediaInfoUseCase(uri))
        }

    private fun requestProductCategory() {
        viewModelScope.launch {
            _categoryEvent.emit(categoryRepository.getProductCategory())
        }
    }

    fun initState(state: ProductEditorDto) {
        selectedImageInfo.uris = state.selectedImageInfo.uris
        selectedImageInfo.changeBitmaps.putAll(state.selectedImageInfo.changeBitmaps)
        viewModelScope.launch {
            categoryID = state.categoryId
            adapter.submitList(selectedImageInfo.uris.toMutableList())
            title.emit(state.title)
            hopePrice.emit(state.hopePrice)
            openingBid.emit(state.openingBid)
            tick.emit(state.tick)
            expiration.emit(state.expiration)
            content.emit(state.content)
        }
    }

    private fun getHashMap(): HashMap<String, RequestBody> {
        println(categoryID)
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
        return map
    }

    fun getProductRegistrationDto() = ProductEditorDto(
        selectedImageInfo,
        title.value,
        hopePrice.value,
        openingBid.value,
        tick.value,
        expiration.value,
        content.value,
        categoryID
    )

    fun setProductCategory(list: List<ProductCategoryDto>) {
        category = list
    }

    fun setCategoryID(index: Long) {
        categoryID = category[index.toInt()].categoryId
    }

    fun refreshImages() {
        if (selectedImageInfo.uris.isNotEmpty()) {
            // 유효한 선택 이미지 리스트로 갱신
            val validUris = getValidUrisUseCase(selectedImageInfo.uris)
            viewModelScope.launch {
                adapter.submitList(validUris)
            }
            if (validUris.isNotEmpty() && validUris.first() != selectedImageInfo.uris.first()) {
                adapter.notifyItemChanged(findSelectedImageIndex(validUris.first()))
            }
            selectedImageInfo.uris = validUris.toMutableList()
        }
    }

    private fun String?.toPlainRequestBody() = requireNotNull(this).toRequestBody("text/plain".toMediaTypeOrNull())

    private fun Bitmap.getEditedBitmap(uri: String) =
        selectedImageInfo.changeBitmaps[uri]?.let { bitmapInfo ->
            getRotatedBitmap(bitmapInfo.degree)
        } ?: this
}
