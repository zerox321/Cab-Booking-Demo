package com.eramint.locationservice.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.eramint.locationservice.R
import com.eramint.locationservice.base.BaseFragment
import com.eramint.locationservice.databinding.FragmentHomeBinding
import com.eramint.locationservice.util.LatLngInterpolator
import com.eramint.locationservice.util.LocationModel
import com.eramint.locationservice.util.MarkerAnimation
import com.eramint.locationservice.util.toGSON
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.lang.Math.toDegrees

class HomeFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel by viewModels<HomeViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mapFragment: SupportMapFragment? by lazy {
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment?.getMapAsync(this)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationFlow
                .map { string -> string?.toGSON() }
                .collect { locationModel: LocationModel? ->
                    Log.e(TAG, "locationModel: $locationModel")
                    onNewLocation(
                        locationModel = locationModel ?: return@collect
                    )
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        _binding?.currentLocation?.setOnClickListener {
            isFirst = true
            requestLocation()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onNewLocation(locationModel: LocationModel) {
        Log.e(TAG, "onNewLocation:  $locationModel")
        val oldLatLng = locationModel.getFromLatLng()
        val newLatLng = locationModel.getToLatLng()

        setupMapCameraView(userLocationLatLng = oldLatLng)


        val myMarker = mMap?.addMarker(
            MarkerOptions()
                .title("Driver Name")
                .position(oldLatLng)
                .icon(BitmapDescriptorFactory.fromBitmap(userBitMap ?: return))
        )
        MarkerAnimation.animateMarkerToICS(
            marker = myMarker,
            finalPosition = newLatLng,
            latLngInterpolator = latLngInterpolator
        )


    }

    private val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.Spherical()
    private val userBitMap by lazy {
        context?.getBitmapFromVectorDrawable(R.drawable.ic_current_location)
    }

    private fun bearingBetweenLocations(latLng1: LatLng, latLng2: LatLng): Double {
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
        brng = toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

    private fun Context.getBitmapFromVectorDrawable(drawableId: Int): Bitmap? {
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

    private fun setupMapCameraView(userLocationLatLng: LatLng) {
        mMap?.clear()
        if (isFirst)
            mMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition(
                        userLocationLatLng,
                        16f,
                        mMap?.cameraPosition?.tilt ?: 0F,  //use old tilt
                        mMap?.cameraPosition?.bearing ?: 0F
                    )
                )
            )
        isFirst = false
    }

    private var mMap: GoogleMap? = null
    private var isFirst: Boolean = true
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isFirst = true
        requestLocation()
    }
}