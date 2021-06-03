package com.eramint.app.ui.bookTrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eramint.app.databinding.RideRowItemBinding

class RidesAdapter(private val clickListener: ClickListener? = null) :
    ListAdapter<String, RidesAdapter.ViewHolder>(DC) {
    var selectedPosition = -1
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = currentList[position],
            clickListener = clickListener,
            position = position
        )
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            RideRowItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    fun clear() {
        selectedPosition = -1
        submitList(null)
    }


    inner class ViewHolder(private val binding: RideRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, clickListener: ClickListener?, position: Int) {
            binding.run {
                root.setOnClickListener {
                    if (selectedPosition == position) return@setOnClickListener
                    if (selectedPosition != -1) notifyItemChanged(selectedPosition)
                    selectedPosition = position

                    notifyItemChanged(position)

                    clickListener?.onItemClick()

                }
                selectedIV.visibility = if (selectedPosition == position)
                    View.VISIBLE
                else
                    View.GONE


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