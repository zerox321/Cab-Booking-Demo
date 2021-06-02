package com.eramint.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object LocationLauncher {

    fun Context.locationAction(latLong: String) {
        val directionsBuilder: Uri.Builder = Uri.Builder()
            .scheme("https")
            .authority("www.google.com")
            .appendPath("maps")
            .appendPath("dir")
            .appendPath("")
            .appendQueryParameter("api", "1")
            .appendQueryParameter(
                "destination",
                latLong
            )
        this.startActivity(Intent(Intent.ACTION_VIEW, directionsBuilder.build()))
    }
}