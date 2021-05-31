package com.eramint.app.util.mapUtility

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.eramint.app.R
import com.eramint.app.util.Constants.cameraZoom
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MapUtility {
    fun moveMapCamera(map: GoogleMap, position: LatLng) {
        Timber.e("moveMapCamera: $position")
        map.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    position,
                    cameraZoom,
                    map.cameraPosition.tilt,  //use old tilt
                    map.cameraPosition.bearing
                )
            )
        )
    }

    fun animateCamera(map: GoogleMap, position: LatLng) {
        Timber.e("animateCamera: $position")
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    position,
                    cameraZoom,
                    map.cameraPosition.tilt,  //use old tilt
                    map.cameraPosition.bearing
                )
            )
        )
    }

    /**
     * This function sets the default google map settings.
     *
     */
    @SuppressLint("MissingPermission")
    fun defaultMapSettings(map: GoogleMap, isLocationEnabled: Boolean) {
        map.apply {
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isRotateGesturesEnabled = false
            uiSettings.isMapToolbarEnabled = false
            uiSettings.isTiltGesturesEnabled = true
            uiSettings.isCompassEnabled = false
            isBuildingsEnabled = true
            isMyLocationEnabled = isLocationEnabled
            uiSettings.isMyLocationButtonEnabled = false
        }

    }

    fun addCustomMarker(
        map: GoogleMap,
        oldPosition: LatLng,
        newPosition: LatLng,
        icon: Bitmap,
    ): Marker? =
        map.addMarker(
            MarkerOptions()
                .position(oldPosition)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .rotation(bearingBetweenLocations(oldPosition, newPosition))
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )

    fun addCustomMarker(
        map: GoogleMap,
        position: LatLng,
        icon: Bitmap,
    ): Marker? =
        map.addMarker(
            MarkerOptions()
                .position(position)
                .flat(true)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
        )

    fun animate(
        marker: Marker,
        markerAnimation: MarkerAnimation,
        newPosition: LatLng, latLngInterpolator: LatLngInterpolator
    ) = markerAnimation.animateMarkerToICS(
        marker = marker,
        rotate = bearingBetweenLocations(marker.position, newPosition),
        finalPosition = newPosition,
        latLngInterpolator = latLngInterpolator
    )


    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
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
        return brng.toFloat() - 90
    }

    fun setMapStyle(map: GoogleMap, context: Context) {

        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )

            if (!success) {
                Timber.e("setMapStyle  Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e("setMapStyle  Error:  ${e.message}")

        }
    }

    suspend fun getAddress(geocoder: Geocoder, latitude: Double?, longitude: Double?): String =
        withContext(Dispatchers.IO) {
            try {
                if (latitude == null || longitude == null)
                    throw Exception("LatLng is Null")

                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.let {
                    val returnedAddress: Address = addresses[0]
                    val address = "${returnedAddress.adminArea ?: ""}," +
                            " ${returnedAddress.subAdminArea ?: ""}, ${returnedAddress.locality ?: ""}," +
                            " ${returnedAddress.thoroughfare ?: ""}"

                    address
                } ?: ""

            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

}