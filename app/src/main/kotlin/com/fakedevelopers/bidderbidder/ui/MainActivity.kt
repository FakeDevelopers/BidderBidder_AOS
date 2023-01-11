package com.fakedevelopers.bidderbidder.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fakedevelopers.bidderbidder.R
import com.fakedevelopers.bidderbidder.databinding.ActivityMainBinding
import com.fakedevelopers.bidderbidder.ui.base.BaseActivity
import com.fakedevelopers.bidderbidder.ui.util.safeNavigate
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private lateinit var navController: NavController

    private val destinationChangeListener by lazy {
        NavController.OnDestinationChangedListener { _, destination, _ ->
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        navController = (supportFragmentManager.findFragmentById(R.id.navigation_main) as NavHostFragment).navController
        navController.addOnDestinationChangedListener(destinationChangeListener)
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
        navController.removeOnDestinationChangedListener(destinationChangeListener)
    }
}
