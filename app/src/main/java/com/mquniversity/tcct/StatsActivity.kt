package com.mquniversity.tcct

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class StatsActivity : AppCompatActivity() {
    private val tripViewModel: TripViewModel by viewModels {
        TripViewModelFactory((application as TripApplication).repository)
    }
    private var year = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        setSupportActionBar(findViewById(R.id.stats_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val chart = findViewById<BarChart>(R.id.chart)

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                return Year.of(year).atDay(value.toInt()).format(DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault()))
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)

        // TODO: retrieve trips from a specified date
        val cal = Calendar.getInstance()
        tripViewModel.tripsFromMonth(cal.time).observe(this) {
            val days = it.groupBy { trip -> trip.dayOfYear() }
            val set = BarDataSet(days.map { (_, trips) ->
                BarEntry(trips.first().dayOfYear().toFloat(), trips.sumOf { t -> t.emissions.toDouble() }.toFloat())
            }, "Emissions")

            val data = BarData(listOf(set))
            chart.data = data
            chart.setFitBars(true)
            chart.invalidate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
