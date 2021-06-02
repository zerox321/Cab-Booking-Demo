package com.eramint.app.base

import android.content.*
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.eramint.app.location.ForegroundOnlyLocationService
import com.eramint.app.location.GpsLocationReceiver
import com.eramint.app.location.LocationChangeInterface
import timber.log.Timber


abstract class LocationActivity : BaseActivity(), LocationChangeInterface {


    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null


    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true

            if (foregroundPermissionApproved()) {


                startLocationService()
            } else {
                requestForegroundPermissions()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }


    private val br: GpsLocationReceiver by lazy {
        GpsLocationReceiver(
            locationChangeInterface = this
        )
    }
    private val filter: IntentFilter by lazy { IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(br, filter)
    }

    override fun onStart() {
        super.onStart()

        bindService(
            Intent(this, ForegroundOnlyLocationService::class.java),
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )

    }


    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }

        super.onStop()

    }


    override fun onResume() {
        super.onResume()
        onLocationChangeAbstraction(locationValue = locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER))
    }


    override fun onLocationChange(locationValue: Boolean) {
        onLocationChangeAbstraction(locationValue = locationValue)
    }

    override fun onLocationPermissionEnabled() {
        startLocationService()
    }

    private fun startLocationService() {
        foregroundOnlyLocationService?.subscribeToLocationUpdates(
            dataStore = dataStore,
            notificationManager = notificationManager
        ) ?: Timber.d(TAG, "Service Not Bound")
    }
     fun stopLocationService(){
        foregroundOnlyLocationService?.unsubscribeToLocationUpdates(
            dataStore = dataStore,
            notificationManager = notificationManager
        ) ?: Timber.d(TAG, "Service Not Bound")
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        stopLocationService()
//    }
    abstract fun onLocationChangeAbstraction(locationValue: Boolean)
}