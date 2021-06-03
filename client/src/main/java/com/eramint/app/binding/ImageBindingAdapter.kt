package com.eramint.app.binding

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso


@BindingAdapter(value = ["loadImage", "loadImageProgress"], requireAll = false)
fun ImageView.loadImage(
    imagePath: String?,
    progress: ProgressBar? = null
) {
    progress?.visibility = View.VISIBLE

    Picasso.get().load("$imagePath").into(this, object : Callback {
        override fun onSuccess() {
            progress?.visibility = View.INVISIBLE
        }

        override fun onError(e: Exception?) {

            progress?.visibility = View.INVISIBLE
        }

    })
}
