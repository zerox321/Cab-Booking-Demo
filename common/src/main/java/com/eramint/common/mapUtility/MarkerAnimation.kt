package com.eramint.common.mapUtility

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Property
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.eramint.data.Constants.locationInterval
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.util.concurrent.TimeUnit


class MarkerAnimation {


    private fun Marker.rotateMarker(toRotation: Float) {
        val handler = Handler(Looper.getMainLooper())

        val start = SystemClock.uptimeMillis()
        val startRotation = rotation
        val duration: Long =
            TimeUnit.SECONDS.toMillis(locationInterval) / 3
        val interpolator: Interpolator = LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val rot = t * toRotation + (1 - t) * startRotation
                rotation = if (-rot > 180) rot / 2 else rot
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    fun animateMarkerToICS(
        marker: Marker,
        rotate: Float,
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator
    ) {
        marker.rotateMarker(rotate)
        val typeEvaluator =
            TypeEvaluator<LatLng> { fraction, startValue, endValue ->
                latLngInterpolator.interpolate(
                    fraction,
                    startValue,
                    endValue
                )
            }
        val property =
            Property.of(
                Marker::class.java, LatLng::class.java, "position"
            )
        val animator =
            ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.duration = TimeUnit.SECONDS.toMillis(locationInterval)
        animator.start()
    }
}