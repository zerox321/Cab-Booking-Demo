package com.eramint.locationservice.ui.ui.home

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
import androidx.lifecycle.ViewModelProvider
import com.eramint.locationservice.*
import com.eramint.locationservice.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Math.toDegrees


class HomeFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val mapFragment: SupportMapFragment? by lazy {
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment?.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
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

    override fun onNewLocation(locationModel: LocationModel) {
        Log.e(TAG, "onNewLocation:  $locationModel")
        val oldLatLng = locationModel.from ?: return
        val newLatLng = locationModel.to ?: return

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