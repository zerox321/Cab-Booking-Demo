package com.eramint.app.ui.inProgressTrip

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InProgressModel(
    val dropLat: Double,
    val dropLon: Double,
    val dropName: String,

    val price: String,
    val rideType: String,

    val tripID: Int,
    val tripState: Int,

    val pickLat: Double,
    val pickLon: Double,
    val pickName: String,

    ):Parcelable
