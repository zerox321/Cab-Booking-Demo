package com.eramint.locationservice.util

import android.animation.*
import android.graphics.Color
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

class MapAnimator {
    private var backgroundPolyline: Polyline? = null
    private var foregroundPolyline: Polyline? = null
    private var optionsForeground: PolylineOptions? = null
    private var firstRunAnimSet: AnimatorSet? = null
    private var secondLoopRunAnimSet: AnimatorSet? = null
    fun clear() {
        secondLoopRunAnimSet?.removeAllListeners()
        secondLoopRunAnimSet?.end()
        secondLoopRunAnimSet?.cancel()
        foregroundPolyline?.remove()
        backgroundPolyline?.remove()
    }

    fun animateRoute(map: GoogleMap, bangaloreRoute: List<LatLng?>) {
        firstRunAnimSet = if (firstRunAnimSet == null) {
            AnimatorSet()
        } else {
            secondLoopRunAnimSet?.removeAllListeners()
            secondLoopRunAnimSet?.end()
            secondLoopRunAnimSet?.cancel()
            AnimatorSet()
        }
        secondLoopRunAnimSet = if (secondLoopRunAnimSet == null) {
            AnimatorSet()
        } else {
            secondLoopRunAnimSet?.removeAllListeners()
            secondLoopRunAnimSet?.end()
            secondLoopRunAnimSet?.cancel()
            AnimatorSet()
        }
        //Reset the polylines
        foregroundPolyline?.remove()
        backgroundPolyline?.remove()
        val optionsBackground = PolylineOptions().add(bangaloreRoute[0]).color(GREY).width(5f)
        backgroundPolyline = map.addPolyline(optionsBackground)
        optionsForeground = PolylineOptions().add(bangaloreRoute[0]).color(Color.BLACK).width(5f)
        foregroundPolyline = map.addPolyline(optionsForeground)
        val percentageCompletion = ValueAnimator.ofInt(0, 100)
        percentageCompletion.duration = 2000
        percentageCompletion.interpolator = DecelerateInterpolator()
        percentageCompletion.addUpdateListener { animation ->
            val foregroundPoints = backgroundPolyline?.points
            val percentageValue = animation.animatedValue as Int
            val pointcount = foregroundPoints?.size ?: return@addUpdateListener
            val countTobeRemoved = (pointcount * (percentageValue / 100.0f)).toInt()
            val subListTobeRemoved = foregroundPoints.subList(0, countTobeRemoved)
            subListTobeRemoved.clear()
            foregroundPolyline?.points = foregroundPoints
        }
        percentageCompletion.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                foregroundPolyline?.color = GREY
                foregroundPolyline?.points = backgroundPolyline?.points ?: return
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), GREY, Color.BLACK)
        colorAnimation.interpolator = AccelerateInterpolator()
        colorAnimation.duration = 1200 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            foregroundPolyline?.color = animator.animatedValue as Int
        }
        val foregroundRouteAnimator = ObjectAnimator.ofObject(
            this,
            "routeIncreaseForward",
            RouteEvaluator(),
            *bangaloreRoute.toTypedArray()
        )
        foregroundRouteAnimator.interpolator = AccelerateDecelerateInterpolator()
        foregroundRouteAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                backgroundPolyline?.points = foregroundPolyline?.points ?: return
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        foregroundRouteAnimator.duration = 1600
        //        foregroundRouteAnimator.start();
        firstRunAnimSet?.playSequentially(
            foregroundRouteAnimator,
            percentageCompletion
        )
        firstRunAnimSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                secondLoopRunAnimSet?.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        secondLoopRunAnimSet?.playSequentially(
            colorAnimation,
            percentageCompletion
        )
        secondLoopRunAnimSet?.startDelay = 200
        secondLoopRunAnimSet?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                secondLoopRunAnimSet?.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        firstRunAnimSet?.start()
    }

    /**
     * This will be invoked by the ObjectAnimator multiple times. Mostly every 16ms.
     */
    fun setRouteIncreaseForward(endLatLng: LatLng) {
        val foregroundPoints = foregroundPolyline?.points
        foregroundPoints?.add(endLatLng)
        foregroundPolyline?.points = foregroundPoints ?: return
    }

    val GREY = Color.parseColor("#FFA7A6A6")

}