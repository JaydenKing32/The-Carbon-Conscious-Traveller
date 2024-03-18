package com.mquniversity.tcct

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class TripListAdapter(
    private val deleteFun: (Trip) -> Unit,
    private val updateFun: (Trip) -> Unit
) : ListAdapter<Trip, TripListAdapter.TripViewHolder>(TripComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        return TripViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, deleteFun, updateFun)
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tripItemView: ConstraintLayout = itemView.findViewById(R.id.trip_item)

        fun bind(trip: Trip, deleteFun: (Trip) -> Unit, updateFun: (Trip) -> Unit) {
            val icon = tripItemView.findViewById<ImageView>(R.id.trip_item_icon)
            val dateTextView = tripItemView.findViewById<TextView>(R.id.trip_item_date)
            val emissionTextView = tripItemView.findViewById<TextView>(R.id.trip_item_emission)
            val reductionTextView = tripItemView.findViewById<TextView>(R.id.trip_item_reduction)
            val removeButton = tripItemView.findViewById<ImageView>(R.id.trip_item_remove)
            val completeButton = tripItemView.findViewById<ImageView>(R.id.trip_item_complete)
            val context = tripItemView.context

            icon.setImageResource(when (trip.mode) {
                TransportMode.CAR -> R.drawable.outline_directions_car_24
                TransportMode.MOTORCYCLE -> R.drawable.outline_sports_motorsports_24
                TransportMode.PUBLIC_TRANSPORT -> R.drawable.outline_directions_subway_24
                TransportMode.AIRPLANE -> R.drawable.outline_flight_24
                // else -> R.drawable.outline_directions_walk_24
            })
            dateTextView.text = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.forLanguageTag("en_AU")).format(trip.date)
            emissionTextView.text = CalculationUtils.formatEmission(trip.emissions)
            reductionTextView.text = CalculationUtils.formatEmission(trip.reduction)
            removeButton.setOnClickListener { deleteFun(trip) }
            dateTextView.setOnClickListener {
                AlertDialog.Builder(context).setMessage(trip.multilineString()).create().show()
            }
            if (trip.complete) {
                completeButton.setImageResource(R.drawable.outline_check_circle_24)
            } else {
                completeButton.setImageResource(R.drawable.outline_cross_circle_24)
                completeButton.setOnClickListener {
                    trip.complete = true
                    updateFun(trip)
                    completeButton.setImageResource(R.drawable.outline_check_circle_24)
                }
            }
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
