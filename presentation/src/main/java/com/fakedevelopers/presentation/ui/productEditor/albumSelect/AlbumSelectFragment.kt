package com.fakedevelopers.presentation.ui.productEditor.albumSelect

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAlbumSelectBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AlbumSelectFragment : BaseFragment<FragmentAlbumSelectBinding>(
    R.layout.fragment_album_select
) {

    private val viewModel: AlbumSelectViewModel by viewModels()
    private val args: AlbumSelectFragmentArgs by navArgs()

    private val adapter: AlbumSelectAdapter by lazy {
        AlbumSelectAdapter { path ->
            findNavController().navigate(
                AlbumSelectFragmentDirections.actionAlbumSelectFragmentToPictureSelectFragment(
                    albumPath = path,
                    selectedImageInfo = args.selectedImageInfo
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initAlbumInfo(getString(R.string.album_select_recent_images))
        binding.recyclerAlbumSelect.adapter = adapter
        if (args.selectedImageInfo.uris.isNotEmpty()) {
            binding.toolbarAlbumSelect.run {
                textviewAlbumComplete.isEnabled = true
                textviewAlbumComplete.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                textviewAlbumCount.text = args.selectedImageInfo.uris.size.toString()
            }
        }
    }

    override fun initListener() {
        val toolbarTitle = getString(R.string.album_select_title, args.title)
        binding.toolbarAlbumSelect.textviewAlbumTitle.run {
            text = SpannableStringBuilder(toolbarTitle).apply {
                setSpan(
                    RelativeSizeSpan(0.5f),
                    toolbarTitle.lastIndex,
                    toolbarTitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            setOnClickListener {
                findNavController().popBackStack()
            }
        }
        binding.toolbarAlbumSelect.buttonAlbumClose.setOnClickListener {
            // AlbumListFragment와 동일한 로직, 추상화가 필요
            findNavController().run {
                if (backQueue.any { it.destination.id == R.id.productRegistrationFragment }) {
                    navigate(
                        AlbumSelectFragmentDirections
                            .actionAlbumSelectFragmentToProductRegistrationFragment(args.selectedImageInfo)
                    )
                } else {
                    // 이렇게 쓰면 안됨. 반드시 수정해야함
                    popBackStack()
                    popBackStack()
                }
            }
        }
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.albumInfoEvent.collectLatest {
                adapter.submitList(it)
            }
        }
    }
}
