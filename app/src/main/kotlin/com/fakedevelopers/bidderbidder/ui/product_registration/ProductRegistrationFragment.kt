package com.fakedevelopers.bidderbidder.ui.product_registration

import android.Manifest
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkCallingOrSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductRegistrationBinding
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

@AndroidEntryPoint
class ProductRegistrationFragment : Fragment() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var _binding: FragmentProductRegistrationBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ProductRegistrationViewModel by viewModels()

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
        if (!args.selectedImageList.isNullOrEmpty()) {
            viewModel.setImageList(args.selectedImageList!!.toList())
            ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                .attachToRecyclerView(binding.recyclerProductRegistration)
        }
        initListener()
        initCollector()
    }

    private fun toPictureSelectFragment() {
        // 미디어 접근 권한이 없으면 안됩니다
        val permissionCheck = checkCallingOrSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            findNavController().navigate(R.id.action_productRegistrationFragment_to_pictureSelectFragment)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun initListener() {
        // 사진 가져오기
        binding.button.setOnClickListener {
            toPictureSelectFragment()
        }
        // 게시글 작성 요청
        binding.button2.setOnClickListener {
            // viewModel.productRegistrationRequest()
            Toast.makeText(requireContext(), "지금은 안돼", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launchWhenStarted {
            viewModel.productRegistrationResponse.collect {
                if (it.isSuccessful) {
                    Logger.t("myImage").i(it.body().toString())
                } else {
                    Logger.t("myImage").e(it.errorBody().toString())
                }
            }
        }
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
    }
}
