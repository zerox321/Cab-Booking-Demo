package com.eramint.app.binding

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.eramint.app.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import timber.log.Timber


@BindingAdapter(value = ["loadImage", "loadImageProgress"], requireAll = false)
fun ImageView.loadImage(
    imagePath: String?,
    progress: ProgressBar? = null
) {
    Timber.e("loadImage: $imagePath")
    val placeholder = R.mipmap.ic_launcher
    progress?.visibility = View.VISIBLE

    Picasso.get().load("$imagePath").error(placeholder).into(this, object : Callback {
        override fun onSuccess() {
            progress?.visibility = View.INVISIBLE
        }

        override fun onError(e: Exception?) {

            progress?.visibility = View.INVISIBLE
        }

    })
}
