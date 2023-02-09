package com.fakedevelopers.presentation.ui.productEditor.albumSelect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAlbumSelectBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import kotlinx.coroutines.flow.collectLatest

class AlbumSelectFragment : BaseFragment<FragmentAlbumSelectBinding>(
    R.layout.fragment_album_select
) {

    private val viewModel: AlbumSelectViewModel by viewModels()
    private val args: AlbumSelectFragmentArgs by navArgs()

    private val adapter: AlbumSelectAdapter by lazy {
        AlbumSelectAdapter { path ->
            findNavController().navigate(
                AlbumSelectFragmentDirections.actionAlbumSelectFragmentToPictureSelectFragment(
                    albumName = path,
                    selectedImageInfo = args.selectedImageInfo
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCollector()
        binding.recyclerAlbumSelect.adapter = adapter
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.albumInfoEvent.collectLatest {
                adapter.submitList(it)
            }
        }
    }
}
