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
import com.mquniversity.tcct.shared.motorcycleSizes

class MotorcycleQueryFragment : PrivateVehicleQueryFragment() {
    private lateinit var sizeInput: TextInputLayout
    private lateinit var sizeInputDropdown: MaterialAutoCompleteTextView
    private var currSizeIdx = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val specifiedBike = pref.getBoolean(getString(R.string.use_specified_motorcycle_key), false)

        if (specifiedBike) {
            setupResult(
                pref.getString(getString(R.string.specified_motorcycle_size_key), getString(R.string.specified_motorcycle_size_default))!!
            )
            return
        }

        headerText.text = getString(R.string.motorcycle)
        sizeDescBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.motorcycle_size_desc)
                .show()
        }

        sizeInput = TextInputLayout(
            ContextThemeWrapper(
                context,
                com.google.android.material
                    .R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
            )
        )
        insertQuery(sizeInput, "Size", motorcycleSizes)
        sizeInputDropdown = (sizeInput.editText as MaterialAutoCompleteTextView)
        sizeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
            currSizeIdx = idx
            calBtn.isEnabled = true
        }

        calBtn.setOnClickListener {
            setupResult(motorcycleSizes[currSizeIdx])
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mainLayout
    }

    private fun setupResult(size: String) {
        mainActivity.bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        calBtn.isEnabled = false
        var bikeResultFrag = parentFragmentManager.findFragmentByTag(getString(R.string.tag_motorcycle_result)) as MotorcycleResultFragment?
        if (bikeResultFrag == null) {
            mainActivity.enableButtons(false)
            bikeResultFrag = MotorcycleResultFragment(size)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, bikeResultFrag, getString(R.string.tag_motorcycle_result))
                .addToBackStack(getString(R.string.tag_motorcycle_result))
                .commit()
        } else {
            bikeResultFrag.updateFactor(size)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, bikeResultFrag, getString(R.string.tag_motorcycle_result))
                .commit()
        }
        calBtn.isEnabled = true
    }
}
