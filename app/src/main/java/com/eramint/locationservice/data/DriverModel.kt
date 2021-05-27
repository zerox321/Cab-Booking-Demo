package com.eramint.locationservice.data

import com.eramint.locationservice.util.LocationModel
import com.google.android.gms.maps.model.Marker

data class DriverModel(val driverID: Int, var driver: LocationModel, val driverMarker: Marker?)
