package com.fakedevelopers.bidderbidder

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fakedevelopers.bidderbidder.databinding.ActivityMainBinding
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
            when (destination.id) {
                R.id.productListFragment -> {
                    binding.bottomNavigationMain.run {
                        visibility = View.VISIBLE
                        selectedItemId = R.id.menu_product_list
                    }
                }
                R.id.productRegistrationFragment -> binding.bottomNavigationMain.visibility = View.GONE
            }
        }
        binding.bottomNavigationMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_product_registration -> navController.navigate(R.id.productRegistrationFragment)
            }
            true
        }
        navController.navigate(R.id.productRegistrationFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
