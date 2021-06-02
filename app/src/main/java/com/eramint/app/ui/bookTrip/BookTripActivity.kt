package com.eramint.app.ui.bookTrip

import android.content.Intent
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
import com.eramint.app.databinding.ActivityBookTripBinding
import com.eramint.app.ui.bookTrip.adapter.RidesAdapter
import com.eramint.app.ui.inProgressTrip.InProgressModel
import com.eramint.app.ui.inProgressTrip.InProgressTripActivity
import com.eramint.app.util.Constants
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
import timber.log.Timber
import java.util.*
import kotlin.random.Random


class BookTripActivity : LocationActivity(), GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveListener, RidesAdapter.ClickListener {
    private val rides = listOf("", "")
    private val ridesAdapter by lazy { RidesAdapter(this) }

    private val viewModel by viewModels<BookTripViewModel>()
    private val driversMap = SparseArray<DriverModel>()
    private val mapFragment: SupportMapFragment? by lazy {
        supportFragmentManager.findFragmentById(R.id.bookTripGoogleMap) as SupportMapFragment?
    }

    private val geoCoder: Geocoder? by lazy {
        Geocoder(this, Locale(getString(R.string.lang_key)))
    }


    //    private val userBitMap by lazy {
//        viewModel.mapUtility.getBitmapFromVectorDrawable(
//            context = this,
//            drawableId = R.drawable.ic_current_location
//        )
//    }
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

    private val binding: ActivityBookTripBinding by binding(R.layout.activity_book_trip)

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
            viewModel = this@BookTripActivity.viewModel

            dropOffView = dropOffViewConstant
            pickupView = pickupViewConstant
            confirmView = confirmViewConstant

            // Todo Location Disabled View click Listener
            noLocationView.run {
                noLocationCardView.setOnClickListener {
                    this@BookTripActivity.viewModel.mapUtility.showLocationPrompt(this@BookTripActivity)
                }
            }

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
                offersRV.adapter = ridesAdapter
                backIv.setOnClickListener { onBackPressed() }
                selectBT.setOnClickListener {
                    val dropOffLocation = dropOffMarker?.position ?: return@setOnClickListener
                    val pickupLocation = pickUpMarker?.position ?: return@setOnClickListener

                    val model = InProgressModel(
                        dropLat = dropOffLocation.latitude,
                        dropLon = dropOffLocation.longitude,
                        dropName = this@BookTripActivity.viewModel.dropOffPlaceTitleText.value
                            ?: "",

                        pickLat = pickupLocation.latitude,
                        pickLon = pickupLocation.longitude,
                        pickName = this@BookTripActivity.viewModel.pickupPlaceTitleText.value ?: "",

                        tripID = 1,
                        tripState = Constants.driverIsComingConstant,
                        price = "200 EGP",
                        rideType = "Go اوفر ",
                        driverName = "Eslma Kamel",
                        driverPhone = "01555892962",
                        driverProfile = "https://avatars.githubusercontent.com/u/13874259?v=4"
                    )
                    val intent =
                        Intent(this@BookTripActivity, InProgressTripActivity::class.java).apply {
                            putExtra("item", model)
                        }
                    startActivity(intent)
                    this@BookTripActivity.finish()
                }
            }
        }
    }

    private fun pickUpClick() {
        ridesAdapter.submitList(rides)

        this@BookTripActivity.viewModel.viewType.value = confirmViewConstant

        lifecycleScope.launchWhenStarted {

            val map = getMap() ?: return@launchWhenStarted
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude
            val pickupLocation = LatLng(lat, lon)
            map.drawPickUpLocation(location = pickupLocation)

            val dropOffLocation = dropOffMarker?.position ?: return@launchWhenStarted
            val lineOptions =
                viewModel.directionRepo.directionDataAsync(
                    options = viewModel.polylineOptions,
                    from = pickupLocation,
                    to = dropOffLocation
                )
                    ?: return@launchWhenStarted
            if (viewModel.viewType.value != confirmViewConstant) return@launchWhenStarted
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
        this@BookTripActivity.viewModel.viewType.value = pickupViewConstant

        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
            val lat = map.cameraPosition.target.latitude
            val lon = map.cameraPosition.target.longitude

            this@BookTripActivity.viewModel.clearPickupPlaceTitleText()
            getCurrentLocation()
            map.drawDropOffLocation(location = LatLng(lat, lon))
        }

    }

    private fun getCurrentLocation() {
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
            this@BookTripActivity.viewModel.locationFlow.map { string -> string?.toGSON() }
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

    private fun updateClick() {
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
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
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
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
        lifecycleScope.launchWhenStarted {
            val map = getMap() ?: return@launchWhenStarted
            val location = viewModel.mapUtility.getAddress(
                geocoder = geoCoder ?: return@launchWhenStarted,
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

                viewModel.isSelected.value = false
                ridesAdapter.clear()

            }

            pickupViewConstant -> {
                viewModel.viewType.value = dropOffViewConstant
                getCurrentLocation()
                dropOffMarker?.remove()
            }

            else -> super.onBackPressed()
        }
    }

    override fun onLocationChangeAbstraction(locationValue: Boolean) {
        viewModel.isLocationEnabled.value = locationValue
    }

    override fun onItemClick() {
        viewModel.isSelected.value = true
    }

}