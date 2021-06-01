package com.eramint.app.binding


import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.eramint.app.R


@BindingAdapter("bindListDecoration")
fun RecyclerView.bindListDecoration(attach: Boolean?) {
    if (attach == true)
        setDivider(R.drawable.recycler_list_line_divider)

}

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val drawable = ContextCompat.getDrawable(context, drawableRes) ?: return
    val divider = DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
    divider.setDrawable(drawable)
    addItemDecoration(divider)
}

