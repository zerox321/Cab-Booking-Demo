package com.eramint.locationservice.util

import android.graphics.Color
import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger

object RouteUtility {
    private const val directionUrl = "https://maps.googleapis.com/maps/api/directions/json?"

    private fun getDirectionUrl(
        currentLat: Double,
        currentLong: Double,
        destinationLat: Double,
        destinationLong: Double,
        Key: String
    ): String {
        return "${directionUrl}origin=${currentLat},${currentLong}&destination=${destinationLat},${destinationLong}&sensor=false&key=${Key}"
    }
    suspend fun directionDataAsync(
        options:PolylineOptions,
        from: LatLng,
        to: LatLng
    ): PolylineOptions? {
        return withContext(IO) {
                val url = getDirectionUrl(
                    from.latitude,
                    from.longitude,
                    to.latitude,
                    to.longitude,
                    "AIzaSyCWBFBPsc5nHelid5Dop0o-TTmeRTscpac"
                )
                Log.e("directionDataAsync", "url:  url $url")

                try {
                    val result = URL(url).readText()
                    // When API call is done, create parser and convert into JsonObjec
                    val parser: Parser = Parser()
                    val stringBuilder: StringBuilder = StringBuilder(result)
                    val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                    // get to the correct element in JsonObject
                    val routes = json.array<JsonObject>("routes")
                    val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                    // For every element in the JsonArray, decode the polyline string and pass all points to a List
                    val polypts = points.flatMap {
                        decodePoly(it.obj("polyline")?.string("points")!!)
                    }
                    // Add  points to polyline and bounds
                    options.add(from)
                    //LatLongB.include(sydney)
                    for (point in polypts) {
                        options.add(point)
                        //LatLongB.include(point)
                    }
                    options.add(to)
                    options
                } catch (t: Throwable) {
                    Logger.getLogger("(HTTPLog)-Static: isSBSettingEnabled false").level =
                        Level.OFF
                    null
                }
            }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

}