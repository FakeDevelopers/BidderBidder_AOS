package com.fakedevelopers.bidderbidder.ui.product_registration

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentProductRegistrationBinding
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source


@AndroidEntryPoint
class ProductRegistrationFragment: Fragment() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var _binding: FragmentProductRegistrationBinding

    private val productRegistrationViewModel: ProductRegistrationViewModel by viewModels()
    private val binding get() = _binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate<FragmentProductRegistrationBinding?>(inflater, R.layout.fragment_product_registration, container, false).also {
            it.vm = productRegistrationViewModel
            it.lifecycleOwner = this
        }
        Logger.addLogAdapter(AndroidLogAdapter())
        initResultLauncher()
        initObserver()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 사진 가져오기
        binding.button.setOnClickListener {
            getPictures()
        }
        // 요청
        binding.button2.setOnClickListener {
            productRegistrationViewModel.productRegistrationRequest()
        }
    }

    private fun getPictures() {
        // 미디어 접근 권한이 없으면 안됩니다
        if(checkCallingOrSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED){
            val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                action = Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            activityResultLauncher.launch(albumIntent)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun initResultLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                if(it.resultCode == RESULT_OK){
                    if(it.data?.clipData != null){
                        val len = it.data?.clipData!!.itemCount
                        for(i in 0 until len) {
                            it.data?.clipData!!.getItemAt(i).uri.let { uri ->
                                productRegistrationViewModel.imageList.add(getMultipart(uri, requireActivity().contentResolver)!!)
                            }
                        }
                    } else {
                        it.data?.data.let { uri ->
                            productRegistrationViewModel.imageList.add(getMultipart(uri!!, requireActivity().contentResolver)!!)
                        }
                    }
                }
            }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
                if(isGranted){
                    getPictures()
                } else {
                    Toast.makeText(requireContext(), R.string.permission_read_external_storage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initObserver() {
        productRegistrationViewModel.productRegistrationResponse.observe(viewLifecycleOwner) {
            if(it.isSuccessful){
                Logger.t("myImage").i(it.body().toString())
            } else {
                Logger.t("myImage").e(it.errorBody().toString())
            }
        }
    }

    private fun getMultipart(uri: Uri, contentResolver: ContentResolver): MultipartBody.Part? {
        return contentResolver.query(uri, null, null, null, null)?.let{
            if (it.moveToNext()){
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
}
