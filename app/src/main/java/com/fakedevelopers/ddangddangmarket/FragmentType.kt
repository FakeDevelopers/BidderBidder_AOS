package com.fakedevelopers.ddangddangmarket

import androidx.fragment.app.Fragment
import com.fakedevelopers.ddangddangmarket.ui.login.LoginFragment
import com.fakedevelopers.ddangddangmarket.ui.login_type.LoginTypeFragment
import com.fakedevelopers.ddangddangmarket.ui.main.MainFragment

enum class FragmentType{
    LOGIN{ override fun get(): Fragment = LoginFragment() },
    LOGINTYPE{ override fun get(): Fragment = LoginTypeFragment() },
    MAIN{ override fun get(): Fragment = MainFragment() };

    abstract fun get(): Fragment
}
