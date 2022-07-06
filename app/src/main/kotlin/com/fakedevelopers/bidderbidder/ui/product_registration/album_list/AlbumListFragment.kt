package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentAlbumListBinding
import com.fakedevelopers.bidderbidder.ui.product_registration.DragAndDropCallback
import com.fakedevelopers.bidderbidder.ui.product_registration.ProductRegistrationDto
import com.fakedevelopers.bidderbidder.ui.util.ContentResolverUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.set

class AlbumListFragment : Fragment() {

    private var _binding: FragmentAlbumListBinding? = null

    private val viewModel: AlbumListViewModel by viewModels()
    private val binding get() = _binding!!
    private val args: AlbumListFragmentArgs by navArgs()

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 뷰 페이저가 열려있으면 페이저만 닫는다.
                if (viewModel.albumViewMode.value == AlbumViewState.PAGER) {
                    viewModel.setAlbumViewMode(AlbumViewState.GRID)
                } else {
                    toProductRegistration(args.productRegistrationDto)
                }
            }
        }
    }
    private val onPageChangeCallback by lazy {
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.textviewAlbumListIndex.text = viewModel.getCurrentPositionString(position + 1)
                // 사진 선택 표시 설정
                setPictureSelectCount(
                    viewModel.findSelectedImageIndex(viewModel.albumPagerAdapter.currentList[position])
                )
            }
        }
    }
    private val contentResolverUtil by lazy {
        ContentResolverUtil(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_album_list,
            container,
            false
        )
        return binding.run {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCollector()
        initListener()
        getPictures()
        if (args.productRegistrationDto.urlList.isNotEmpty()) {
            viewModel.initSelectedImageList(args.productRegistrationDto.urlList)
            binding.buttonAlbumListComplete.visibility = View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        // 선택 이미지 리스트가 존재한다면 유효한지 검사
        if (viewModel.selectedImageList.value.isNotEmpty()) {
            // 유효한 선택 이미지 리스트로 갱신
            viewModel.setSelectedImage(contentResolverUtil.getValidList(viewModel.selectedImageList.value))
            if (viewModel.albumViewMode.value == AlbumViewState.PAGER) {
                setPictureSelectCount(
                    viewModel.findSelectedImageIndex(
                        viewModel.albumPagerAdapter.currentList[binding.viewpagerPictureSelect.currentItem]
                    )
                )
            }
        }
    }

    private fun toProductRegistration(dto: ProductRegistrationDto) {
        dto.urlList = viewModel.selectedImageList.value.toList()
        // 선택한 이미지 uri를 들고 돌아갑니다
        findNavController().navigate(
            AlbumListFragmentDirections.actionPictureSelectFragmentToProductRegistrationFragment(dto)
        )
    }

    private fun getPictures() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        requireActivity().contentResolver.query(
            uri,
            null,
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        ).let {
            if (it != null) {
                val albums = mutableMapOf<String, MutableList<String>>()
                albums[ALL_PICTURES] = mutableListOf()
                val albumNameSummary = mutableMapOf<String, String>()
                while (it.moveToNext()) {
                    // 이미지 Uri
                    val imageUri = ContentUris.withAppendedId(
                        uri,
                        it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    ).toString()
                    // 전체보기에 저장
                    albums[ALL_PICTURES]!!.add(imageUri)
                    // 이미지 상대 경로에 저장
                    it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)).let { path ->
                        if (!albumNameSummary.containsKey(path)) {
                            path.split("/").let { split ->
                                albumNameSummary[path] = split[split.lastIndex - 1]
                                albums[split[split.lastIndex - 1]] = mutableListOf()
                            }
                        }
                        albums[albumNameSummary[path]]!!.add(imageUri)
                    }
                }
                viewModel.initAlbumInfo(albums)
                viewModel.setAlbumList(ALL_PICTURES)
                initSpinner(albums.keys.toTypedArray())
                // 앨범 전환 시 가장 위로 스크롤
                binding.recyclerAlbemList.layoutManager = object : GridLayoutManager(requireContext(), 3) {
                    override fun onLayoutCompleted(state: RecyclerView.State?) {
                        super.onLayoutCompleted(state)
                        // onLayoutCompleted는 정말 여러번 호출됩니다.
                        // 스크롤을 올리는 이벤트를 단 한번만 실행하기 위해 flag를 사용했읍니다.
                        if (viewModel.scrollToTopFlag) {
                            viewModel.setScrollFlag()
                            binding.recyclerAlbemList.scrollToPosition(0)
                        }
                    }
                }
                it.close()
            }
        }
    }

    private fun initSpinner(albumArray: Array<String>) {
        binding.spinnerAlbumList.apply {
            adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, albumArray
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.setAlbumList(albumArray[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 안쓸거야!!
                }
            }
        }
    }

    private fun initListener() {
        binding.buttonAlbumListComplete.setOnClickListener {
            toProductRegistration(args.productRegistrationDto)
        }
        binding.viewpagerPictureSelect.registerOnPageChangeCallback(onPageChangeCallback)
        ItemTouchHelper(DragAndDropCallback(viewModel.selectedPictureAdapter))
            .attachToRecyclerView(binding.recyclerSelectedPicture)
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onListChange.collectLatest {
                    binding.buttonAlbumListComplete.visibility =
                        if (viewModel.selectedImageList.value.isEmpty())
                            View.INVISIBLE
                        else
                            View.VISIBLE
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectErrorImage.collectLatest {
                    Toast.makeText(
                        requireContext(),
                        getText(R.string.album_selected_error_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startViewPagerIndex.collectLatest {
                    binding.viewpagerPictureSelect.setCurrentItem(it, false)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagerSelectedState.collectLatest {
                    // 추가 됐다면 반드시 가장 마지막 자리에 추가됐을것임
                    if (it) {
                        setPictureSelectCount(viewModel.selectedImageList.value.lastIndex)
                    } else {
                        setPictureSelectCount(-1)
                    }
                }
            }
        }
    }

    private fun setPictureSelectCount(index: Int) {
        binding.textviewPictureSelectCount.apply {
            text = if (index != -1) {
                setBackgroundResource(R.drawable.shape_picture_select_count)
                (index + 1).toString()
            } else {
                setBackgroundResource(R.drawable.shape_picture_select_empty)
                ""
            }
        }
    }

    companion object {
        const val ALL_PICTURES = "전체보기"
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewpagerPictureSelect.unregisterOnPageChangeCallback(onPageChangeCallback)
        _binding = null
        backPressedCallback.remove()
    }
}
