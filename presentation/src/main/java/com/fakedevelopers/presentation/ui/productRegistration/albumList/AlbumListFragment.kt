package com.fakedevelopers.presentation.ui.productRegistration.albumList

import android.content.ContentUris
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fakedevelopers.domain.model.AlbumItem
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentAlbumListBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.productRegistration.DragAndDropCallback
import com.fakedevelopers.presentation.ui.productRegistration.ProductRegistrationDto
import com.fakedevelopers.presentation.ui.util.ROTATE_DEGREE
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlin.collections.set
import kotlin.math.roundToInt

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
                    toProductRegistration(args.productRegistrationDto)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        if (args.productRegistrationDto.selectedImageInfo.uris.isNotEmpty()) {
            viewModel.initSelectedImageList(args.productRegistrationDto.selectedImageInfo)
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
        viewModel.updateAlbumList()
        viewModel.checkSelectedImages(binding.viewpagerPictureSelect.currentItem)
    }

    private fun toProductRegistration(dto: ProductRegistrationDto) {
        dto.selectedImageInfo.apply {
            uris.clear()
            uris.addAll(viewModel.selectedImageInfo.uris)
            changeBitmaps.clear()
            changeBitmaps.putAll(viewModel.selectedImageInfo.changeBitmaps)
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
                MediaStore.Images.Media.DATA,
                MediaStore.Images.ImageColumns.DATE_MODIFIED
            ),
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC"
        )?.use { cursor ->
            val albums = mutableMapOf<String, MutableList<AlbumItem>>()
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
                val albumItem = AlbumItem(imageUri, date)
                // 전체보기에 저장
                albums[ALL_PICTURES]?.add(albumItem)
                // 이미지 상대 경로에 저장
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)).let { path ->
                    val url = path.substringBeforeLast("/")
                    if (!albumNameSummary.containsKey(url)) {
                        albumNameSummary[url] = url.split("/").last()
                        albums[url] = mutableListOf()
                    }
                    albums[url]?.add(albumItem)
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
                        viewModel.switchScrollFlag()
                        binding.recyclerAlbumList.post {
                            binding.recyclerAlbumList.scrollToPosition(0)
                        }
                    }
                }
            }
        }
    }

    private fun initSpinner(albumNameSummary: Map<String, String>) {
        val (keys, values) = albumNameSummary.keys.toList() to albumNameSummary.values.toList()
        binding.spinnerAlbumList.apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                values
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    viewModel.updateAlbumList(keys[position])
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
        binding.buttonAlbumListRotate.setOnClickListener {
            val uri = viewModel.getCurrentUri()
            // 로테이트된 비트맵이 있으면 그걸 돌림
            // 없다면 새로 추가
            viewModel.getEditedBitmapInfo(uri)?.let { bitmapInfo ->
                updateBitmapInfo(uri, bitmapInfo)
            } ?: addBitmapInfo(uri)
            // 이미지 새로고침
            viewModel.albumPagerAdapter.notifyItemChanged(viewModel.currentViewPagerIdx)
        }
        binding.viewpagerPictureSelect.registerOnPageChangeCallback(onPageChangeCallback)
        ItemTouchHelper(DragAndDropCallback(viewModel.selectedPictureAdapter))
            .attachToRecyclerView(binding.recyclerSelectedPicture)
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.onListChange.collectLatest {
                binding.buttonAlbumListComplete.visibility =
                    if (viewModel.selectedImageInfo.uris.isEmpty()) {
                        View.INVISIBLE
                    } else {
                        View.VISIBLE
                    }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.selectErrorImage.collectLatest {
                Toast.makeText(
                    requireContext(),
                    getText(R.string.album_selected_error_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.startViewPagerIndex.collectLatest { position ->
                // 같은 요소를
                if (viewModel.currentViewPagerIdx == position) {
                    setPagerUI(position)
                }
                binding.viewpagerPictureSelect.setCurrentItem(position, false)
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.imageCountEvent.collectLatest { idx ->
                binding.textviewPictureSelectCount.apply {
                    text = if (idx != -1) {
                        setBackgroundResource(R.drawable.shape_picture_select_count)
                        (idx + 1).toString()
                    } else {
                        setBackgroundResource(R.drawable.shape_picture_select_empty)
                        ""
                    }
                }
            }
        }
    }

    private fun setPagerUI(position: Int) {
        // 사진 편집 대상을 알기 위해 현재 보고 있는 이미지의 인덱스 저장
        viewModel.setCurrentViewPagerIdx(position)
        binding.textviewAlbumListIndex.text = viewModel.getCurrentPositionString(position + 1)
    }

    // 수정된 이미지 비트맵 추가
    private fun addBitmapInfo(uri: String) {
        // 이미지가 선택이 안되어 있다면 이미지 선택
        if (viewModel.findSelectedImageIndex(uri) == -1) {
            viewModel.setSelectedState(uri, true)
        }
        viewModel.addBitmapInfo(uri, BitmapInfo(ROTATE_DEGREE))
    }

    // BitmapInfo 갱신
    private fun updateBitmapInfo(uri: String, bitmapInfo: BitmapInfo) {
        bitmapInfo.degree += ROTATE_DEGREE
        // 360도 돌아갔다면 변경 사항이 없는거다. bitmapInfo를 삭제한다.
        if (bitmapInfo.degree.roundToInt() == 360) {
            viewModel.removeBitmapInfo(uri)
        }
    }

    override fun onDestroyView() {
        binding.viewpagerPictureSelect.unregisterOnPageChangeCallback(onPageChangeCallback)
        requireActivity().contentResolver.unregisterContentObserver(contentObserver)
        backPressedCallback.remove()
        super.onDestroyView()
    }

    companion object {
        const val ALL_PICTURES = "전체보기"
    }
}
