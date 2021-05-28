package com.eramint.locationservice.location

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.eramint.locationservice.location.LocationUtil.dismissNoLocationNotification
import com.eramint.locationservice.location.LocationUtil.showNoLocationNotification

class GpsLocationReceiver : BroadcastReceiver() {
    private val TAG = "LocationProviderChanged"
    var isGpsEnabled = false
    var isNetworkEnabled = false
    private var notificationManager: NotificationManager? = null

    private fun getNotificationManager(context: Context): NotificationManager? {
        if (notificationManager == null) notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager
    }

    // START OF onReceive
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.e(TAG, "onReceive: $action")
        if (action == "android.location.PROVIDERS_CHANGED" || action == "android.intent.action.BOOT_COMPLETED") {
            Log.e(TAG, "Location Providers Changed")
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            Log.e(
                TAG,
                "change: isGpsEnabled : $isGpsEnabled isNetworkEnabled : $isNetworkEnabled"
            )

            val notificationManager = getNotificationManager(context)
            if (!isGpsEnabled) notificationManager?.showNoLocationNotification(context) else
                notificationManager?.dismissNoLocationNotification()
        }

    }


}