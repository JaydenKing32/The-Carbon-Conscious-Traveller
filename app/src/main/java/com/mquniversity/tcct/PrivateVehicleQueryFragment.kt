package com.mquniversity.tcct

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView

abstract class PrivateVehicleQueryFragment : Fragment() {
    protected lateinit var mainLayout: LinearLayout
    protected lateinit var headerText: MaterialTextView
    protected lateinit var sizeDescBtn: MaterialButton
    protected lateinit var calBtn: MaterialButton

    protected lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        mainLayout = layoutInflater.inflate(R.layout.query_container, null, false) as LinearLayout
        val shapeDivider = GradientDrawable()
        shapeDivider.setSize(0, 32)
        mainLayout.dividerDrawable = shapeDivider
        mainLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE

        headerText = mainLayout.findViewById(R.id.header_text)
        sizeDescBtn = mainLayout.findViewById(R.id.options_desc_button)
        calBtn = mainLayout.findViewById(R.id.calculate_button)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mainLayout
    }

    protected fun insertQuery(menu: TextInputLayout, label: String, options: Array<String>) {
        menu.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        menu.hint = label
        val dropDown = MaterialAutoCompleteTextView(ContextThemeWrapper(
            context, com.google.android.material.R.style.ThemeOverlay_Material3_AutoCompleteTextView_OutlinedBox
        ))
        dropDown.inputType = InputType.TYPE_NULL
        dropDown.setSimpleItems(options)
        menu.addView(dropDown)
        mainLayout.addView(menu, mainLayout.childCount - 1)
    }
}
