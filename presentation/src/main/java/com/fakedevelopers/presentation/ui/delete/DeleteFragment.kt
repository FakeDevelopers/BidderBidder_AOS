package com.fakedevelopers.presentation.ui.delete

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.fakedevelopers.presentation.R
import com.fakedevelopers.presentation.databinding.FragmentDeleteBinding
import com.fakedevelopers.presentation.ui.base.BaseFragment
import com.fakedevelopers.presentation.ui.util.repeatOnStarted
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DeleteFragment : BaseFragment<FragmentDeleteBinding>(
    R.layout.fragment_delete
) {

    private val viewModel: DeleteViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        initCollector()
    }

    private fun initCollector() {
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.deleteEvent.collectLatest {
                if (it.isSuccess) {
                    sendSnackBar(it.toString())
                } else {
                    sendSnackBar("어.. 요건 디스코드를 확인해")
                }
            }
        }
        repeatOnStarted(viewLifecycleOwner) {
            viewModel.checkEvent.collectLatest {
                if (it.isSuccess) {
                    sendSnackBar(it.toString())
                } else {
                    sendSnackBar("어.. 요건 디스코드를 확인해")
                }
            }
        }
    }
}
