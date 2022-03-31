package com.fakedevelopers.ddangddangmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fakedevelopers.ddangddangmarket.ui.login_type.LoginTypeFragment
import com.fakedevelopers.ddangddangmarket.ui.login.LoginFragment

class MainActivity : AppCompatActivity() {

    private val loginTypeFragment = LoginTypeFragment()
    private val loginFragment = LoginFragment()

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
        }
        transition.commit()
    }
}
