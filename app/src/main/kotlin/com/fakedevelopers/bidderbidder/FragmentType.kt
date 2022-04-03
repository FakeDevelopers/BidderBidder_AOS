package com.fakedevelopers.bidderbidder

import androidx.fragment.app.Fragment
import com.fakedevelopers.bidderbidder.ui.login.LoginFragment
import com.fakedevelopers.bidderbidder.ui.login_type.LoginTypeFragment
import com.fakedevelopers.bidderbidder.ui.main.MainFragment

enum class FragmentType(val fragment: Fragment) {
    LOGIN(LoginFragment()),
    LOGINTYPE(LoginTypeFragment()),
    MAIN(MainFragment());
}
