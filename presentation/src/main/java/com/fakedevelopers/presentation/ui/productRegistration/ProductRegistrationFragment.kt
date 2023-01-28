package com.fakedevelopers.presentation.ui.productRegistration

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.PermissionChecker
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductRegistrationFragment : ProductEditorFragment() {
    private val args: ProductRegistrationFragmentArgs by navArgs()

    override val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_productRegistrationFragment_to_productListFragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.productEditorDto?.let {
            viewModel.initState(it)
            if (it.selectedImageInfo.uris.isNotEmpty()) {
                ItemTouchHelper(DragAndDropCallback(viewModel.adapter))
                    .attachToRecyclerView(binding.recyclerProductEditor)
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding.includeProductEditorToolbar.buttonToolbarRegistration.setOnClickListener {
            if (viewModel.condition.value && checkPriceCondition()) {
                sendSnackBar("게시글 등록 요청")
                binding.includeProductEditorToolbar.buttonToolbarRegistration.isEnabled = false
                viewModel.requestProductRegistration()
            }
        }
    }

    override fun toPictureSelectFragment(permission: String) {
        val permissionCheck =
            PermissionChecker.checkCallingOrSelfPermission(requireContext(), permission)
        if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
            findNavController().navigate(
                ProductRegistrationFragmentDirections
                    .actionProductRegistrationFragmentToPictureSelectFragment(viewModel.getProductEditorDto())
            )
        } else {
            permissionLauncher.launch(permission)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
