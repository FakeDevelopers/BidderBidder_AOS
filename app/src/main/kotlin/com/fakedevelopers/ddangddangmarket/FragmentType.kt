package com.fakedevelopers.ddangddangmarket

import androidx.fragment.app.Fragment
import com.fakedevelopers.ddangddangmarket.ui.login.LoginFragment
import com.fakedevelopers.ddangddangmarket.ui.login_type.LoginTypeFragment
import com.fakedevelopers.ddangddangmarket.ui.main.MainFragment

enum class FragmentType(val fragment: Fragment) {
    LOGIN(LoginFragment()),
    LOGINTYPE(LoginTypeFragment()),
    MAIN(MainFragment());
}
