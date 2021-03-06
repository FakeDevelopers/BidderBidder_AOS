package com.fakedevelopers.bidderbidder.ui.product_registration

import android.Manifest
import android.content.ContentResolver
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
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
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
        val arrayAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_product_registration,
            viewModel.category
        ) {
            override fun getCount(): Int {
                return super.getCount() - 1
            }
        }
        binding.spinnerProductRegistrationCategory.apply {
            adapter = arrayAdapter
            setSelection(arrayAdapter.count)
        }
        initListener()
        initCollector()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        // ?????? ????????? ???????????? ??????????????? ???????????? ??????
        if (viewModel.urlList.value.isNotEmpty()) {
            // ????????? ?????? ????????? ???????????? ??????
            viewModel.setUrlList(contentResolverUtil.getValidList(viewModel.urlList.value))
        }
    }

    private fun toPictureSelectFragment() {
        // ????????? ?????? ????????? ????????? ????????????
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
        // ?????? ?????? ??????
        initEditTextFilter(binding.edittextProductRegistrationHopePrice, MAX_PRICE_LENGTH)
        initEditTextFilter(binding.edittextProductRegistrationOpeningBid, MAX_PRICE_LENGTH)
        initEditTextFilter(binding.edittextProductRegistrationTick, MAX_TICK_LENGTH)
        val expirationFilter = InputFilter { source, _, _, _, dstart, _ ->
            if (source == "0" && dstart == 0) "" else source.replace(IS_NOT_NUMBER.toRegex(), "")
        }
        // ?????? ?????? ?????? ??????
        binding.edittextProductRegistrationExpiration.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // ??????!
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
        // ?????? ????????????
        binding.imageviewSelectPicture.setOnClickListener {
            toPictureSelectFragment()
        }
        // ????????? ?????? ??????
        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                binding.includeProductRegistrationToolbar.buttonToolbarRegistration.isEnabled = false
                val list = mutableListOf<MultipartBody.Part>()
                viewModel.urlList.value.forEach { uri ->
                    getMultipart(Uri.parse(uri), requireActivity().contentResolver)?.let { it1 -> list.add(it1) }
                }
                viewModel.productRegistrationRequest(list)
            }
        }
        // ????????? ?????????
        keyboardVisibilityUtils = KeyboardVisibilityUtils(
            requireActivity().window,
            onHideKeyboard = {
                viewModel.setContentLengthVisibility(false)
            }
        )
        // ?????? ??????????????? ??????, ?????????
        binding.edittextProductRegistrationContent.apply {
            setOnClickListener {
                viewModel.setContentLengthVisibility(true)
            }
            setOnFocusChangeListener { _, hasFocus ->
                viewModel.setContentLengthVisibility(hasFocus)
            }
        }
        // ?????? ???????????? ??????
        binding.includeProductRegistrationToolbar.buttonToolbarBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun initEditTextFilter(editText: EditText, length: Int) {
        val priceFilter = InputFilter { source, _, _, _, _, _ ->
            source.replace("[^(0-9|,)]".toRegex(), "")
        }
        editText.filters = arrayOf(priceFilter, InputFilter.LengthFilter(length))
        editText.addTextChangedListener(PriceTextWatcher(editText) { viewModel.checkRegistrationCondition() })
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
        // ?????? ?????? api
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productRegistrationResponse.collectLatest {
                    if (it.isSuccessful) {
                        findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
                    } else {
                        binding.includeProductRegistrationToolbar.buttonToolbarRegistration.isEnabled = true
                        Logger.t("myImage").e(it.errorBody().toString())
                    }
                }
            }
        }
        // ??????
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
        // ??? ?????? ?????? ??? ?????? ??? textView??? visible ??????
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contentLengthVisible.collect {
                    if (binding.edittextProductRegistrationContent.isFocused != viewModel.contentLengthVisible.value) {
                        viewModel.setContentLengthVisibility(true)
                    }
                }
            }
        }
        // ?????? ??????
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.condition.collectLatest {
                    val color = if (it) Color.BLACK else Color.GRAY
                    binding.includeProductRegistrationToolbar.buttonToolbarRegistration.setTextColor(color)
                }
            }
        }
    }

    // ????????? <= ?????? ????????? ?????? ??????
    private fun checkPriceCondition(): Boolean {
        runCatching {
            Pair(
                viewModel.openingBid.value.replace(IS_NOT_NUMBER.toRegex(), "").toLong(),
                viewModel.hopePrice.value.replace(IS_NOT_NUMBER.toRegex(), "").toLong()
            )
        }.onSuccess {
            if (it.first >= it.second) {
                Toast.makeText(requireContext(), "?????? ???????????? ?????? ???????????? ????????? ?????????.", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun getMultipart(uri: Uri, contentResolver: ContentResolver): MultipartBody.Part? {
        return contentResolver.query(uri, null, null, null, null)?.let {
            if (it.moveToNext()) {
                // ?????? ?????? ??????
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
        const val MAX_TICK_LENGTH = 12
        const val MAX_CONTENT_LENGTH = 1000
        const val MAX_EXPIRATION_TIME = 72
        const val MAX_EXPIRATION_LENGTH = 3
        const val IS_NOT_NUMBER = "[^0-9]"
    }
}
