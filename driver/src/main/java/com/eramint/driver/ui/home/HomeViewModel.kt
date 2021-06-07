package com.eramint.driver.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val driverName = MutableLiveData<String>("Eslam Kamel")
    val driverPhone = MutableLiveData<String>("01555892962")
    val totalEarned = MutableLiveData<String>("12 SAR")
}