package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.ContentResolver.NOTIFY_DELETE
import android.content.ContentResolver.NOTIFY_INSERT
import android.content.ContentResolver.NOTIFY_UPDATE
import android.content.ContentUris
import android.database.ContentObserver
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.set

class AlbumListFragment : Fragment() {

    private var _binding: FragmentAlbumListBinding? = null
    private val viewModel: AlbumListViewModel by viewModels()
    private val binding get() = _binding!!
    private val args: AlbumListFragmentArgs by navArgs()
    // 회전
    private val matrix = Matrix().apply {
        postRotate(ROTATE_DEGREE)
    }

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
                    viewModel.findSelectedImageIndex(viewModel.albumPagerAdapter.currentList[position].first)
                )
            }
        }
    }
    private val contentResolverUtil by lazy {
        ContentResolverUtil(requireContext())
    }
    // 외부 저장소에 변화가 생기면 얘가 호출이 됩니다.
    private val contentObserver by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
                super.onChange(selfChange, uri, flags)
                uri?.let {
                    viewModel.onAlbumListChanged(it.toString(), flags)
                }
            }
        }
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
        if (args.productRegistrationDto.selectedImageDto.uris.isNotEmpty()) {
            viewModel.initSelectedImageList(args.productRegistrationDto.selectedImageDto.uris)
            binding.buttonAlbumListComplete.visibility = View.VISIBLE
        }
        initCollector()
        initListener()
        getPictures()
        // 요소가 많아 난잡해지므로 이동 애니메이션은 없앰
        binding.recyclerAlbumList.itemAnimator = null
    }

    override fun onStart() {
        super.onStart()
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        updateAlbumList()
    }

    private fun toProductRegistration(dto: ProductRegistrationDto) {
        dto.selectedImageDto.uris.apply {
            clear()
            addAll(viewModel.selectedImageDto.uris)
        }
        // 선택한 이미지 uri를 들고 돌아갑니다
        findNavController().navigate(
            AlbumListFragmentDirections.actionPictureSelectFragmentToProductRegistrationFragment(dto)
        )
    }

    private fun getPictures() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        requireActivity().contentResolver.registerContentObserver(uri, true, contentObserver)
        requireActivity().contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        ).let {
            it?.let { cursor ->
                val albums = mutableMapOf<String, MutableList<Pair<String, Long>>>()
                albums[ALL_PICTURES] = mutableListOf()
                val albumNameSummary = mutableMapOf<String, String>()
                albumNameSummary[ALL_PICTURES] = ALL_PICTURES
                while (cursor.moveToNext()) {
                    // 이미지 Uri
                    val imageUri = ContentUris.withAppendedId(
                        uri,
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    ).toString()
                    // 최근 수정 날짜
                    val date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))
                    // 전체보기에 저장
                    albums[ALL_PICTURES]?.add(imageUri to date)
                    // 이미지 상대 경로에 저장
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)).let { path ->
                        if (!albumNameSummary.containsKey(path)) {
                            path.split("/").let { split ->
                                albumNameSummary[path] = split[split.lastIndex - 1]
                                albums[path] = mutableListOf()
                            }
                        }
                        albums[path]?.add(imageUri to date)
                    }
                }
                viewModel.initAlbumInfo(albums)
                initSpinner(albumNameSummary)
                // 앨범 전환 시 가장 위로 스크롤
                binding.recyclerAlbumList.layoutManager = object : GridLayoutManager(requireContext(), 3) {
                    override fun onLayoutCompleted(state: RecyclerView.State?) {
                        super.onLayoutCompleted(state)
                        // onLayoutCompleted는 정말 여러번 호출됩니다.
                        // 스크롤을 올리는 이벤트를 단 한번만 실행하기 위해 flag를 사용했읍니다.
                        if (viewModel.scrollToTopFlag && viewModel.isAlbumListChanged()) {
                            viewModel.setScrollFlag()
                            binding.recyclerAlbumList.scrollToPosition(0)
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    private fun initSpinner(albumNameSummary: Map<String, String>) {
        val (keys, values) = albumNameSummary.keys.toList() to albumNameSummary.values.toList()
        binding.spinnerAlbumList.apply {
            adapter = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_item, values
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    updateAlbumList(keys[position])
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
                    Logger.i(viewModel.selectedImageDto.uris.isEmpty().toString())
                    binding.buttonAlbumListComplete.visibility =
                        if (viewModel.selectedImageDto.uris.isEmpty())
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
                        setPictureSelectCount(viewModel.selectedImageDto.uris.lastIndex)
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

    private fun updateAlbumList(albumName: String? = null) {
        // 선택 이미지 리스트가 존재한다면 유효한지 검사
        if (viewModel.selectedImageDto.uris.isNotEmpty()) {
            // 유효한 선택 이미지 리스트로 갱신
            viewModel.setSelectedImage(contentResolverUtil.getValidList(viewModel.selectedImageDto.uris))
            if (viewModel.albumViewMode.value == AlbumViewState.PAGER) {
                setPictureSelectCount(
                    viewModel.findSelectedImageIndex(
                        viewModel.getPictureUri(position = binding.viewpagerPictureSelect.currentItem)
                    )
                )
            }
        }
        // 앨범 리스트 갱신
        // 도중에 추가된 이미지들이 유효한지 검사한다.
        val list = mutableListOf<Triple<String, String, Long>>()
        for (uri in viewModel.addedImageList) {
            // 해당 uri이 유효하면 list에 추가
            Uri.parse(uri).let {
                if (contentResolverUtil.isExist(it)) {
                    val (rel, date) = contentResolverUtil.getDateModifiedFromUri(it)
                    list.add(Triple(uri, rel, date))
                }
            }
        }
        if (albumName != null) {
            viewModel.updateAlbumList(list, albumName)
        } else {
            viewModel.updateAlbumList(list)
        }
    }

    companion object {
        const val ALL_PICTURES = "전체보기"

        // 이미지 변경 플래그
        private const val BASE_FLAG = 32768
        const val ADD_IMAGE = BASE_FLAG + NOTIFY_INSERT
        const val REMOVE_IMAGE = BASE_FLAG + NOTIFY_DELETE
        const val MODIFY_IMAGE = BASE_FLAG + NOTIFY_UPDATE
        // 회전 각도
        private const val ROTATE_DEGREE = 90f
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewpagerPictureSelect.unregisterOnPageChangeCallback(onPageChangeCallback)
        requireActivity().contentResolver.unregisterContentObserver(contentObserver)
        _binding = null
        backPressedCallback.remove()
    }
}
