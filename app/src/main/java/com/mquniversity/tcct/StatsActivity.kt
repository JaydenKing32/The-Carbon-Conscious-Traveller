package com.mquniversity.tcct

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class StatsActivity : AppCompatActivity() {
    private val tripViewModel: TripViewModel by viewModels {
        TripViewModelFactory((application as TripApplication).repository)
    }
    private val calendar = Calendar.getInstance()
    private lateinit var chart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        setSupportActionBar(findViewById(R.id.stats_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        chart = findViewById(R.id.chart)
        chart.setNoDataText(getString(R.string.chart_no_data))

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                return Year.of(calendar.get(Calendar.YEAR)).atDay(value.toInt())
                    .format(DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault()))
            }
        }

        val leftAxis = chart.axisLeft
        leftAxis.setPosition(YAxisLabelPosition.OUTSIDE_CHART)

        val leftButton = findViewById<Button>(R.id.chart_left_button)
        val rightButton = findViewById<Button>(R.id.chart_right_button)

        leftButton.setOnClickListener {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
            setData()
        }
        rightButton.setOnClickListener {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1)
            setData()
        }

        setData()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setData() {
        val chartTitle = findViewById<TextView>(R.id.chart_title)
        chartTitle.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        tripViewModel.tripsFromMonth(calendar.time).observe(this) {
            if (it.isEmpty()) {
                val paint: Paint = chart.getPaint(Chart.PAINT_INFO)
                paint.textSize = 60f
                paint.color = Color.BLACK
                chart.clear()
                chart.invalidate()
                return@observe
            }

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
}
