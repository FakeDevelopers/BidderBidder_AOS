package com.fakedevelopers.presentation.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity<VB : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> VB
) : AppCompatActivity() {

    protected lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindingFactory(layoutInflater)
        setContentView(binding.root)
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
        startActivity(Intent(this, activity))
        finish()
    }
}
