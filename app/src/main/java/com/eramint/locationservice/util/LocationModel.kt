package com.eramint.locationservice.util

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class LocationModel(
    @SerializedName("fromLat") val fromLat: Double? = 0.0,
    @SerializedName("fromLon") val fromLon: Double? = 0.0,

    @SerializedName("toLat") val toLat: Double? = 0.0,
    @SerializedName("toLon") val toLon: Double? = 0.0
) {
    fun getFromLatLng() = LatLng(fromLat!!, fromLon!!)
    fun getToLatLng() = LatLng(toLat!!, toLon!!)
}

fun LocationModel.convertToString(): String = Gson().toJson(this)
fun String.toGSON(): LocationModel = Gson().fromJson(this, LocationModel::class.java)
