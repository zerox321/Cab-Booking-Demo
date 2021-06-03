package com.eramint.driver

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.eramint.common.NotificationChannel.createNotificationChannel
import com.google.android.gms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.eramint.driver.R

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(baseContext)

        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())

//        this@App.createNotificationChannel(
//            getString(R.string.notification_channel_id),
//            getString(R.string.notification_channel_id)
//        )
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

    }
}
