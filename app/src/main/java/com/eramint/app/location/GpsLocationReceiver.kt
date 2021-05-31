package com.eramint.app.location

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.eramint.app.location.LocationUtil.dismissNoLocationNotification
import com.eramint.app.location.LocationUtil.showNoLocationNotification
import timber.log.Timber

class GpsLocationReceiver(
    private val notificationManager: NotificationManager,
    private val locationManger: LocationManager
) :
    BroadcastReceiver() {
    private val TAG = "LocationProviderChanged"


    // START OF onReceive
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Timber.tag(TAG).e("onReceive: $action")
        if (action == "android.location.PROVIDERS_CHANGED") {
            Timber.tag(TAG).e("Location Providers Changed")
            val isGpsEnabled = locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Timber.tag(TAG).e(
                "change: isGpsEnabled : $isGpsEnabled isNetworkEnabled : $isNetworkEnabled"
            )

            if (!isGpsEnabled) notificationManager.showNoLocationNotification(context) else
                notificationManager.dismissNoLocationNotification()
        }

    }


}