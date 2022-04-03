package com.fakedevelopers.bidderbidder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(FragmentType.LOGINTYPE)
    }

    fun setFragment(name : FragmentType) {
        val transition = supportFragmentManager.beginTransaction()
        transition.replace(R.id.mainContainer, name.fragment)
        if(name != FragmentType.LOGINTYPE)
            transition.addToBackStack(null)
        transition.commit()
    }
}
