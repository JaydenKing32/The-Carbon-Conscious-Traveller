package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class CarQueryFragment : PrivateVehicleQueryFragment() {
    private lateinit var sizeInput: TextInputLayout
    private lateinit var sizeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var sizeOptions: Array<String>
    private var currSizeIdx = -1

    private lateinit var fuelTypeInput: TextInputLayout
    private lateinit var fuelTypeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var fuelTypeOptions: Array<String>
    private var currFuelTypeIdx = -1

    private lateinit var fuelTypes: Array<String>
    private lateinit var carValues: Array<FloatArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedCar = pref.getBoolean(getString(R.string.use_specified_car_key), false)

        if (specifiedCar) {
            val carSize = pref.getString(getString(R.string.specified_car_size_key), getString(R.string.specified_car_size_default))
            val carFuel = pref.getString(getString(R.string.specified_car_fuel_key), getString(R.string.specified_car_fuel_default))

            setupResult(carSize!!, carFuel!!)
            return
        }

        headerText.text = getString(R.string.car)
        sizeDescBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.car_size_desc)
                .show()
        }

        fuelTypes = mainActivity.calculationValues.carFuelTypes
        carValues = mainActivity.calculationValues.carValuesMatrix.toTypedArray()

        sizeInput = TextInputLayout(ContextThemeWrapper(
            context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
        ))
        sizeOptions = mainActivity.calculationValues.carSizes.toTypedArray()
        insertQuery(sizeInput, "Size", sizeOptions)
        sizeInputDropdown = sizeInput.editText as MaterialAutoCompleteTextView

        fuelTypeInput = TextInputLayout(ContextThemeWrapper(
            context, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
        ))
        insertQuery(fuelTypeInput, "Fuel Type", emptyArray())
        fuelTypeInputDropdown = fuelTypeInput.editText as MaterialAutoCompleteTextView

        fuelTypeInput.isEnabled = false

        sizeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
            fuelTypeInput.isEnabled = false
            if (currSizeIdx != idx) {
                setFuelTypeItems(idx)
            }
            fuelTypeInput.isEnabled = true
        }
        fuelTypeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
            currFuelTypeIdx = idx
            calBtn.isEnabled = true
        }

        calBtn.setOnClickListener {
            setupResult(sizeOptions[currSizeIdx], fuelTypeOptions[currFuelTypeIdx])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mainLayout
    }

    private fun setFuelTypeItems(selectedSizeIdx: Int) {
        currSizeIdx = selectedSizeIdx
        calBtn.isEnabled = false
        val options = mutableListOf<String>()
        fuelTypeInputDropdown.text = null
        for (i in carValues[selectedSizeIdx].indices) {
            if (carValues[selectedSizeIdx][i] != 0f) {
                options.add(fuelTypes[i])
            }
        }
        fuelTypeOptions = options.toTypedArray()
        fuelTypeInputDropdown.setSimpleItems(fuelTypeOptions)
    }

    private fun setupResult(carSize: String, carFuelType: String) {
        mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        calBtn.isEnabled = false
        var carResultFrag = parentFragmentManager.findFragmentByTag(getString(R.string.tag_car_result)) as CarResultFragment?
        if (carResultFrag == null) {
            mainActivity.enableButtons(false)
            carResultFrag = CarResultFragment(carSize, carFuelType)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, carResultFrag, getString(R.string.tag_car_result))
                .addToBackStack(getString(R.string.tag_car_result))
                .commit()
        } else {
            carResultFrag.updateFactor(carSize, carFuelType)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, carResultFrag, getString(R.string.tag_car_result))
                .commit()
        }
        calBtn.isEnabled = true
    }
}
