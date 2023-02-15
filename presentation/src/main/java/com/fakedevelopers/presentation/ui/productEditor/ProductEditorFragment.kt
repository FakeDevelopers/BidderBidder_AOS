package com.fakedevelopers.presentation.ui.productEditor

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.domain.model.ProductCategoryDto
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentProductEditorBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.IS_NOT_NUMBER
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.MAX_CONTENT_LENGTH
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.MAX_EXPIRATION_LENGTH
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.MAX_EXPIRATION_TIME
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.MAX_PRICE_LENGTH
import com.fakedevelopers.presentation.ui.productEditor.PriceTextWatcher.Companion.MAX_TICK_LENGTH
import com.fakedevelopers.presentation.ui.util.ApiErrorHandler
import com.fakedevelopers.presentation.ui.util.KeyboardVisibilityUtils
import com.fakedevelopers.presentation.ui.util.priceToInt
import com.fakedevelopers.presentation.ui.util.priceToLong
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.logging.helper.stringify
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
abstract class ProductEditorFragment(
    @StringRes private val titleId: Int
) : BaseFragment<FragmentProductEditorBinding>(
    R.layout.fragment_product_editor
) {

    protected val viewModel: ProductEditorViewModel by viewModels()
    private val expirationFilter by lazy {
        InputFilter { source, _, _, _, dstart, _ ->
            if (source == "0" && dstart == 0) "" else source.replace(IS_NOT_NUMBER.toRegex(), "")
        }
    }

    private val permissionLauncher: ActivityResultLauncher<String> by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkStoragePermission()
            } else {
                sendSnackBar(getString(R.string.read_external_storage))
            }
        }
    }

    private val keyboardVisibilityUtils: KeyboardVisibilityUtils by lazy {
        KeyboardVisibilityUtils(
            requireActivity().window,
            onHideKeyboard = {
                binding.textviewProductEditorContentLength.isVisible = false
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.includeProductEditorToolbar.textviewToolbarTitle.setText(titleId)
        initSelectedImages()
        if (viewModel.category.isNotEmpty()) {
            setCategory(viewModel.category)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshImages()
        binding.textviewProductEditorContentLength.isVisible =
            binding.edittextProductEditorContent.isFocused
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
            navigatePictureSelectFragment()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    protected abstract fun navigatePictureSelectFragment()

    protected abstract fun initSelectedImages()

    override fun initListener() {
        // 가격 필터 등록
        binding.edittextProductEditorHopePrice.setPriceFilter(MAX_PRICE_LENGTH)
        binding.edittextProductEditorOpeningBid.setPriceFilter(MAX_PRICE_LENGTH)
        binding.edittextProductEditorTick.setPriceFilter(MAX_TICK_LENGTH)
        // 만료 시간 필터 등록
        binding.edittextProductEditorExpiration.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 안써!
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    s.toString().priceToInt()?.let {
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
                    viewModel.checkEditorCondition()
                }
            })
            filters = arrayOf(expirationFilter, InputFilter.LengthFilter(MAX_EXPIRATION_LENGTH))
        }
        // 사진 가져오기
        binding.imageviewSelectPicture.setOnClickListener {
            checkStoragePermission()
        }
        // 본문 에딧텍스트 터치, 포커싱
        binding.edittextProductEditorContent.apply {
            setOnClickListener {
                binding.textviewProductEditorContentLength.isVisible = true
            }
            setOnFocusChangeListener { _, hasFocus ->
                binding.textviewProductEditorContentLength.isVisible = hasFocus
            }
        }
        // 툴바 뒤로가기 버튼
        binding.includeProductEditorToolbar.buttonToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.spinnerProductEditorCategory.apply {
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

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.productEditorResponse.collectLatest {
                if (it.isSuccess) {
                    findNavController().popBackStack()
                } else {
                    binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = true
                    sendSnackBar("글쓰기에 실패했어요~")
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.content.collectLatest {
                binding.textviewProductEditorContentLength.apply {
                    text = getString(R.string.product_editor_contents_length, it.length, MAX_CONTENT_LENGTH)
                    val color = if (it.length == MAX_CONTENT_LENGTH) Color.RED else Color.GRAY
                    setTextColor(color)
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.condition.collectLatest {
                val color = if (it) Color.BLACK else Color.GRAY
                binding.includeProductEditorToolbar.buttonToolbarRegistration.setTextColor(color)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.categoryEvent.collectLatest { result ->
                val category = result.getOrNull()
                if (category.isNullOrEmpty().not()) {
                    handleCategoryResult(category)
                } else {
                    ApiErrorHandler.printMessage(result.exceptionOrNull()?.stringify().toString())
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
    protected fun checkPriceCondition(): Boolean {
        val openingBid = viewModel.openingBid.value.priceToLong() ?: return false
        val hopePrice = viewModel.hopePrice.value.priceToLong()
        if (hopePrice != null && hopePrice <= openingBid) {
            sendSnackBar(getString(R.string.product_editor_error_minimum_bid))
            return false
        }
        return true
    }

    private fun setCategory(category: List<ProductCategoryDto>) {
        val arrayAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_product_registration,
            category.map { it.categoryName }
        )
        binding.spinnerProductEditorCategory.apply {
            adapter = arrayAdapter
            setSelection(arrayAdapter.count - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        keyboardVisibilityUtils.deleteKeyboardListeners()
    }

    private fun EditText.setPriceFilter(length: Int) {
        PriceTextWatcher.addEditTextFilter(
            this,
            length
        ) { viewModel.checkEditorCondition() }
    }
}
