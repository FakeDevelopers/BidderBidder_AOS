package com.fakedevelopers.bidderbidder.ui.product_registration.picture_select

import android.content.ContentUris
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.FragmentPictureSelectBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PictureSelectFragment : Fragment() {

    private var _binding: FragmentPictureSelectBinding? = null

    private val viewModel: PictureSelectViewModel by viewModels()
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_picture_select,
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
        getPictures()
        initCollector()
        binding.buttonPictureSelectComplete.setOnClickListener {
            PictureSelectFragmentDirections
                .actionPictureSelectFragmentToProductRegistrationFragment(viewModel.selectedImageList.toTypedArray())
                .let {
                    findNavController().navigate(it)
                }
        }
    }

    private fun getPictures() {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        requireActivity().contentResolver.query(
            uri,
            null,
            null,
            null,
            MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        ).let {
            if (it != null) {
                val albums = hashSetOf<String>()
                val list = mutableListOf<String>()
                while (it.moveToNext()) {
                    albums.add(it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)))
                    list.add(
                        ContentUris.withAppendedId(
                            uri,
                            it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        ).toString()
                    )
                }
                viewModel.setList(list)
                it.close()
            }
        }
    }

    private fun initCollector() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedIndexList.collectLatest {
                    for (i in it.indices) {
                        binding
                            .recyclerPictureSelect
                            .findViewHolderForAdapterPosition(it[i])!!
                            .itemView
                            .findViewById<TextView>(R.id.textview_picture_select_count)
                            .text = (i + 1).toString()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
