package com.mquniversity.tcct

import android.icu.util.Calendar
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TripActivity : AppCompatActivity() {
    private val tripViewModel: TripViewModel by viewModels {
        TripViewModelFactory((application as TripApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)

        setSupportActionBar(findViewById(R.id.trip_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.trip_recyclerview)
        val adapter = TripListAdapter { tripViewModel.delete(it) }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        tripViewModel.allTrips.observe(this) { trips -> trips.let { adapter.submitList(it) } }

        // val cal = Calendar.getInstance()
        // tripViewModel.tripsFromYear(cal.time).observe(this) {
        //     println()
        //     for (trip in it) {
        //         println(trip)
        //     }
        // }
        // tripViewModel.tripsFromMonth(cal.time).observe(this) {
        //     println()
        //     for (trip in it) {
        //         println(trip)
        //     }
        // }
        // tripViewModel.tripsFromWeek(cal.time).observe(this) {
        //     println()
        //     for (trip in it) {
        //         println(trip)
        //     }
        // }
        // val cal2 = Calendar.getInstance()
        // cal2.add(Calendar.WEEK_OF_YEAR, -1)
        // tripViewModel.tripsFromWeek(cal2.time).observe(this) {
        //     println()
        //     for (trip in it) {
        //         println(trip)
        //     }
        // }
        // tripViewModel.tripsFromDay(cal.time).observe(this) {
        //     println()
        //     for (trip in it) {
        //         println(trip)
        //     }
        // }
        // println("test")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
