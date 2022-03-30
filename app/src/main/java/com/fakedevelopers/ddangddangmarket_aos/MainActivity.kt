package com.fakedevelopers.ddangddangmarket_aos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fakedevelopers.ddangddangmarket_aos.ui.login_type.LoginTypeFragment
import com.fakedevelopers.ddangddangmarket_aos.ui.login.LoginFragment

class MainActivity : AppCompatActivity() {

    private val loginTypeFragment = LoginTypeFragment()
    private val loginFragment = LoginFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment("LoginType")
    }

    fun setFragment(name : String) {
        val transition = supportFragmentManager.beginTransaction()
        when(name){
            "LoginType" -> {
                transition.replace(R.id.mainContainer, loginTypeFragment)
            }
            "Login" -> {
                transition.replace(R.id.mainContainer, loginFragment)
                transition.addToBackStack(null)
            }
        }
        transition.commit()
    }
}