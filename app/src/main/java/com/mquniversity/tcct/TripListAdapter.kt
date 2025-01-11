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
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
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

                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3600000L).build()
                    val lsrBuilder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false)
                    val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)
                    val lsrTask: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(lsrBuilder.build())
                    lsrTask.addOnSuccessListener lsrTask@{ lsr: LocationSettingsResponse? ->
                        if (lsr?.locationSettingsStates?.isLocationUsable != true) {
                            return@lsrTask
                        }
                        val currLocRequest = CurrentLocationRequest.Builder()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                            .build()
                        val cts = CancellationTokenSource()
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        val locTask = fusedLocationClient.getCurrentLocation(currLocRequest, cts.token)
                        locTask.addOnSuccessListener locTask@{ curLoc ->
                            if (curLoc == null) {
                                return@locTask
                            }
                            val bound = LatLngBounds(
                                LatLng(trip.destLat - VERIFICATION_RADIUS, trip.destLng - VERIFICATION_RADIUS),
                                LatLng(trip.destLat + VERIFICATION_RADIUS, trip.destLng + VERIFICATION_RADIUS)
                            )

                            if (!bound.contains(LatLng(curLoc.latitude, curLoc.longitude))) {
                                Snackbar.make(
                                    completeButton.rootView, "Current location does not match end location", Snackbar.LENGTH_SHORT
                                ).show()
                                return@locTask
                            }

                            trip.complete = true
                            completeButton.setImageResource(R.drawable.outline_check_circle_24)
                            updateFun(trip)
                        }
                        locTask.addOnFailureListener {
                            Snackbar.make(
                                completeButton.rootView,
                                "Could not retrieve current location. Please try again later.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
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
