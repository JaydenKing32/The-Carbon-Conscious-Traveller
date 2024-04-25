package com.mquniversity.tcct

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mquniversity.tcct.shared.Trip
import org.koin.androidx.viewmodel.ext.android.viewModel

class TripActivity : AppCompatActivity() {
    private val tripViewModel: TripViewModel by viewModel<TripViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        setSupportActionBar(findViewById(R.id.trip_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.trip_recyclerview)
        val adapter = TripListAdapter(
            { trip: Trip -> tripViewModel.delete(trip) },
            { trip: Trip -> tripViewModel.setComplete(trip) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        tripViewModel.allTrips.observe(this) { trips -> trips.let { adapter.submitList(it) } }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
