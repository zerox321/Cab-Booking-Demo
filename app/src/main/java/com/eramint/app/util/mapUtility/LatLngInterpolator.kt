package com.eramint.app.util.mapUtility

import com.google.android.gms.maps.model.LatLng

interface LatLngInterpolator {
    fun interpolate(fraction: Float, from: LatLng, to: LatLng): LatLng
}