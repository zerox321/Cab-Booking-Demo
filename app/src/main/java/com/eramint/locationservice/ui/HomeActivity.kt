package com.eramint.locationservice.ui

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.eramint.locationservice.LocationActivity
import com.eramint.locationservice.R
import com.eramint.locationservice.databinding.ActivityHomeBinding

class HomeActivity : LocationActivity() {

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment_activity_home)
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navView.setupWithNavController(navController)
    }
}