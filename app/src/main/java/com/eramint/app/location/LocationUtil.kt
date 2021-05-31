package com.eramint.app.location

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import androidx.core.app.NotificationCompat
import com.eramint.app.R
import com.eramint.app.ui.HomeActivity
import com.eramint.app.util.Constants.NOTIFICATION_CHANNEL_ID
import com.eramint.app.util.Constants.No_Location_NOTIFICATION_ID
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

object LocationUtil {
    fun NotificationManager.dismissNoLocationNotification() {
        cancel(No_Location_NOTIFICATION_ID)
    }

    fun Activity.showLocationPrompt() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val result =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener { task: Task<LocationSettingsResponse?> ->
            try {
                val response = task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                if (e.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED && e is ResolvableApiException) {
                    try {
                        e.startResolutionForResult(this, LocationRequest.PRIORITY_HIGH_ACCURACY)
                    } catch (sendIntentException: SendIntentException) {
                        sendIntentException.printStackTrace()
                    }
                }
                e.printStackTrace()
            }
        }
    }

    fun NotificationManager.showNoLocationNotification(context: Context) {
        val titleText = context.getString(R.string.location_disabled)
        val mainNotificationText = context.getString(R.string.location_disabled_content)
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        val intent = Intent(context, HomeActivity::class.java)

        val contentIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationCompatBuilder = NotificationCompat.Builder(
            context.applicationContext,
            NOTIFICATION_CHANNEL_ID
        ).apply {
            setStyle(bigTextStyle)
            setContentTitle(titleText)
            setContentText(mainNotificationText)
            setContentIntent(contentIntent)
            setSmallIcon(R.mipmap.ic_launcher)
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setOngoing(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        notify(No_Location_NOTIFICATION_ID, notificationCompatBuilder.build())
    }
}