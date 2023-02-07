package com.fakedevelopers.presentation.ui.productEditor.albumList

import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAlbumListBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.productEditor.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productEditor.ProductEditorDto
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumListFragment : BaseFragment<FragmentAlbumListBinding>(
    R.layout.fragment_album_list
) {

    private val viewModel: AlbumListViewModel by viewModels()
    private val args: AlbumListFragmentArgs by navArgs()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뷰 페이저가 열려있으면 페이저만 닫는다.
                if (viewModel.albumViewMode.value == AlbumViewState.PAGER) {
                    viewModel.setAlbumViewMode(AlbumViewState.GRID)
                } else {
                    toProductRegistration(args.productEditorDto)
                }
            }
        }
    }
    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setPagerUI(position)
            }
        }
    }

    // 외부 저장소에 변화가 생기면 얘가 호출이 됩니다.
    private val contentObserver by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                if (uri != null) {
                    viewModel.onAlbumListUpdated(uri.toString())
                }
            }
        }
    }

    private val albumLayoutManager by lazy {
        object : GridLayoutManager(requireContext(), 3) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                viewModel.scrollToTop()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        if (args.productEditorDto.selectedImageInfo.uris.isNotEmpty()) {
            viewModel.initSelectedImageList(args.productEditorDto.selectedImageInfo)
            binding.buttonAlbumListComplete.visibility = View.VISIBLE
        }
        requireActivity().contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
        binding.recyclerAlbumList.run {
            layoutManager = albumLayoutManager
            itemAnimator = null
        }
        initListener()
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        viewModel.updateAlbumList()
        viewModel.checkSelectedImages(binding.viewpagerPictureSelect.currentItem)
    }

    private fun toProductRegistration(dto: ProductEditorDto) {
        dto.selectedImageInfo.apply {
            uris.clear()
            uris.addAll(viewModel.selectedImageInfo.uris)
            changeBitmaps.clear()
            changeBitmaps.putAll(viewModel.selectedImageInfo.changeBitmaps)
        }
        // 선택한 이미지 uri를 들고 돌아갑니다
        findNavController().popBackStack()
    }

    private fun initListener() {
        binding.buttonAlbumListComplete.setOnClickListener {
            toProductRegistration(args.productEditorDto)
        }
        binding.viewpagerPictureSelect.registerOnPageChangeCallback(onPageChangeCallback)
        ItemTouchHelper(DragAndDropCallback(viewModel.selectedPictureAdapter))
            .attachToRecyclerView(binding.recyclerSelectedPicture)
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.event.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun handleEvent(event: AlbumListViewModel.Event) {
        when (event) {
            is AlbumListViewModel.Event.AlbumList -> initSpinner(event.albums)
            is AlbumListViewModel.Event.ImageCount -> handleImageCount(event.count)
            is AlbumListViewModel.Event.OnListChange -> onAlbumChanged(event.state)
            is AlbumListViewModel.Event.ScrollToTop -> scrollAlbumListToTop()
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

    private fun onAlbumChanged(state: Boolean) {
        binding.buttonAlbumListComplete.visibility =
            if (state) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
    }

    private fun scrollAlbumListToTop() {
        binding.recyclerAlbumList.run {
            post { scrollToPosition(0) }
        }
    }

    private fun initSpinner(albums: List<String>) {
        binding.spinnerAlbumList.apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                albums.map { album -> album.substringAfterLast("/") }
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.updateAlbumList(albums[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 안쓸거야!!
                }
            }
        }
    }

    private fun initViewPagerIndex(idx: Int) {
        if (viewModel.currentViewPagerIdx == idx) {
            setPagerUI(idx)
        }
        binding.viewpagerPictureSelect.setCurrentItem(idx, false)
    }

    private fun setPagerUI(position: Int) {
        // 사진 편집 대상을 알기 위해 현재 보고 있는 이미지의 인덱스 저장
        viewModel.setCurrentViewPagerIdx(position)
        binding.textviewAlbumListIndex.text = viewModel.getCurrentPositionString(position + 1)
    }

    override fun onDestroyView() {
        binding.viewpagerPictureSelect.unregisterOnPageChangeCallback(onPageChangeCallback)
        requireActivity().contentResolver.unregisterContentObserver(contentObserver)
        backPressedCallback.remove()
        super.onDestroyView()
    }
}
