package com.eramint.app.base

import android.Manifest
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.eramint.app.BuildConfig
import com.eramint.app.R
import com.eramint.app.location.ForegroundOnlyLocationService
import com.eramint.app.location.GpsLocationReceiver
import com.eramint.app.location.LocationChangeInterface
import com.eramint.app.util.Constants.REQUESTFOREGROUNDONLYPERMISSIONSREQUEST_CODE
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import timber.log.Timber


abstract class LocationActivity : BaseActivity(), LocationChangeInterface {


    internal val defaultContext = Dispatchers.Main

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
                foregroundOnlyLocationService?.subscribeToLocationUpdates(dataStore = dataStore)
                    ?: Timber.d(TAG, "Service Not Bound")
            } else {
                requestForegroundPermissions()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    private val locationManger: LocationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val br: GpsLocationReceiver by lazy {
        GpsLocationReceiver(
            locationManger = locationManger,
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

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    internal fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            val view = currentFocus ?: return
            Snackbar.make(
                view,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@LocationActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUESTFOREGROUNDONLYPERMISSIONSREQUEST_CODE
                    )
                }
                .show()
        } else {
            Timber.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                this@LocationActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUESTFOREGROUNDONLYPERMISSIONSREQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUESTFOREGROUNDONLYPERMISSIONSREQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Timber.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates(dataStore)
                else -> {
                    // Permission denied.
                    val view = currentFocus ?: return
                    Snackbar.make(
                        view,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    override fun onLocationChange(locationValue: Boolean) {
        onLocationChangeAbstraction(locationValue = locationValue)
    }

    abstract fun onLocationChangeAbstraction(locationValue: Boolean)
}