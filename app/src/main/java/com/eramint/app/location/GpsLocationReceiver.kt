package com.eramint.app.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.eramint.app.local.DataStore
import timber.log.Timber
import javax.inject.Inject

class GpsLocationReceiver constructor(
    private val locationChangeInterface: LocationChangeInterface
) : BroadcastReceiver() {
    @Inject
    lateinit var locationManger: LocationManager

    private val TAG = "LocationProviderChanged"


    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Timber.tag(TAG).e("onReceive: $action")
        if (action == "android.location.PROVIDERS_CHANGED") {
            Timber.tag(TAG).e("Location Providers Changed")
            val isGpsEnabled = locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
            locationChangeInterface.onLocationChange(locationValue = isGpsEnabled)
        }

    }


}