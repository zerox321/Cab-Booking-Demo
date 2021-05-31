package com.eramint.app.ui

import android.location.Geocoder
import android.os.Bundle
import android.util.SparseArray
import androidx.activity.viewModels
import androidx.core.util.forEach
import androidx.lifecycle.lifecycleScope
import com.eramint.app.R
import com.eramint.app.base.LocationActivity
import com.eramint.app.data.DriverModel
import com.eramint.app.data.LocationModel
import com.eramint.app.data.toGSON
import com.eramint.app.databinding.ActivityHomeBinding
import com.eramint.app.util.Constants.confirmViewConstant
import com.eramint.app.util.Constants.dropOffViewConstant
import com.eramint.app.util.Constants.padding
import com.eramint.app.util.Constants.pickupViewConstant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
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


    private val userBitMap by lazy {
        viewModel.mapUtility.getBitmapFromVectorDrawable(
            context = this,
            drawableId = R.drawable.ic_current_location
        )
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
            mMap?.setOnCameraIdleListener(this)
            mMap?.setOnCameraMoveListener(this)
        }
        return mMap
    }

    private fun getID(): Int = Random.nextInt(0, 10)
    private fun driver() = LocationModel(
        fromLat = 30.7904085 - Random.nextDouble(0.001, 0.01),
        fromLon = 31.0111404 - Random.nextDouble(0.001, 0.01),
        toLat = 30.7904085 + Random.nextDouble(0.001, 0.01),
        toLon = 31.0111404 + Random.nextDouble(0.001, 0.01)
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
            confirmView = confirmViewConstant

            // Todo attach drop off View click Listener
            dropOffHome.run {
                backIv.setOnClickListener { onBackPressed() }
                dropOffBT.setOnClickListener { dropOffClick() }
                currentLocationFab.setOnClickListener { getCurrentLocation() }
            }
            // Todo attach pick up View click Listener

            pickUpHome.run {
                backIv.setOnClickListener { onBackPressed() }
                pickUpBT.setOnClickListener { pickUpClick() }
                currentLocationFab.setOnClickListener { getCurrentLocation() }

            }
            // Todo attach confirm View click Listener
            confirmHome.run {
                backIv.setOnClickListener { onBackPressed() }
            }
            test.setOnClickListener { updateClick() }
        }
    }

    private fun pickUpClick() {
        this@HomeActivity.viewModel.viewType.value = confirmViewConstant

        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude
            val pickupLocation = LatLng(lat, lon)
            map.drawPickUpLocation(location = pickupLocation)

            val dropOffLocation = dropOffMarker?.position ?: return@launch

            val lineOptions =
                viewModel.directionRepo.directionDataAsync(
                    from = pickupLocation,
                    to = dropOffLocation
                )
                    ?: return@launch
            val points = lineOptions.points
            val bounds: LatLngBounds = LatLngBounds.Builder().apply {
                for (point in points)
                    include(point)
            }.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            viewModel.mapAnimator.animateRoute(map, points)


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
                    viewModel.mapUtility.animateCamera(
                        map = map,
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
            map.updateDriver(
                driverID = getID(),
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
            viewModel.mapUtility.animate(
                marker = driverModel.driverMarker ?: return,
                markerAnimation = viewModel.markerAnimation,
                newPosition = locationModel.getToLatLng(),
                latLngInterpolator = viewModel.spherical
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
        val driverMarker = viewModel.mapUtility.addCustomMarker(
            map = this,
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
        Timber.tag(TAG).e("removeDriver: driverID $driverID")
        val driverModel: DriverModel = driversMap.get(driverID) ?: return
        driverModel.driverMarker?.remove()
        driversMap.remove(driverID)
        Timber.tag(TAG).e("removeDriver: driverID $driverID removed Successfully")
    }

    private fun clearMapDrivers() {
        driversMap.forEach { _, item ->
            removeDriver(driverID = item.driverID)
        }
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


    override fun onCameraIdle() {
        viewModel.setIsCoveredArea(value = true)
        getLocationName()
    }

    private fun getLocationName() {
        val viewType = viewModel.viewType.value
        if (viewType != pickupViewConstant && viewType != dropOffViewConstant) return
        lifecycleScope.launch(defaultContext) {
            val map = getMap() ?: return@launch
            val location = viewModel.mapUtility.getAddress(
                geocoder = geoCoder ?: return@launch,
                map.cameraPosition.target.latitude,
                map.cameraPosition.target.longitude
            )
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


    override fun onBackPressed() {
        when (viewModel.viewType.value) {
            confirmViewConstant -> {
                viewModel.viewType.value = pickupViewConstant
                getCurrentLocation()
                pickUpMarker?.remove()
                viewModel.mapAnimator.clear()
            }

            pickupViewConstant -> {
                viewModel.viewType.value = dropOffViewConstant
                getCurrentLocation()
                dropOffMarker?.remove()
            }

            else -> super.onBackPressed()
        }
    }

}