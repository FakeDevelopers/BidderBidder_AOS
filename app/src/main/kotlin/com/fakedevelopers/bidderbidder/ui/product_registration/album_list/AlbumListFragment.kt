package com.fakedevelopers.bidderbidder.ui.product_registration.album_list

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentAlbumListBinding
import com.fakedevelopers.bidderbidder.ui.product_registration.DragAndDropCallback
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlbumListFragment : Fragment() {

    private var _binding: FragmentAlbumListBinding? = null

    private val viewModel: AlbumListViewModel by viewModels()
    private val binding get() = _binding!!

    private val backPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(
                    AlbumListFragmentDirections
                        .actionPictureSelectFragmentToProductRegistrationFragment(
                            viewModel.selectedImageList.value.toTypedArray()
                        )
                )
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
        initCollector()
        getPictures()
        val args: AlbumListFragmentArgs by navArgs()
        if (!args.selectedImageList.isNullOrEmpty()) {
            viewModel.initSelectedImageList(args.selectedImageList!!.toList())
            binding.buttonAlbumListComplete.visibility = View.VISIBLE
        }
        binding.buttonAlbumListComplete.setOnClickListener {
            // 선택한 이미지 uri를 들고 돌아갑니다
            findNavController().navigate(
                AlbumListFragmentDirections
                    .actionPictureSelectFragmentToProductRegistrationFragment(
                        viewModel.selectedImageList.value.toTypedArray()
                    )
            )
        }
        requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)
        ItemTouchHelper(DragAndDropCallback(viewModel.selectedPictureAdapter))
            .attachToRecyclerView(binding.recyclerSelectedPicture)
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
    }

    companion object {
        const val ALL_PICTURES = "전체보기"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        backPressedCallback.remove()
    }
}
