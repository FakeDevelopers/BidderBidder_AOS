package com.fakedevelopers.presentation.ui.productEditor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakedevelopers.domain.model.ProductCategoryDto
import com.fakedevelopers.domain.model.ProductEditorInfo
import com.fakedevelopers.domain.model.ProductWriteDto
import com.fakedevelopers.domain.usecase.GetBytesByUriUseCase
import com.fakedevelopers.domain.usecase.GetMediaInfoUseCase
import com.fakedevelopers.domain.usecase.GetProductCategoryUseCase
import com.fakedevelopers.domain.usecase.GetProductWriteUseCase
import com.fakedevelopers.domain.usecase.GetRotateUseCase
import com.fakedevelopers.domain.usecase.GetValidUrisUseCase
import com.fakedevelopers.domain.usecase.ProductModificationUseCase
import com.fakedevelopers.domain.usecase.ProductRegistrationUseCase
import com.fakedevelopers.domain.usecase.SetProductWriteUseCase
import com.fakedevelopers.presentation.model.ProductModificationDto
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class ProductEditorViewModel @Inject constructor(
    private val productModificationUseCase: ProductModificationUseCase,
    private val productRegistrationUseCase: ProductRegistrationUseCase,
    private val getProductCategoryUseCase: GetProductCategoryUseCase,
    private val getValidUrisUseCase: GetValidUrisUseCase,
    private val getBytesByUriUseCase: GetBytesByUriUseCase,
    private val getMediaInfoUseCase: GetMediaInfoUseCase,
    private val getRotateUseCase: GetRotateUseCase,
    private val getProductWriteUseCase: GetProductWriteUseCase,
    private val setProductWriteUseCase: SetProductWriteUseCase
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    val adapter = SelectedPictureListAdapter(
        deleteSelectedImage = { deleteSelectedImage(it) },
        findSelectedImageIndex = { findSelectedImageIndex(it) }
    ) { fromPosition, toPosition ->
        swapSelectedImage(fromPosition, toPosition)
    }

    private val _categoryEvent = MutableEventFlow<Result<List<ProductCategoryDto>>>()
    val categoryEvent = _categoryEvent.asEventFlow()

    private val _productEditorResponse = MutableEventFlow<Result<String>>()
    val productEditorResponse = _productEditorResponse.asEventFlow()

    private val _condition = MutableStateFlow(false)
    val condition: StateFlow<Boolean> get() = _condition

    var category = listOf<ProductCategoryDto>()
    private var categoryID = 0L

    private var productId = 0L

    val selectedImageInfo = SelectedImageInfo()

    var title = MutableStateFlow("")
    val content = MutableStateFlow("")
    var hopePrice = MutableStateFlow("")
    var openingBid = MutableStateFlow("")
    var tick = MutableStateFlow("")
    var expiration = MutableStateFlow("")

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
    fun checkEditorCondition() {
        viewModelScope.launch {
            // 희망가, 호가, 만료 시간은 0이 되면 안됨
            if (hopePrice.value == "0") {
                hopePrice.value = ""
            }
            if (tick.value == "0") {
                tick.value = ""
            }
            if (expiration.value == "0") {
                expiration.value = ""
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
            val imageInfo = getMultipartList()
            _productEditorResponse.emit(productRegistrationUseCase(getProductEditorInfo(imageInfo)))
        }
    }

    fun requestProductModification() {
        viewModelScope.launch {
            val imageInfo = getMultipartList()
            _productEditorResponse.emit(productModificationUseCase(productId, getProductEditorInfo(imageInfo)))
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
            _categoryEvent.emit(getProductCategoryUseCase())
        }
    }

    fun initState(
        selectedImageInfo: SelectedImageInfo? = null,
        productModificationDto: ProductModificationDto? = null
    ) {
        selectedImageInfo?.let {
            selectedImageInfo.uris = it.uris
            selectedImageInfo.changeBitmaps.putAll(it.changeBitmaps)
            adapter.submitList(it.uris.toMutableList())
        }
        productModificationDto?.let {
            viewModelScope.launch {
                title.emit(it.productTitle)
                hopePrice.emit(it.hopePrice.toString())
                openingBid.emit(it.openingBid.toString())
                tick.emit(it.tick.toString())
                expiration.emit(it.expirationDate)
                content.emit(it.productContent)
            }
            productId = it.productId
        }
    }

    private fun getProductEditorInfo(imageInfo: List<MultipartBody.Part>): ProductEditorInfo {
        val date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(System.currentTimeMillis() + expiration.value.toInt() * 3600000),
            ZoneId.of("Asia/Seoul")
        )

        return ProductEditorInfo(
            content.value,
            title.value,
            dateFormatter.format(date),
            hopePrice.value.replace(",", ""),
            openingBid.value.replace(",", ""),
            "0",
            tick.value.replace(",", ""),
            categoryID.toString(),
            imageInfo
        )
    }

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

    fun loadProductWrite() {
        viewModelScope.launch {
            val productWriteDto = getProductWriteUseCase()
            title.emit(productWriteDto.title)
            hopePrice.emit(productWriteDto.hopePrice)
            openingBid.emit(productWriteDto.openingBid)
            tick.emit(productWriteDto.tick)
            expiration.emit(productWriteDto.expiration)
            categoryID = productWriteDto.categoryId
            content.emit(productWriteDto.content)
        }
    }

    fun saveProductWrite(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        CoroutineScope(dispatcher).launch {
            setProductWriteUseCase(
                ProductWriteDto(
                    title = title.value,
                    hopePrice = hopePrice.value,
                    openingBid = openingBid.value,
                    tick = tick.value,
                    expiration = expiration.value,
                    content = content.value,
                    categoryId = categoryID
                )
            )
        }
    }

    fun clearProductWrite(dispatcher: CoroutineDispatcher = Dispatchers.IO) {
        CoroutineScope(dispatcher).launch {
            setProductWriteUseCase()
        }
    }

    private fun Bitmap.getEditedBitmap(uri: String) =
        selectedImageInfo.changeBitmaps[uri]?.let { bitmapInfo ->
            getRotatedBitmap(bitmapInfo.degree)
        } ?: this
}
