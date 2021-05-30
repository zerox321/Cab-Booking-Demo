package com.eramint.locationservice.util

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Property
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import com.eramint.locationservice.location.ForegroundOnlyLocationService
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.util.concurrent.TimeUnit


object MarkerAnimation {
    fun animateMarkerToGB(
        marker: Marker,
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = marker.position
        val handler = Handler(Looper.getMainLooper())
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 3000f
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition!!)

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    fun Marker.animateMarkerToHC(
        finalPosition: LatLng?,
        latLngInterpolator: LatLngInterpolator
    ) {
        val startPosition = position
        val valueAnimator = ValueAnimator()
        valueAnimator.addUpdateListener { animation ->
            val v = animation.animatedFraction
            val newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition!!)
            position = newPosition
        }
        valueAnimator.setFloatValues(0f, 1f) // Ignored.
        valueAnimator.duration =
            TimeUnit.SECONDS.toMillis(ForegroundOnlyLocationService.locationInterval)

        valueAnimator.start()
    }

    private fun Marker.rotateMarker(toRotation: Float) {
        val handler = Handler(Looper.getMainLooper())

        val start = SystemClock.uptimeMillis()
        val startRotation = rotation
        val duration: Long =
            TimeUnit.SECONDS.toMillis(ForegroundOnlyLocationService.locationInterval)
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

    fun Marker.animateMarkerToICS(
        finalPosition: LatLng,
        latLngInterpolator: LatLngInterpolator
    ) {
        rotateMarker(MapUtility.bearingBetweenLocations(position, finalPosition))
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
            ObjectAnimator.ofObject(this, property, typeEvaluator, finalPosition)
        animator.duration =
            TimeUnit.SECONDS.toMillis(ForegroundOnlyLocationService.locationInterval)
        animator.start()
    }
}