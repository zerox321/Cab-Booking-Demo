package com.eramint.app.data

import com.google.android.gms.maps.model.Marker

data class DriverModel(val driverID: Int, var driver: LocationModel, val driverMarker: Marker?)
