package com.fakedevelopers.bidderbidder

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fakedevelopers.bidderbidder.databinding.ActivityMainBinding
import com.fakedevelopers.bidderbidder.ui.util.safeNavigate
import com.jakewharton.threetenabp.AndroidThreeTen
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
        AndroidThreeTen.init(this)
        navController = (supportFragmentManager.findFragmentById(R.id.navigation_main) as NavHostFragment).navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.productListFragment -> {
                    binding.bottomNavigationMain.run {
                        visibility = View.VISIBLE
                        selectedItemId = R.id.menu_product_list
                    }
                }
                R.id.channelListFragment -> binding.bottomNavigationMain.visibility = View.VISIBLE
                else -> binding.bottomNavigationMain.visibility = View.GONE
            }
        }
        binding.bottomNavigationMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_product_registration -> navController.safeNavigate(R.id.productRegistrationFragment)
                R.id.menu_chat -> navController.safeNavigate(R.id.channelListFragment)
            }
            true
        }
        binding.bottomNavigationMain.setItemOnTouchListener(R.id.menu_product_list) { view, motionEvent ->
            if (motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                view.performClick()
                navController.apply {
                    getViewModelStoreOwner(R.id.nav_graph).viewModelStore.clear()
                    safeNavigate(R.id.productListFragment)
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
