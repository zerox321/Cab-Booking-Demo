package com.eramint.app.ui.inProgressTrip

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.eramint.app.R
import com.eramint.app.base.LocationActivity
import com.eramint.app.data.LocationModel
import com.eramint.app.data.toGSON
import com.eramint.app.databinding.ActivityInProgressTripBinding
import com.eramint.app.util.Constants
import com.eramint.app.util.Constants.driverArrivedConstant
import com.eramint.app.util.Constants.driverIsComingConstant
import com.eramint.app.util.Constants.tripFinishedConstant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import timber.log.Timber

class InProgressTripActivity : LocationActivity() {
    private val viewModel by viewModels<InProgressTripViewModel>()
    private val mapFragment: SupportMapFragment? by lazy {
        supportFragmentManager.findFragmentById(R.id.inProgressGoogleMap) as SupportMapFragment?
    }

    private val dropOffMap by lazy {
        viewModel.mapUtility.getBitmapFromVectorDrawable(
            context = this,
            drawableId = R.drawable.ic_drop_off_map
        )
    }
    private val pickUpMap by lazy {
        viewModel.mapUtility.getBitmapFromVectorDrawable(
            context = this,
            drawableId = R.drawable.ic_pick_up_map
        )
    }
    private val driverBitMap by lazy {
        viewModel.mapUtility.getBitmapFromVectorDrawable(
            context = this,
            drawableId = R.drawable.ic_taxi
        )
    }

    private var isFirst: Boolean = true

    private var userMarker: Marker? = null

    private var dropOffMarker: Marker? = null
    private var pickUpMarker: Marker? = null

    private var mMap: GoogleMap? = null
    private suspend fun getMap(): GoogleMap? {
        if (mMap == null) {
            isFirst = true
            mMap = mapFragment?.awaitMap()
            mMap?.let { map ->
                viewModel.mapUtility.defaultMapSettings(
                    map = map,
                    isLocationEnabled = foregroundPermissionApproved()
                )
                viewModel.mapUtility.setMapStyle(map = map, this)
            }
        }
        return mMap
    }

    private val binding: ActivityInProgressTripBinding by binding(R.layout.activity_in_progress_trip)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted { getMap() }

        val model = intent.extras?.getParcelable<InProgressModel>("item")
        updateView(model = model ?: return)
        bindView()

        lifecycleScope.launchWhenStarted {
            viewModel.locationFlow
                .map { string -> string?.toGSON() }
                .collect { locationModel: LocationModel? ->
                    onNewLocation(locationModel = locationModel ?: return@collect)
                }
        }

    }

    private fun updateView(model: InProgressModel) {
        viewModel.updateView(model = model)
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted

            val dropOffLocation = LatLng(model.dropLat, model.dropLon)
            map.drawDropOffLocation(location = dropOffLocation)

            val pickupLocation = LatLng(model.pickLat, model.pickLon)
            map.drawPickUpLocation(location = pickupLocation)

            val lineOptions =
                viewModel.directionRepo.directionDataAsync(
                    options = viewModel.polylineOptions,
                    from = pickupLocation,
                    to = dropOffLocation
                )
                    ?: return@launchWhenStarted
            val points = lineOptions.points
            val bounds: LatLngBounds = LatLngBounds.Builder().apply {
                for (point in points)
                    include(point)
            }.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.padding))

            viewModel.mapAnimator.animateRoute(map, points)


        }

    }

    private fun bindView() {

        binding.run {

            driverIsComing = driverIsComingConstant
            driverArrived = driverArrivedConstant
            tripFinished = tripFinishedConstant

            viewModel = this@InProgressTripActivity.viewModel
            driverIsComingInProgressTrip.run {
                navigationIv.setOnClickListener {
                    val trip =
                        this@InProgressTripActivity.viewModel.trip.value
                            ?: return@setOnClickListener
//                locationAction(latLong = "${trip.pickLat},${trip.pickLon}")
                    trip.tripState = driverArrivedConstant
                    this@InProgressTripActivity.viewModel.trip.value = trip
                }
            }
            driverGoingDropInProgressTrip.run {
                navigationIv.setOnClickListener {
                    val trip =
                        this@InProgressTripActivity.viewModel.trip.value
                            ?: return@setOnClickListener
//                locationAction(latLong = "${trip.dropLat},${trip.dropLon}")
                    trip.tripState = tripFinishedConstant
                    this@InProgressTripActivity.viewModel.trip.value = trip
                }
            }
            tripRateInProgressTrip.run {
                reviewBT.setOnClickListener {
                    stopLocationService()
                    this@InProgressTripActivity.finish()
                }
            }

        }
    }

    private fun GoogleMap.drawDropOffLocation(location: LatLng) {
        dropOffMarker = viewModel.mapUtility.addCustomMarker(
            map = this,
            position = location,
            icon = dropOffMap ?: return
        )
    }

    private fun GoogleMap.drawPickUpLocation(location: LatLng) {
        pickUpMarker = viewModel.mapUtility.addCustomMarker(
            map = this,
            position = location,
            icon = pickUpMap ?: return
        )
    }

    private suspend fun onNewLocation(locationModel: LocationModel) {
        Timber.tag(TAG).e("onNewLocation:  $locationModel")
//        val oldPosition = locationModel.getFromLatLng()

        val newPosition = locationModel.getToLatLng()

        val map = getMap() ?: return

        // Todo move Map Camera
        map.setupMapCameraView(position = newPosition)
//        // Todo add user location marker
//        if (userMarker == null)
//            userMarker = map.addCustomMarker(
//                oldPosition = oldPosition,
//                newPosition = newPosition,
//                icon = userBitMap ?: return
//            )
//        // Todo animate user location marker
//        else userMarker?.animate(
//            newPosition = newPosition,
//            latLngInterpolator = latLngInterpolator
//        )

    }

    private fun GoogleMap.setupMapCameraView(position: LatLng) {
        if (isFirst) viewModel.mapUtility.moveMapCamera(map = this, position = position)
        isFirst = false
    }

    override fun onResume() {
        super.onResume()
        mapFragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapFragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment?.onLowMemory()
    }

    override fun onLocationChangeAbstraction(locationValue: Boolean) {
        viewModel.isLocationEnabled.value = locationValue
    }


}