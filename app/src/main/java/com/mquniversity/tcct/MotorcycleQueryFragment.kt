package com.mquniversity.tcct

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class MotorcycleQueryFragment: QueryFragment() {
    private lateinit var sizeInput: TextInputLayout
    private lateinit var sizeInputDropdown: MaterialAutoCompleteTextView
    private lateinit var sizeOptions: Array<String>
    private var currSizeIdx = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (!isInitialized) {
            isInitialized = true

            headerText.text = "Motorcycle"

            val args = requireArguments()

            sizeInput = TextInputLayout(
                ContextThemeWrapper(
                    context,
                    com.google.android.material
                        .R.style.Widget_Material3_TextInputLayout_OutlinedBox_ExposedDropdownMenu
                )
            )
            sizeOptions = args.getStringArray("motorcycleSizes")!!
            insertQuery(sizeInput, "Size", sizeOptions)
            sizeInputDropdown = (sizeInput.editText as MaterialAutoCompleteTextView)
            sizeInputDropdown.setOnItemClickListener { _, _, idx, _ ->
                currSizeIdx = idx
                calBtn.isEnabled = true
            }

            calBtn.setOnClickListener {
                calBtn.isEnabled = false
                val motorcycleResultFragment = MotorcycleResultFragment(sizeOptions[currSizeIdx])
                parentFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        motorcycleResultFragment,
                        getString(
                            R.string.tag_motorcycle_result)
                    )
                    .addToBackStack(getString(R.string.tag_motorcycle_result))
                    .commit()
            }
        }

        return mainLayout
    }
}