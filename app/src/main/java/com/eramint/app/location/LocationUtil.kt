package com.eramint.app.location

import android.app.Activity
import android.content.IntentSender.SendIntentException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

object LocationUtil {


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

}