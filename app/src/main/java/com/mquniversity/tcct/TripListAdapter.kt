package com.mquniversity.tcct

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.snackbar.Snackbar
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
                    if (
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Snackbar.make(
                            completeButton.rootView, "Location needs to be enabled to retrieve current location", Snackbar.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val placesClient = Places.createClient(context)
                    val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.LAT_LNG))
                    val placeResult = placesClient.findCurrentPlace(request)

                    placeResult.addOnCompleteListener { task ->
                        if (!task.isSuccessful || task.result == null) {
                            return@addOnCompleteListener
                        }
                        val curPlace = task.result.placeLikelihoods[0].place

                        val bound = LatLngBounds(
                            LatLng(trip.destLat - VERIFICATION_RADIUS, trip.destLng - VERIFICATION_RADIUS),
                            LatLng(trip.destLat + VERIFICATION_RADIUS, trip.destLng + VERIFICATION_RADIUS)
                        )

                        if (!bound.contains(curPlace.latLng!!)) {
                            Snackbar.make(
                                completeButton.rootView, "Current location does not match end location", Snackbar.LENGTH_SHORT
                            ).show()
                            return@addOnCompleteListener
                        }
                        trip.complete = true
                        completeButton.setImageResource(R.drawable.outline_check_circle_24)
                        updateFun(trip)
                    }
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
