package com.eramint.app

import android.app.Application
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(baseContext)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
    }
}
