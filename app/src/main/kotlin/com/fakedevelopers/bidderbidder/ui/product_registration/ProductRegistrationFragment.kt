package com.fakedevelopers.bidderbidder.ui.product_registration

import android.Manifest
import android.content.ContentResolver
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.fakedevelopers.bidderbidder.ui.util.KeyboardVisibilityUtils
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@AndroidEntryPoint
class ProductRegistrationFragment : Fragment() {

    private lateinit var keyboardVisibilityUtils: KeyboardVisibilityUtils
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var _binding: FragmentProductRegistrationBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ProductRegistrationViewModel by viewModels()
    private val grey by lazy { resources.getColor(R.color.grey, requireActivity().theme) }
    private val black by lazy { resources.getColor(R.color.black, requireActivity().theme) }
    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
            }
        }
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
            if (it.urlList.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductRegistration)
            }
        }
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_product_registration,
            viewModel.category
        ) {
            override fun getCount(): Int {
                return super.getCount() - 1
            }
        }
        binding.spinnerProductRegistrationCategory.adapter = adapter
        binding.spinnerProductRegistrationCategory.setSelection(adapter.count)
        initListener()
        initCollector()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
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
        val priceFilters = arrayOf(
            object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    return if (dest.toString() == "0" && source.toString() == "0") {
                        ""
                    } else {
                        source.toString().replace("[^(0-9|,)]".toRegex(), "")
                    }
                }
            },
            InputFilter.LengthFilter(MAX_PRICE_LENGTH)
        )
        // 가격 필터 등록
        binding.edittextProductRegistrationHopePrice.also {
            it.filters = priceFilters
            it.addTextChangedListener(PriceTextWatcher(it) { viewModel.checkRegistrationCondition() })
        }
        binding.edittextProductRegistrationOpeningBid.also {
            it.filters = priceFilters
            it.addTextChangedListener(PriceTextWatcher(it) { viewModel.checkRegistrationCondition() })
        }
        binding.edittextProductRegistrationTick.also {
            it.filters = priceFilters
            it.addTextChangedListener(PriceTextWatcher(it) { viewModel.checkRegistrationCondition() })
        }
        val expirationFilter = arrayOf(
            object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence {
                    return source.toString().replace("[^0-9]".toRegex(), "")
                }
            },
            InputFilter.LengthFilter(MAX_EXPIRATION_LENGTH)
        )
        // 만료 시간 필터 등록
        binding.edittextProductRegistrationExpiration.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 안써!
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s.toString().replace("[^0-9]".toRegex(), "").toIntOrNull()?.let {
                        if (it > MAX_EXPIRATION_TIME) {
                            binding.edittextProductRegistrationExpiration.apply {
                                setText(MAX_EXPIRATION_TIME.toString())
                                setSelection(text.length)
                            }
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.checkRegistrationCondition()
                }
            })
            filters = expirationFilter
        }
        // 사진 가져오기
        binding.imageviewSelectPicture.setOnClickListener {
            toPictureSelectFragment()
        }
        // 게시글 작성 요청
        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                val list = mutableListOf<MultipartBody.Part>()
                viewModel.urlList.value.forEach { uri ->
                    getMultipart(Uri.parse(uri), requireActivity().contentResolver)?.let { it1 -> list.add(it1) }
                }
                viewModel.productRegistrationRequest(list)
            }
        }
        // 키보드 이벤트
        keyboardVisibilityUtils = KeyboardVisibilityUtils(
            requireActivity().window,
            onShowKeyboard = {
                if (binding.edittextProductRegistrationContent.isFocused) {
                    binding.textviewProductRegistrationContentLength.visibility = View.VISIBLE
                }
            },
            onHideKeyboard = {
                binding.textviewProductRegistrationContentLength.visibility = View.INVISIBLE
            }
        )
        // 툴바 뒤로가기 버튼
        binding.includeProductRegistrationToolbar.buttonToolbarBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
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
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productRegistrationResponse.collectLatest {
                    if (it.isSuccessful) {
                        findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
                    } else {
                        Logger.t("myImage").e(it.errorBody().toString())
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.content.collectLatest {
                    binding.textviewProductRegistrationContentLength.apply {
                        text = "${it.length} / $MAX_CONTENT_LENGTH"
                        setTextColor(if (it.length == MAX_CONTENT_LENGTH) Color.RED else Color.GRAY)
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.condition.collectLatest {
                    binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setTextColor(
                        if (it)
                            black
                        else
                            grey
                    )
                }
            }
        }
    }

    // 희망가 <= 최소 입찰가 인지 검사
    private fun checkPriceCondition(): Boolean {
        runCatching {
            Pair(
                viewModel.openingBid.value.replace("[^0-9]".toRegex(), "").toLong(),
                viewModel.hopePrice.value.replace("[^0-9]".toRegex(), "").toLong()
            )
        }.onSuccess {
            if (it.first >= it.second) {
                Toast.makeText(requireContext(), "최소 입찰가는 희망 가격보다 작아야 합니다.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun getMultipart(uri: Uri, contentResolver: ContentResolver): MultipartBody.Part? {
        return contentResolver.query(uri, null, null, null, null)?.let {
            if (it.moveToNext()) {
                // 절대 경로 얻기
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val displayName = it.getString(idx)
                val requestBody = object : RequestBody() {
                    override fun contentType(): MediaType? {
                        return contentResolver.getType(uri)?.toMediaType()
                    }
                    override fun writeTo(sink: BufferedSink) {
                        sink.writeAll(contentResolver.openInputStream(uri)?.source()!!)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        backPressedCallback.remove()
        keyboardVisibilityUtils.deleteKeyboardListeners()
    }

    companion object {
        const val MAX_PRICE_LENGTH = 17
        const val MAX_CONTENT_LENGTH = 1000
        const val MAX_EXPIRATION_TIME = 72
        const val MAX_EXPIRATION_LENGTH = 3
    }
}
