package com.fakedevelopers.presentation.ui.productModification

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.model.ProductModificationDto
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductModificationFragment : ProductEditorFragment() {

    private val args: ProductModificationFragmentArgs by navArgs()

    override val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productModificationFragment_to_productListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productModificationDto = args.productModificationDto
        viewModel.editorToolbarTitle = getString(R.string.product_modification_title)
        viewModel.productId = productModificationDto.productId
        Logger.t("datatest").i("$productModificationDto")
        initState(productModificationDto)
    }

    private fun initState(productModificationDto: ProductModificationDto) {
        binding.edittextProductEditorTitle.setText(productModificationDto.productTitle)
        binding.edittextProductEditorHopePrice.setText(productModificationDto.hopePrice.toString())
        binding.edittextProductEditorContent.setText(productModificationDto.productContent)
        binding.edittextProductEditorTick.setText(productModificationDto.tick.toString())
        binding.edittextProductEditorOpeningBid.setText(productModificationDto.openingBid.toString())
        binding.edittextProductEditorExpiration.setText(productModificationDto.expirationDate)

    }

    override fun initListener() {
        super.initListener()
        // 게시글 수정 요청
        binding.includeProductEditorToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 수정 요청")
                binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = false
                viewModel.requestProductModification(viewModel.productId)
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
