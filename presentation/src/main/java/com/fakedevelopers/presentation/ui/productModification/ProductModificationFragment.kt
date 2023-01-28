package com.fakedevelopers.presentation.ui.productModification

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.PermissionChecker
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        binding.includeProductEditorToolbar.textviewToolbarTitle.text = getString(R.string.product_modification_title)
        args.productEditorDto?.let {
            viewModel.initState(it)
            if (it.selectedImageInfo.uris.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductEditor)
            }
        }

        viewModel.productId = args.productId
    }

    override fun initListener() {
        super.initListener()
        // 게시글 수정 요청
        binding.includeProductEditorToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 수정 요청")
                binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = false
                lifecycleScope.launch {
                    viewModel.requestProductModification(viewModel.productId)
                }
            }
        }
    }

    override fun toPictureSelectFragment(permission: String) {
        val permissionCheck =
            PermissionChecker.checkCallingOrSelfPermission(requireContext(), permission)
        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            findNavController().navigate(
                ProductModificationFragmentDirections
                    .actionProductModificationFragmentToPictureSelectFragment(viewModel.getProductEditorDto())
            )
        } else {
            permissionLauncher.launch(permission)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
