package com.fakedevelopers.presentation.ui

import com.fakedevelopers.presentation.databinding.ActivityLoginBinding
import com.fakedevelopers.presentation.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate)
