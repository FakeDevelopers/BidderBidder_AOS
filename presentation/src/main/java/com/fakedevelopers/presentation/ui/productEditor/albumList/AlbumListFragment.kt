package com.fakedevelopers.presentation.ui.productEditor.albumList

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.viewpager2.widget.ViewPager2
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAlbumListBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AlbumListFragment : BaseFragment<FragmentAlbumListBinding>(
    R.layout.fragment_album_list
) {

    @Inject
    lateinit var viewModelFactory: AlbumListViewModel.PathAssistedFactory

    private val viewModel by viewModels<AlbumListViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return viewModelFactory.create(args.albumPath) as T
            }
        }
    }

    private val args: AlbumListFragmentArgs by navArgs()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뷰 페이저가 열려있으면 페이저만 닫는다.
                if (viewModel.albumViewMode.value == AlbumViewState.PAGER) {
                    viewModel.setAlbumViewMode(AlbumViewState.GRID)
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }
    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentViewPagerIdx(position)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        if (args.selectedImageInfo.uris.isNotEmpty()) {
            viewModel.initSelectedImageList(args.selectedImageInfo)
            setCompleteTextVisibility(args.selectedImageInfo.uris.size)
        }
        binding.recyclerAlbumList.itemAnimator = null
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        viewModel.updateAlbumList()
        viewModel.checkSelectedImages(binding.viewpagerPictureSelect.currentItem)
    }

    private fun toProductEditor(selectedImageInfo: SelectedImageInfo) {
        selectedImageInfo.run {
            uris.clear()
            uris.addAll(viewModel.selectedImageInfo.uris)
            changeBitmaps.clear()
            changeBitmaps.putAll(viewModel.selectedImageInfo.changeBitmaps)
        }
        findNavController().run {
            if (backQueue.any { it.destination.id == R.id.productRegistrationFragment }) {
                navigate(
                    AlbumListFragmentDirections
                        .actionPictureSelectFragmentToProductRegistrationFragment(selectedImageInfo)
                )
            } else {
                popBackStack()
            }
        }
    }

    override fun initListener() {
        binding.toolbarAlbumList.run {
            textviewAlbumComplete.setOnClickListener {
                toProductEditor(args.selectedImageInfo)
            }
            textviewAlbumTitle.setOnClickListener {
                if (viewModel.albumViewMode.value == AlbumViewState.GRID) {
                    findNavController().navigate(
                        AlbumListFragmentDirections.actionPictureSelectFragmentToAlbumSelectFragment(
                            selectedImageInfo = viewModel.selectedImageInfo,
                            title = binding.toolbarAlbumList.textviewAlbumTitle.text.toString().substringBeforeLast(' ')
                        )
                    )
                }
            }
            buttonAlbumClose.setOnClickListener {
                findNavController().popBackStack()
            }
        }
        binding.viewpagerPictureSelect.registerOnPageChangeCallback(onPageChangeCallback)
        ItemTouchHelper(DragAndDropCallback(viewModel.selectedPictureAdapter))
            .attachToRecyclerView(binding.recyclerSelectedPicture)
    }

    override fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.event.collect { event ->
                handleEvent(event)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.albumTitle.collectLatest { title ->
                val toolbarTitle = getString(
                    R.string.album_list_title,
                    title.ifEmpty { getString(R.string.album_select_recent_images) }
                )
                binding.toolbarAlbumList.textviewAlbumTitle.text =
                    SpannableStringBuilder(toolbarTitle).apply {
                        setSpan(
                            RelativeSizeSpan(0.5f),
                            toolbarTitle.lastIndex,
                            toolbarTitle.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
            }
        }
    }

    private fun handleEvent(event: AlbumListViewModel.Event) {
        when (event) {
            is AlbumListViewModel.Event.AlbumList -> viewModel.updateAlbumList()
            is AlbumListViewModel.Event.ImageCount -> handleImageCount(event.count)
            is AlbumListViewModel.Event.OnListChange -> setCompleteTextVisibility(event.count)
            is AlbumListViewModel.Event.SelectErrorImage -> sendSnackBar(getString(R.string.album_selected_error_image))
            is AlbumListViewModel.Event.StartViewPagerIndex -> initViewPagerIndex(event.idx)
        }
    }

    private fun handleImageCount(count: Int) {
        binding.textviewPictureSelectCount.apply {
            text = if (count != -1) {
                setBackgroundResource(R.drawable.shape_picture_select_count)
                (count + 1).toString()
            } else {
                setBackgroundResource(R.drawable.shape_picture_select_empty)
                ""
            }
        }
    }

    private fun setCompleteTextVisibility(count: Int) {
        val state = count != 0
        val colorId = if (state) R.color.black else R.color.gray_80
        binding.toolbarAlbumList.run {
            textviewAlbumCount.isVisible = state
            textviewAlbumCount.text = count.toString()
            textviewAlbumComplete.isEnabled = state
            textviewAlbumComplete.setTextColor(ContextCompat.getColor(requireContext(), colorId))
        }
    }

    private fun initViewPagerIndex(idx: Int) {
        if (viewModel.currentViewPagerIdx == idx) {
            viewModel.setCurrentViewPagerIdx(idx)
        }
        binding.viewpagerPictureSelect.setCurrentItem(idx, false)
    }

    override fun onDestroyView() {
        binding.viewpagerPictureSelect.unregisterOnPageChangeCallback(onPageChangeCallback)
        backPressedCallback.remove()
        super.onDestroyView()
    }
}
