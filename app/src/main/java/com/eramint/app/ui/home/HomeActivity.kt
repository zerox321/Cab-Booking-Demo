package com.eramint.app.ui.home

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.eramint.app.R
import com.eramint.app.base.BaseActivity
import com.eramint.app.databinding.ActivityHomeBinding

class HomeActivity : BaseActivity() {
    private val binding: ActivityHomeBinding by binding(R.layout.activity_home)
    private val navController: NavController by lazy { findNavController(R.id.home_nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
    }



    private fun bindView() {
        binding.run {
            navView.setupWithNavController(navController)
            navView.setOnNavigationItemReselectedListener { }
        }
    }

    override fun onLocationPermissionEnabled() {}
}