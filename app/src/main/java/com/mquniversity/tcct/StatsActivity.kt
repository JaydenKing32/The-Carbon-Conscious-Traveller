package com.mquniversity.tcct

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.ViewPortHandler
import com.mquniversity.tcct.shared.TripViewModel
import kotlinx.datetime.Instant
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.time.MonthDay
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class StatsActivity : AppCompatActivity() {
    private val tripViewModel: TripViewModel by viewModel<TripViewModel>()
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
        chart.description.isEnabled = false

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.granularity = 1f

        val xAxisFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase?): String? {
                return if (value == 0f) {
                    ""
                } else {
                    Year.of(calendar.get(Calendar.YEAR)).atDay(value.toInt())
                        .format(DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault()))
                }
            }
        }

        val markerView = object : MarkerView(this, R.layout.chart_marker) {
            private var markerText: TextView = this.findViewById(R.id.marker_text)

            @SuppressLint("SetTextI18n")
            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                super.refreshContent(e, highlight)
                markerText.text = "${xAxisFormatter.getFormattedValue(e!!.x, null)}: %.2f".format(e.y)
            }
        }
        markerView.chartView = chart
        chart.marker = markerView
        xAxis.valueFormatter = xAxisFormatter

        chart.axisLeft.setPosition(YAxisLabelPosition.OUTSIDE_CHART)
        chart.axisRight.isEnabled = false

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
        val trips = tripViewModel.tripsFromMonth(Instant.fromEpochMilliseconds(calendar.time.time))

        if (trips.isEmpty()) {
            val paint: Paint = chart.getPaint(Chart.PAINT_INFO)
            paint.textSize = 60f
            paint.color = Color.BLACK
            chart.clear()
            chart.invalidate()
            return
        }

        val maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = trips.groupBy { trip -> trip.dayOfYear }
        val emissions = ArrayList<BarEntry>(maxDaysInMonth)
        val reductions = ArrayList<BarEntry>(maxDaysInMonth)

        val time = Year.of(calendar.get(Calendar.YEAR)).atMonthDay(MonthDay.of(calendar.get(Calendar.MONTH) + 1, 1))
        val minDayOfYear = time.dayOfYear

        for (i in 0..<maxDaysInMonth) {
            val dayIndex = i + minDayOfYear
            if (days.containsKey(dayIndex)) {
                emissions.add(BarEntry(dayIndex.toFloat(), days[dayIndex]!!.sumOf { t ->
                    if (t.complete) t.emissions.toDouble() / 1000 else 0.0
                }.toFloat()))
                reductions.add(BarEntry(dayIndex.toFloat(), days[dayIndex]!!.sumOf { t ->
                    if (t.complete) t.reduction.toDouble() / 1000 else 0.0
                }.toFloat()))
            } else {
                emissions.add(BarEntry(dayIndex.toFloat(), 0f))
                reductions.add(BarEntry(dayIndex.toFloat(), 0f))
            }
        }

        val emissionSet = BarDataSet(emissions, "Total emissions")
        val reductionSet = BarDataSet(reductions, "Emission reduction")
        reductionSet.color = Color.GREEN

        chart.data = BarData(emissionSet, reductionSet)
        chart.barData.setValueFormatter(object : IValueFormatter {
            override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String {
                return if (value > 0) {
                    "%.2f".format(value)
                } else {
                    ""
                }
            }
        })

        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per group
        val groupSpace = 0.06f
        val barSpace = 0.02f
        val barWidth = 0.45f

        chart.barData.barWidth = barWidth
        chart.xAxis.axisMinimum = minDayOfYear.toFloat()
        chart.xAxis.axisMaximum = minDayOfYear + chart.barData.getGroupWidth(groupSpace, barSpace) * maxDaysInMonth
        chart.groupBars(minDayOfYear.toFloat(), groupSpace, barSpace)

        chart.invalidate()
    }
}
