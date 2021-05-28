package com.eramint.locationservice.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.eramint.locationservice.R
import com.eramint.locationservice.util.MarkerAnimation.animateMarkerToICS
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

object MapUtility {
    fun GoogleMap.moveMapCamera(position: LatLng) {
        moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    position,
                    16f,
                    cameraPosition.tilt,  //use old tilt
                    cameraPosition.bearing
                )
            )
        )
    }

    /**
     * This function sets the default google map settings.
     *
     */
    @SuppressLint("MissingPermission")
    fun GoogleMap.defaultMapSettings() {
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMapToolbarEnabled = false
        uiSettings.isRotateGesturesEnabled = true
        uiSettings.isMapToolbarEnabled = false
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isCompassEnabled = false
        isBuildingsEnabled = true
        isMyLocationEnabled = true
        uiSettings.isMyLocationButtonEnabled = false

    }

    fun GoogleMap.addCustomMarker(
        oldPosition: LatLng,
        newPosition: LatLng,
        icon: Bitmap,
    ) =
        addMarker(
            MarkerOptions()
                .position(oldPosition)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(bearingBetweenLocations(oldPosition, newPosition))
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )

    fun Marker.animate(
        newPosition: LatLng, latLngInterpolator: LatLngInterpolator
    ) = animateMarkerToICS(
        finalPosition = newPosition,
        latLngInterpolator = latLngInterpolator
    )


    fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
        val drawable: Drawable? = ContextCompat.getDrawable(this, drawableId)
        return if (drawable != null) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } else
            null
    }

    fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Float {
        val pi = 3.14159
        val lat1 = latLng1.latitude * pi / 180
        val long1 = latLng1.longitude * pi / 180
        val lat2 = latLng2.latitude * pi / 180
        val long2 = latLng2.longitude * pi / 180
        val dLon = long2 - long1
        val y = kotlin.math.sin(dLon) * kotlin.math.cos(lat2)
        val x =
            kotlin.math.cos(lat1) * kotlin.math.sin(lat2) - (kotlin.math.sin(lat1)
                    * kotlin.math.cos(lat2) * kotlin.math.cos(dLon))
        var brng = kotlin.math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng.toFloat() -90
    }

    fun GoogleMap.setMapStyle(context: Context) {

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("setMapStyle", " Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("setMapStyle", " Error:  ${e.message}")

        }
    }

}