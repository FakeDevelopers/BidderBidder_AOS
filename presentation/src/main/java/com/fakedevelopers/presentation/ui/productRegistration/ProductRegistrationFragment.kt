package com.fakedevelopers.presentation.ui.productRegistration

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductRegistrationFragment : ProductEditorFragment(
    R.string.product_registration_title
) {
    private val args: ProductRegistrationFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadProductWrite()
    }

    override fun initListener() {
        super.initListener()
        // 게시글 등록 요청
        binding.includeProductEditorToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 등록 요청")
                binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = false
                viewModel.requestProductRegistration()
            }
        }
    }

    override fun initCollector() {
        super.initCollector()
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.productEditorResponse.collectLatest {
                if (it.isSuccess) {
                    viewModel.clearProductWrite()
                }
            }
        }
    }

    override fun navigatePictureSelectFragment() {
        findNavController().navigate(
            ProductRegistrationFragmentDirections
                .actionProductRegistrationFragmentToPictureSelectFragment(viewModel.selectedImageInfo)
        )
    }

    override fun initSelectedImages() {
        args.selectedImageInfo?.let {
            viewModel.initState(it)
            if (it.uris.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductEditor)
            }
        }
    }

    override fun onDestroyView() {
        viewModel.saveProductWrite()
        super.onDestroyView()
    }
}
