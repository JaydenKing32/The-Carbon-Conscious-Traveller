package com.mquniversity.tcct

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.mquniversity.tcct.CalculationUtils.formatEmissionWithCO2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class AirplaneResultFragment(private val requestBody: RequestBody) : DialogFragment() {
    private lateinit var root: LinearLayout
    private lateinit var resultLayout: LinearLayout
    private lateinit var errorTextView: MaterialTextView
    private lateinit var progressBar: ProgressBar
    private lateinit var calAgainBtn: MaterialButton

    private val requestURL = "https://travelimpactmodel.googleapis.com/v1/flights:computeFlightEmissions?key="
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = layoutInflater.inflate(
            R.layout.airplane_result_container,
            null,
            false
        ) as LinearLayout

        resultLayout = root.findViewById(R.id.airplane_result_layout)
        for (i in 0 until 8) {
            val textView = MaterialTextView(requireContext())
            if (i % 2 == 0) {
                textView.textSize = 24f
            } else {
                textView.textSize = 18f
            }
            resultLayout.addView(textView)
        }
        (resultLayout[0] as MaterialTextView).text = getString(R.string.cabin_class_first)
        (resultLayout[2] as MaterialTextView).text = getString(R.string.cabin_class_business)
        (resultLayout[4] as MaterialTextView).text = getString(R.string.cabin_class_premium_economy)
        (resultLayout[6] as MaterialTextView).text = getString(R.string.cabin_class_economy)

        errorTextView = root.findViewById(R.id.airplane_error_text)

        calAgainBtn = root.findViewById(R.id.calculate_again_button)
        calAgainBtn.setOnClickListener {
            showResult()
        }
        progressBar = root.findViewById(R.id.airplane_progress_bar)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        showResult()
        return root
    }

    private fun showResult() {
        CoroutineScope(Dispatchers.IO).launch {
            root.post {
                calAgainBtn.isEnabled = false
                resultLayout.visibility = View.GONE
                errorTextView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
            }
            var response: ResponseBody? = null
            val connection = URL(requestURL + (requireActivity() as MainActivity).apiKey).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("charset", "utf-8")
            connection.setRequestProperty("Content-Type", "application/json")
            val json = gson.toJson(requestBody)
            val byteArray = json.toByteArray()
            connection.setRequestProperty("Content-length", byteArray.size.toString())
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(byteArray)
            outputStream.flush()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = DataInputStream(connection.inputStream)
                val reader = BufferedReader(InputStreamReader(inputStream))
                response = gson.fromJson(reader.readText(), ResponseBody::class.java)
                reader.close()
                inputStream.close()
            } else {
                Log.i("AirplaneQueryFragment getFlightEmission HttpRequest error", connection.responseMessage)
                Log.i("AirplaneQueryFragment getFlightEmission HttpRequest error", connection.responseCode.toString())
            }
            connection.disconnect()

            root.post {
                if (response?.flightEmissions != null) {
                    val emissions = response.flightEmissions[0].emissionsGramsPerPax
                    if (emissions != null) {
                        (resultLayout[1] as MaterialTextView).text = formatEmissionWithCO2(emissions.first.toFloat(), false)
                        (resultLayout[3] as MaterialTextView).text = formatEmissionWithCO2(emissions.business.toFloat(), false)
                        (resultLayout[5] as MaterialTextView).text = formatEmissionWithCO2(emissions.premiumEconomy.toFloat(), false)
                        (resultLayout[7] as MaterialTextView).text = formatEmissionWithCO2(emissions.economy.toFloat(), false)
                        resultLayout.visibility = View.VISIBLE
                    } else {
                        errorTextView.text = getString(R.string.flight_request_error)
                        errorTextView.visibility = View.VISIBLE
                    }
                } else {
                    errorTextView.text = getString(R.string.flight_error)
                    errorTextView.visibility = View.VISIBLE
                }
                progressBar.visibility = View.GONE
                calAgainBtn.isEnabled = true
            }
        }
    }
}
