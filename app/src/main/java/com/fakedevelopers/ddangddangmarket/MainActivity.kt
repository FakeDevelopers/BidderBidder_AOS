package com.fakedevelopers.ddangddangmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fakedevelopers.ddangddangmarket.ui.login_type.LoginTypeFragment
import com.fakedevelopers.ddangddangmarket.ui.login.LoginFragment
import com.fakedevelopers.ddangddangmarket.ui.main.MainFragment

class MainActivity : AppCompatActivity() {
    private val loginTypeFragment = LoginTypeFragment()
    private val loginFragment = LoginFragment()
    private val mainFragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(FragmentType.LOGINTYPE)
    }

    fun setFragment(name : FragmentType) {
        val transition = supportFragmentManager.beginTransaction()
        when(name){
            FragmentType.LOGINTYPE -> {
                transition.replace(R.id.mainContainer, loginTypeFragment)
            }
            FragmentType.LOGIN -> {
                transition.replace(R.id.mainContainer, loginFragment)
                transition.addToBackStack(null)
            }
            FragmentType.MAIN -> {
                transition.replace(R.id.mainContainer, mainFragment)
                transition.addToBackStack(null)
            }
        }
        transition.commit()
    }
}
