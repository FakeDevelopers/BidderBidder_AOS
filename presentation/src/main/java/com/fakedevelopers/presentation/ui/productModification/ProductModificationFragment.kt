package com.fakedevelopers.presentation.ui.productModification

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductModificationFragment : ProductEditorFragment() {

    override val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productModificationFragment_to_productListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editorToolbarTitle = "내 물건 수정"
        viewModel.productId = args.productId
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
                .actionProductModificationFragmentToPictureSelectFragment(viewModel.getProductEditorDto())
        )
    }
}
