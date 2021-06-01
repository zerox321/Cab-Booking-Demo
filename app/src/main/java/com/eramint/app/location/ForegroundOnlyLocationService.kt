package com.eramint.app.location


import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.eramint.app.R
import com.eramint.app.data.LocationModel
import com.eramint.app.data.convertToString
import com.eramint.app.local.DataStore
import com.eramint.app.local.DataStoreImp.saveLocation
import com.eramint.app.ui.bookTrip.BookTripActivity
import com.eramint.app.util.Constants.NOTIFICATION_CHANNEL_ID
import com.eramint.app.util.Constants.NOTIFICATION_ID
import com.eramint.app.util.Constants.locationInterval
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
class ForegroundOnlyLocationService : Service() {
    private val TAG = "ForegroundOnlyLocation"

    private var dataStore: DataStore? = null

    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder by lazy{ LocalBinder() }
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(locationInterval)
            fastestInterval = TimeUnit.SECONDS.toMillis(locationInterval)
            maxWaitTime = TimeUnit.SECONDS.toMillis(locationInterval)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                val location = locationResult.lastLocation
                Timber.e(TAG, "onLocationResult:$location ")
                newLocation = LatLng(location.latitude, location.longitude)

                emitCurrentLocation()

                // Updates notification content if this service is running as a foreground
                // service.
                if (serviceRunningInForeground)
                    notificationManager.notify(NOTIFICATION_ID, generateNotification(newLocation))

            }
        }
    }

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var newLocation: LatLng? = null
    private var oldLocation: LatLng? = null

    override fun onCreate() {
        Timber.tag(TAG).d("onCreate()")

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d(TAG, "onStartCommand()")
        emitCurrentLocation()
        // Tells the system not to recreate the service after it's been killed.
        return START_NOT_STICKY
    }

    fun emitCurrentLocation() {

        GlobalScope.launch {
            if (newLocation == null) return@launch
            val old = oldLocation ?: newLocation
            val model =
                LocationModel(
                    fromLat = old?.latitude, fromLon = old?.longitude,
                    toLat = newLocation?.latitude, toLon = newLocation?.longitude
                )
            dataStore?.saveLocation(value = model.convertToString())
            oldLocation = newLocation
        }


    }

    override fun onBind(intent: Intent): IBinder {
        Timber.tag(TAG).d("onBind()")
        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Timber.tag(TAG).d("onRebind()")
        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.tag(TAG).d("onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange) {
            Timber.tag(TAG).d("Start foreground service")

            val notification = generateNotification(newLocation)
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onDestroy() {
        Timber.tag(TAG).d("onDestroy")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates(dataStore: DataStore) {
        this.dataStore = dataStore
        Timber.tag(TAG).d("subscribeToLocationUpdates")

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, ForegroundOnlyLocationService::class.java))

        try {
            // TODO: Step 1.5, Subscribe to location changes.
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (unlikely: SecurityException) {
            Timber.tag(TAG).d("Lost location permissions. Couldn't remove updates. $unlikely")

        }
    }

    fun unsubscribeToLocationUpdates() {
        Timber.tag(TAG).d("unsubscribeToLocationUpdates")


        try {
            // TODO: Step 1.6, Unsubscribe to location changes.
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag(TAG).d("Location Callback removed.")

                    stopSelf()
                } else {
                    Timber.tag(TAG).d("Failed to remove Location Callback.")

                }
            }
        } catch (unlikely: SecurityException) {
            Timber.tag(TAG).d("Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(location: LatLng?): Notification {
        Timber.tag(TAG).d( "generateNotification()")

        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val mainNotificationText = location.toText()
        val titleText = getString(R.string.app_name)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
            )
            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, BookTripActivity::class.java)


        val activityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(activityPendingIntent)
            .build()
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: ForegroundOnlyLocationService
            get() = this@ForegroundOnlyLocationService
    }

    private fun LatLng?.toText(): String {
        return if (this != null) {
            "($latitude, $longitude)"
        } else {
            "Unknown location"
        }
    }


}