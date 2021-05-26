package com.eramint.locationservice

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

interface LatLngInterpolator {
    fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
    class Linear : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            val lng = (b.longitude - a.longitude) * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }

    class LinearFixed : LatLngInterpolator {
        override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
            val lat = (b.latitude - a.latitude) * fraction + a.latitude
            var lngDelta = b.longitude - a.longitude

            // Take the shortest path across the 180th meridian.
            if (abs(lngDelta) > 180) {
                lngDelta -= sign(lngDelta) * 360
            }
            val lng = lngDelta * fraction + a.longitude
            return LatLng(lat, lng)
        }
    }

    class Spherical : LatLngInterpolator {
        override fun interpolate(fraction: Float, from: LatLng, to: LatLng): LatLng {
            val fromLat = Math.toRadians(from.latitude)
            val fromLng = Math.toRadians(from.longitude)
            val toLat = Math.toRadians(to.latitude)
            val toLng = Math.toRadians(to.longitude)
            val cosFromLat = cos(fromLat)
            val cosToLat = cos(toLat)

            // Computes Spherical interpolation coefficients.
            val angle = computeAngleBetween(fromLat, fromLng, toLat, toLng)
            val sinAngle = sin(angle)
            if (sinAngle < 1E-6) {
                return from
            }
            val a = sin((1 - fraction) * angle) / sinAngle
            val b = sin(fraction * angle) / sinAngle

            // Converts from polar to vector and interpolate.
            val x =
                a * cosFromLat * cos(fromLng) + b * cosToLat * cos(
                    toLng
                )
            val y =
                a * cosFromLat * sin(fromLng) + b * cosToLat * sin(
                    toLng
                )
            val z = a * sin(fromLat) + b * sin(toLat)

            // Converts interpolated vector back to polar.
            val lat = atan2(z, sqrt(x * x + y * y))
            val lng = atan2(y, x)
            return LatLng(Math.toDegrees(lat), Math.toDegrees(lng))
        }

        private fun computeAngleBetween(
            fromLat: Double,
            fromLng: Double,
            toLat: Double,
            toLng: Double
        ): Double {
            val dLat = fromLat - toLat
            val dLng = fromLng - toLng
            return 2 * asin(
                sqrt(
                    sin(dLat / 2)
                        .pow(2.0) + cos(fromLat) * cos(toLat) * sin(
                        dLng / 2
                    ).pow(2.0)
                )
            )
        }
    }
}