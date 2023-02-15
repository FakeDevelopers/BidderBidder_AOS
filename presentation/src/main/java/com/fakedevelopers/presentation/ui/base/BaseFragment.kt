package com.fakedevelopers.presentation.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

abstract class BaseFragment<VB : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        return binding.run {
            lifecycleOwner = viewLifecycleOwner
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initCollector()
    }

    protected open fun initListener() {
        // 호출을 BaseFragment로 위임하기 위한 빈 메소드 입니다.
    }

    protected open fun initCollector() {
        // 호출을 BaseFragment로 위임하기 위한 빈 메소드 입니다.
    }

    protected fun sendSnackBar(
        message: String,
        @IntRange(from = -2) length: Int = Snackbar.LENGTH_SHORT,
        anchorView: View? = null
    ) {
        Snackbar.make(
            binding.root,
            message,
            length
        ).apply {
            if (anchorView != null) {
                this.anchorView = anchorView
            }
        }.show()
    }

    protected fun navigateActivity(activity: Class<*>) {
        startActivity(Intent(requireContext(), activity))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
