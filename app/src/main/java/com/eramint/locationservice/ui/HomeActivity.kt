package com.eramint.locationservice.ui

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import androidx.activity.viewModels
import androidx.core.util.forEach
import androidx.lifecycle.lifecycleScope
import com.eramint.locationservice.R
import com.eramint.locationservice.base.LocationActivity
import com.eramint.locationservice.data.DriverModel
import com.eramint.locationservice.databinding.ActivityHomeBinding
import com.eramint.locationservice.util.LatLngInterpolator
import com.eramint.locationservice.util.LocationModel
import com.eramint.locationservice.util.MapAnimator
import com.eramint.locationservice.util.MapUtility.addCustomMarker
import com.eramint.locationservice.util.MapUtility.animate
import com.eramint.locationservice.util.MapUtility.animateCamera
import com.eramint.locationservice.util.MapUtility.defaultMapSettings
import com.eramint.locationservice.util.MapUtility.getAddress
import com.eramint.locationservice.util.MapUtility.getBitmapFromVectorDrawable
import com.eramint.locationservice.util.MapUtility.moveMapCamera
import com.eramint.locationservice.util.MapUtility.setMapStyle
import com.eramint.locationservice.util.RouteUtility.directionDataAsync
import com.eramint.locationservice.util.toGSON
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random


class HomeActivity : LocationActivity(), GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveListener {

    private val viewModel by viewModels<HomeViewModel>()
    private val driversMap = SparseArray<DriverModel>()

    private val mapFragment: SupportMapFragment? by lazy {
        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
    }
    private val geoCoder: Geocoder? by lazy {
        Geocoder(this, Locale("ar"))
    }
    private val latLngInterpolator: LatLngInterpolator by lazy {
        LatLngInterpolator.Spherical()
    }
    private val mapAnimator: MapAnimator by lazy { MapAnimator() }


    private val userBitMap by lazy {
        getBitmapFromVectorDrawable(R.drawable.ic_current_location)
    }
    private val dropOffMap by lazy {
        getBitmapFromVectorDrawable(R.drawable.ic_drop_off_map)
    }
    private val pickUpMap by lazy {
        getBitmapFromVectorDrawable(R.drawable.ic_pick_up_map)
    }
    private val driverBitMap by lazy {
        getBitmapFromVectorDrawable(R.drawable.ic_taxi)
    }
    private val padding = 50

    private val options by lazy {
        PolylineOptions().apply {
            color(Color.parseColor("#15AEB4"))
            width(10f)
        }
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
            mMap?.defaultMapSettings(isLocationEnabled = foregroundPermissionApproved())
            mMap?.setMapStyle(this)
            mMap?.setOnCameraIdleListener(this)
            mMap?.setOnCameraMoveListener(this)
        }
        return mMap
    }

    private fun driver() = LocationModel(
        fromLat = 30.7904085,
        fromLon = 31.0111404,
        toLat = 30.7904085 - Random.nextDouble(0.0, 0.01),
        toLon = 31.0111404 + Random.nextDouble(0.0, 0.01)
    )

    private val binding: ActivityHomeBinding by binding(R.layout.activity_home)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted { getMap() }
        bindView()

        lifecycleScope.launchWhenStarted {
            viewModel.locationFlow
                .map { string -> string?.toGSON() }
                .collect { locationModel: LocationModel? ->
                    onNewLocation(locationModel = locationModel ?: return@collect)
                }
        }

    }

    private fun bindView() {
        binding.run {
            viewModel = this@HomeActivity.viewModel

            dropOffView = dropOffViewConstant

            pickupView = pickupViewConstant

            dropOffHome.currentLocationFab.setOnClickListener { getCurrentLocation() }
            pickUpHome.currentLocationFab.setOnClickListener { getCurrentLocation() }
            dropOffHome.backIv.setOnClickListener { onBackPressed() }
            pickUpHome.backIv.setOnClickListener { onBackPressed() }
            dropOffHome.dropOffBT.setOnClickListener { dropOffClick() }
            pickUpHome.pickUpBT.setOnClickListener { pickUpClick() }

        }
    }

    private fun pickUpClick() {
        this@HomeActivity.viewModel.viewType.value = confirmViewConstant

        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude
            val from = LatLng(lat, lon)
            map.drawPickUpLocation(location = from)

            val destination = dropOffMarker?.position ?: return@launch
            val lineOptions = directionDataAsync(options = options, from = from, to = destination)
                ?: return@launch
            val points = lineOptions.points
            val bounds: LatLngBounds = LatLngBounds.Builder().apply {
                for (point in points)
                    include(point)
            }.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            mapAnimator.animateRoute(map, points)


        }
    }


    private fun dropOffClick() {
        this@HomeActivity.viewModel.viewType.value = pickupViewConstant

        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude

            this@HomeActivity.viewModel.clearPickupPlaceTitleText()
            getCurrentLocation()
            map.drawDropOffLocation(location = LatLng(lat, lon))
        }

    }

    private fun getCurrentLocation() {
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
            this@HomeActivity.viewModel.locationFlow.map { string -> string?.toGSON() }
                .collect { location ->
                    map.animateCamera(
                        position = LatLng(
                            location?.toLat ?: return@collect,
                            location.toLon ?: return@collect
                        )
                    )
                    this.cancel()
                }
        }
    }

    private fun GoogleMap.drawDropOffLocation(location: LatLng) {
        dropOffMarker = addCustomMarker(
            position = location,
            icon = dropOffMap ?: return
        )
    }

    private fun GoogleMap.drawPickUpLocation(location: LatLng) {
        pickUpMarker = addCustomMarker(
            position = location,
            icon = pickUpMap ?: return
        )
    }

    private suspend fun onNewLocation(locationModel: LocationModel) {
        Log.e(TAG, "onNewLocation:  $locationModel")
        val oldPosition = locationModel.getFromLatLng()

        val newPosition = locationModel.getToLatLng()

        val map = getMap() ?: return

        // Todo move Map Camera
        map.setupMapCameraView(position = oldPosition)
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

    private fun updateClick() {
        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val driverID = 1
            map.updateDriver(
                driverID = driverID,
                locationModel = driver()
            )

        }
    }

    private fun GoogleMap.updateDriver(driverID: Int, locationModel: LocationModel) {
        val driverModel = driversMap.get(driverID)
        if (driverModel == null)
            addDriver(
                driverID = driverID,
                driver = driver()
            )
        else {
            driverModel.driverMarker?.animate(
                newPosition = locationModel.getToLatLng(),
                latLngInterpolator = latLngInterpolator
            )
            driversMap.get(driverID)?.driver = locationModel
        }
    }

    private fun addClick() {
        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            map.addDriver(
                driverID = 1,
                driver = driver()
            )
        }
    }

    private fun GoogleMap.addDriver(driverID: Int, driver: LocationModel) {
        // check if driver is Draw Before
        if (driversMap.get(driverID) != null) removeDriver(driverID = driverID)
        val driverMarker = addCustomMarker(
            oldPosition = driver.getFromLatLng(),
            newPosition = driver.getToLatLng(),
            icon = driverBitMap ?: return
        )
        driversMap.append(
            driverID,
            DriverModel(driverID = driverID, driver = driver, driverMarker = driverMarker)
        )
    }

    //Todo remove Driver ->driverID
    private fun removeDriver(driverID: Int) {
        Log.e(TAG, "removeDriver: driverID $driverID")
        val driverModel: DriverModel = driversMap.get(driverID) ?: return
        driverModel.driverMarker?.remove()
        driversMap.remove(driverID)
        Log.e(TAG, "removeDriver: driverID $driverID removed Successfully")
    }


    private fun GoogleMap.setupMapCameraView(position: LatLng) {
        if (isFirst) moveMapCamera(position = position)
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


    override fun onCameraIdle() {
        viewModel.setIsCoveredArea(value = true)
        getLocationName()
    }

    private fun getLocationName() {
        val viewType = viewModel.viewType.value
        if (viewType != pickupViewConstant && viewType != dropOffViewConstant) return
        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val location = geoCoder?.getAddress(
                map.cameraPosition.target.latitude,
                map.cameraPosition.target.longitude
            ) ?: return@launch
            viewModel.setPlaceValue(
                viewType = viewType,
                location = location
            )
        }
    }

    override fun onCameraMove() {
        viewModel.setIsCoveredArea(value = false)
        viewModel.setIsCameraMove(value = true)
    }

    private fun clearMapDrivers() {
        driversMap.forEach { _, item ->
            removeDriver(driverID = item.driverID)
        }
    }

    companion object {
        const val dropOffViewConstant = 1
        const val pickupViewConstant = 2
        const val confirmViewConstant = 3
    }

    override fun onBackPressed() {
        when (viewModel.viewType.value) {
            pickupViewConstant -> {
                viewModel.viewType.value = dropOffViewConstant
                getCurrentLocation()
                dropOffMarker?.remove()
            }
            confirmViewConstant -> {
                viewModel.viewType.value = pickupViewConstant
                getCurrentLocation()
                pickUpMarker?.remove()
                mapAnimator.clear()
            }

            else -> super.onBackPressed()
        }
    }

}