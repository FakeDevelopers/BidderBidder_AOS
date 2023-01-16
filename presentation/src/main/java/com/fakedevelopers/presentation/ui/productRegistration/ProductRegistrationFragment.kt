package com.fakedevelopers.presentation.ui.productRegistration

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentProductRegistrationBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.IS_NOT_NUMBER
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.MAX_CONTENT_LENGTH
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.MAX_EXPIRATION_LENGTH
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.MAX_EXPIRATION_TIME
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.MAX_PRICE_LENGTH
import com.fakedevelopers.presentation.ui.productRegistration.PriceTextWatcher.Companion.MAX_TICK_LENGTH
import com.fakedevelopers.presentation.ui.util.AlbumImageUtils
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.ContentResolverUtil
import com.fakedevelopers.presentation.ui.util.KeyboardVisibilityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import javax.inject.Inject

@AndroidEntryPoint
class ProductRegistrationFragment : BaseFragment<FragmentProductRegistrationBinding>(
    R.layout.fragment_product_registration
) {

    @Inject
    lateinit var contentResolverUtil: ContentResolverUtil

    @Inject
    lateinit var albumImageUtils: AlbumImageUtils

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private val viewModel: ProductRegistrationViewModel by viewModels()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        val args: ProductRegistrationFragmentArgs by navArgs()
        args.productRegistrationDto?.let {
            viewModel.initState(it)
            if (it.selectedImageInfo.uris.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductRegistration)
            }
        }
        initResultLauncher()
        initListener()
        initCollector()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        // 선택 이미지 리스트가 존재한다면 유효한지 검사
        if (viewModel.selectedImageInfo.uris.isNotEmpty()) {
            // 유효한 선택 이미지 리스트로 갱신
            viewModel.setUrlList(contentResolverUtil.getValidList(viewModel.selectedImageInfo.uris))
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            toPictureSelectFragment(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            toPictureSelectFragment(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun toPictureSelectFragment(permission: String) {
        val permissionCheck = checkCallingOrSelfPermission(requireContext(), permission)
        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            findNavController().navigate(
                ProductRegistrationFragmentDirections
                    .actionProductRegistrationFragmentToPictureSelectFragment(viewModel.getProductRegistrationDto())
            )
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun initListener() {
        // 가격 필터 등록
        initEditTextFilter(binding.edittextProductRegistrationHopePrice, MAX_PRICE_LENGTH)
        initEditTextFilter(binding.edittextProductRegistrationOpeningBid, MAX_PRICE_LENGTH)
        initEditTextFilter(binding.edittextProductRegistrationTick, MAX_TICK_LENGTH)
        val expirationFilter = InputFilter { source, _, _, _, dstart, _ ->
            if (source == "0" && dstart == 0) "" else source.replace(IS_NOT_NUMBER.toRegex(), "")
        }
        // 만료 시간 필터 등록
        binding.edittextProductRegistrationExpiration.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 안써!
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s.toString().replace(IS_NOT_NUMBER.toRegex(), "").toIntOrNull()?.let {
                        if (it > MAX_EXPIRATION_TIME) {
                            setText(MAX_EXPIRATION_TIME.toString())
                            setSelection(text.length)
                        } else if (it.toString().length != text.length) {
                            setText(it.toString())
                            setSelection(text.length)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.checkRegistrationCondition()
                }
            })
            filters = arrayOf(expirationFilter, InputFilter.LengthFilter(MAX_EXPIRATION_LENGTH))
        }
        // 사진 가져오기
        binding.imageviewSelectPicture.setOnClickListener {
            checkStoragePermission()
        }
        // 게시글 작성 요청
        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 등록 요청")
                binding.includeProductRegistrationToolbar.buttonToolbarRegistration.isEnabled = false
                lifecycleScope.launch {
                    viewModel.requestProductRegistration(getMultipartList())
                }
            }
        }
        // 키보드 이벤트
        keyboardVisibilityUtils = KeyboardVisibilityUtils(
            requireActivity().window,
            onHideKeyboard = {
                viewModel.setContentLengthVisibility(false)
            }
        )
        // 본문 에딧텍스트 터치, 포커싱
        binding.edittextProductRegistrationContent.apply {
            setOnClickListener {
                viewModel.setContentLengthVisibility(true)
            }
            setOnFocusChangeListener { _, hasFocus ->
                viewModel.setContentLengthVisibility(hasFocus)
            }
        }
        // 툴바 뒤로가기 버튼
        binding.includeProductRegistrationToolbar.buttonToolbarBack.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

        binding.spinnerProductRegistrationCategory.apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.setCategoryID(selectedItemId)
                }
            }
        }
    }

    private fun initEditTextFilter(editText: EditText, length: Int) {
        PriceTextWatcher.addEditTextFilter(editText, length) { viewModel.checkRegistrationCondition() }
    }

    private fun initResultLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    checkStoragePermission()
                } else {
                    sendSnackBar(getString(R.string.read_external_storage))
                }
            }
    }

    private fun initCollector() {
        // 등록 요청 api
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productRegistrationResponse.collectLatest {
                    if (it.isSuccessful) {
                        findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
                    } else {
                        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.isEnabled = true
                        sendSnackBar("글쓰기에 실패했어요~")
                    }
                }
            }
        }
        // 본문
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.content.collectLatest {
                    binding.textviewProductRegistrationContentLength.apply {
                        text = "${it.length} / $MAX_CONTENT_LENGTH"
                        val color = if (it.length == MAX_CONTENT_LENGTH) Color.RED else Color.GRAY
                        setTextColor(color)
                    }
                }
            }
        }
        // 홈 화면 이동 시 글자 수 textView의 visible 처리
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contentLengthVisible.collect {
                    if (binding.edittextProductRegistrationContent.isFocused != viewModel.contentLengthVisible.value) {
                        viewModel.setContentLengthVisibility(true)
                    }
                }
            }
        }
        // 등록 버튼
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.condition.collectLatest {
                    val color = if (it) Color.BLACK else Color.GRAY
                    binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setTextColor(color)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryEvent.collectLatest { result ->
                    if (result.isSuccessful) {
                        handleCategoryResult(result.body())
                    } else {
                        ApiErrorHandler.printErrorMessage(result.errorBody())
                    }
                }
            }
        }
    }

    private fun handleCategoryResult(body: List<ProductCategoryDto>?) {
        body?.let { category ->
            viewModel.setProductCategory(category)
            setCategory(category)
        } ?: run {
            ApiErrorHandler.printMessage("카테고리 api의 body가 비었어")
        }
    }

    // 희망가 <= 최소 입찰가 인지 검사
    private fun checkPriceCondition(): Boolean {
        val openingBid = viewModel.openingBid.value.replace(IS_NOT_NUMBER.toRegex(), "").toLongOrNull() ?: return false
        val hopePrice = viewModel.hopePrice.value.replace(IS_NOT_NUMBER.toRegex(), "").toLongOrNull()
        if (hopePrice != null && hopePrice <= openingBid) {
            sendSnackBar(getString(R.string.product_registration_error_minimum_bid))
            return false
        }
        return true
    }

    private suspend fun getMultipartList(): List<MultipartBody.Part> {
        val result = lifecycleScope.async {
            val list = mutableListOf<MultipartBody.Part>()
            viewModel.selectedImageInfo.uris.forEach { uri ->
                albumImageUtils.getBitmapByURI(uri)?.let { bitmap ->
                    val editedBitmap = getEditedBitmap(uri, bitmap)
                    getMultipart(uri, editedBitmap)?.let { multiPart -> list.add(multiPart) }
                }
            }
            list.toList()
        }
        return result.await()
    }

    private fun getEditedBitmap(uri: String, bitmap: Bitmap): Bitmap {
        // 맵에 없다면 변경 사항이 없는 것이므로 쌩 비트맵 반환
        return viewModel.selectedImageInfo.changeBitmaps[uri]?.let { bitmapInfo ->
            // 회전. 나중엔 이미지 자르는 작업도 들어가겠죠
            albumImageUtils.getRotateBitmap(bitmap, bitmapInfo.degree)
        } ?: bitmap
    }

    private fun getMultipart(uri: String, bitmap: Bitmap): MultipartBody.Part? {
        val (mimeType, extension) = albumImageUtils.getMimeTypeAndExtension(uri)
        return requireContext().contentResolver.query(Uri.parse(uri), null, null, null, null)?.let {
            if (it.moveToNext()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx < 0) {
                    return null
                }
                val displayName = it.getString(idx)
                val requestBody = object : RequestBody() {
                    override fun contentType(): MediaType {
                        return mimeType.toMediaType()
                    }
                    override fun writeTo(sink: BufferedSink) {
                        bitmap.compress(Bitmap.CompressFormat.valueOf(extension), COMPRESS_QUALITY, sink.outputStream())
                    }
                }
                it.close()
                MultipartBody.Part.createFormData("files", displayName, requestBody)
            } else {
                it.close()
                null
            }
        }
    }

    private fun setCategory(category: List<ProductCategoryDto>) {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_product_registration,
            category.map { it.categoryName }
        )
        binding.spinnerProductRegistrationCategory.apply {
            adapter = arrayAdapter
            setSelection(arrayAdapter.count - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backPressedCallback.remove()
        keyboardVisibilityUtils.deleteKeyboardListeners()
    }

    companion object {
        private const val COMPRESS_QUALITY = 70
    }
}
