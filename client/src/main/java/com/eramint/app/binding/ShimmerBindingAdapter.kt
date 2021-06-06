package com.eramint.app.binding

import android.view.View
import androidx.databinding.BindingAdapter
import com.facebook.shimmer.ShimmerFrameLayout


@BindingAdapter("bindShimmerView")
fun ShimmerFrameLayout.bindShimmerView(isShimming: Boolean) {
    visibility = if (isShimming) {
        stopShimmerAnimation()
        View.GONE
    } else {
        startShimmerAnimation()
        View.VISIBLE

    }

}