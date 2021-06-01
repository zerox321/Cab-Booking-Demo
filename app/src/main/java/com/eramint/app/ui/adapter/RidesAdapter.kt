package com.eramint.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eramint.app.databinding.RideRowItemBinding

class RidesAdapter(private val clickListener: ClickListener? = null) :
    ListAdapter<String, RidesAdapter.ViewHolder>(DC) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(item = currentList[position], clickListener = clickListener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            RideRowItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )


    inner class ViewHolder(private val binding: RideRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, clickListener: ClickListener?) {
            binding.run {
                root.setOnClickListener {
                    clickListener?.onItemClick()
                }
//                this.item = item
                executePendingBindings()
            }
        }
    }

    interface ClickListener {
        fun onItemClick()
    }

    private object DC : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
//            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
            oldItem == newItem
    }

}