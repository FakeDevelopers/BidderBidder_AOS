package com.fakedevelopers.presentation.ui.productRegistration

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductRegistrationFragment : ProductEditorFragment() {

    override val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.editorToolbarTitle = "내 물건 등록"
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

    override fun navigatePictureSelectFragment() {
        findNavController().navigate(
            ProductRegistrationFragmentDirections
                .actionProductRegistrationFragmentToPictureSelectFragment(viewModel.getProductEditorDto())
        )
    }
}
