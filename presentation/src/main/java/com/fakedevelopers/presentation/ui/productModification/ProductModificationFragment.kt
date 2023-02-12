package com.fakedevelopers.presentation.ui.productModification

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductModificationFragment : ProductEditorFragment(
    R.string.product_modification_title
) {
    private val args: ProductModificationFragmentArgs by navArgs()

    override fun initListener() {
        super.initListener()
        // 게시글 수정 요청
        binding.includeProductEditorToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 수정 요청")
                binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = false
                viewModel.requestProductModification()
            }
        }
    }

    override fun navigatePictureSelectFragment() {
        findNavController().navigate(
            ProductModificationFragmentDirections
                .actionProductModificationFragmentToPictureSelectFragment(viewModel.selectedImageInfo)
        )
    }

    override fun initSelectedImages() {
        viewModel.initState(args.selectedImageInfo, args.productModificationDto)
        if (args.selectedImageInfo?.uris.isNullOrEmpty().not()) {
            ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                .attachToRecyclerView(binding.recyclerProductEditor)
        }
    }
}
