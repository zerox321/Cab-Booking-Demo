package com.eramint.app.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class GpsLocationReceiver constructor(private val locationChangeInterface: LocationChangeInterface) :
    BroadcastReceiver() {
    private val TAG = "LocationProviderChanged"

    @Inject
    lateinit var locationManger: LocationManager


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.location.PROVIDERS_CHANGED") {
            Timber.tag(TAG).e("Location Providers Changed")
            val isGpsEnabled = locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
            locationChangeInterface.onLocationChange(locationValue = isGpsEnabled)
        }

    }


}