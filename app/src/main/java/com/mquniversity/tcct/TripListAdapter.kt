package com.mquniversity.tcct

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TripListAdapter : ListAdapter<Trip, TripListAdapter.TripViewHolder>(TripComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        return TripViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.destination)
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripItemView: TextView = itemView.findViewById(R.id.trip_item)

        fun bind(text: String?) {
            tripItemView.text = text
        }

        companion object {
            fun create(parent: ViewGroup): TripViewHolder {
                val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_trip_item, parent, false)
                return TripViewHolder(view)
            }
        }
    }

    class TripComparator : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}
