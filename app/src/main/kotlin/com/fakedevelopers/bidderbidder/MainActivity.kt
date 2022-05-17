package com.fakedevelopers.bidderbidder

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fakedevelopers.bidderbidder.databinding.ActivityMainBinding
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = (supportFragmentManager.findFragmentById(R.id.navigation_main) as NavHostFragment).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Logger.i(destination.displayName)
            when (destination.displayName.substringAfterLast("/")) {
                "mainFragment" -> {
                    binding.bottomNavigationMain.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
