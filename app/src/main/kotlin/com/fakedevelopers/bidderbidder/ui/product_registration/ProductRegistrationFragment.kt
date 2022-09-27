package com.fakedevelopers.bidderbidder.ui.product_registration

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductRegistrationBinding
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.IS_NOT_NUMBER
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.MAX_CONTENT_LENGTH
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.MAX_EXPIRATION_LENGTH
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.MAX_EXPIRATION_TIME
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.MAX_PRICE_LENGTH
import com.fakedevelopers.bidderbidder.ui.product_registration.PriceTextWatcher.Companion.MAX_TICK_LENGTH
import com.fakedevelopers.bidderbidder.ui.product_registration.album_list.AlbumImageUtils
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
import com.fakedevelopers.bidderbidder.ui.util.KeyboardVisibilityUtils
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

@AndroidEntryPoint
class ProductRegistrationFragment : Fragment() {

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var _binding: FragmentProductRegistrationBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ProductRegistrationViewModel by viewModels()
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
            }
        }
    }
    private val contentResolverUtil by lazy {
        ContentResolverUtil(requireContext())
    }
    private val albumImageUtils by lazy {
        AlbumImageUtils(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        initResultLauncher()
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_product_registration,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ProductRegistrationFragmentArgs by navArgs()
        args.productRegistrationDto?.let {
            viewModel.initState(it)
            if (it.selectedImageInfo.uris.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductRegistration)
            }
        }
        viewModel.productCategoryRequest()
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

    private fun toPictureSelectFragment() {
        // 미디어 접근 권한이 없으면 안됩니다
        val permissionCheck = checkCallingOrSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            findNavController().navigate(
                ProductRegistrationFragmentDirections
                    .actionProductRegistrationFragmentToPictureSelectFragment(viewModel.getProductRegistrationDto())
            )
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                            binding.edittextProductRegistrationExpiration.apply {
                                setText(MAX_EXPIRATION_TIME.toString())
                                setSelection(text.length)
                            }
                        } else if (it.toString().length != text.length) {
                            setText(it.toString())
                            setSelection(text.length)
                        }
                        it
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
            toPictureSelectFragment()
        }
        // 게시글 작성 요청
        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                Toast.makeText(requireContext(), "게시글 등록 요청", Toast.LENGTH_SHORT).show()
                binding.includeProductRegistrationToolbar.buttonToolbarRegistration.isEnabled = false
                lifecycleScope.launch {
                    viewModel.productRegistrationRequest(getMultipartList())
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
                    toPictureSelectFragment()
                } else {
                    Toast.makeText(requireContext(), R.string.read_external_storage, Toast.LENGTH_SHORT).show()
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
                        Logger.t("myImage").e(it.errorBody().toString())
                        Toast.makeText(requireContext(), "글쓰기에 실패했어요~", Toast.LENGTH_SHORT).show()
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
            viewModel.productCategory.collectLatest {
                if (it.isNotEmpty()) {
                    viewModel.setCategoryList(it)
                    setCategory(viewModel.category)
                }
            }
        }
    }

    // 희망가 <= 최소 입찰가 인지 검사
    private fun checkPriceCondition(): Boolean {
        runCatching {
            Pair(
                viewModel.openingBid.value.replace(IS_NOT_NUMBER.toRegex(), "").toLong(),
                viewModel.hopePrice.value.replace(IS_NOT_NUMBER.toRegex(), "").toLong()
            )
        }.onSuccess {
            if (it.first >= it.second) {
                Toast.makeText(requireContext(), "최소 입찰가는 희망 가격보다 작아야 합니다.", Toast.LENGTH_SHORT).show()
                return false
            }
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
            return@async list.toList()
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
                        bitmap.compress(Bitmap.CompressFormat.valueOf(extension), 100, sink.outputStream())
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

    private fun setCategory(category: List<String>) {
        val arrayAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_product_registration,
            category
        ) {}
        binding.spinnerProductRegistrationCategory.apply {
            adapter = arrayAdapter
            setSelection(arrayAdapter.count - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        backPressedCallback.remove()
        keyboardVisibilityUtils.deleteKeyboardListeners()
    }
}
