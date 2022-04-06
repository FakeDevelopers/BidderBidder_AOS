package com.fakedevelopers.bidderbidder

import androidx.fragment.app.Fragment
import com.fakedevelopers.bidderbidder.ui.login.LoginFragment
import com.fakedevelopers.bidderbidder.ui.login_type.LoginTypeFragment
import com.fakedevelopers.bidderbidder.ui.main.MainFragment
import com.fakedevelopers.bidderbidder.ui.register.PhoneAuthFragment
import com.fakedevelopers.bidderbidder.ui.register.RegisterFragment

enum class FragmentType(val fragment: Fragment) {
    LOGIN(LoginFragment()),
    LOGINTYPE(LoginTypeFragment()),
    PHONEAUTH(PhoneAuthFragment()),
    REGISTER(RegisterFragment()),
    MAIN(MainFragment());
}
