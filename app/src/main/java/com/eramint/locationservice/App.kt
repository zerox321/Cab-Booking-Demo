package com.eramint.locationservice

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(baseContext)
//        Places.initialize(baseContext, "AIzaSyBG7Tdt8ZLceXZOle3-7ZZktgsyYcU2bWw")
    }
}
