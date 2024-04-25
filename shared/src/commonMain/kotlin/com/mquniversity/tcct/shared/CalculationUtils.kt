package com.mquniversity.tcct.shared

import kotlin.math.roundToInt

object CalculationUtils {
    const val DEFAULT_ONE_LEAF_CO2_GRAM = 100f
    const val DEFAULT_FOUR_LEAVES_CO2_GRAM = 1000f
    const val DEFAULT_TREE_BRANCH_CO2_GRAM = 5000f
    const val DEFAULT_TREE_CO2_GRAM = 29000f

    fun formatEmission(emissionInGram: Float): String {
        if (emissionInGram >= 1000) {
            return "${twoDecimalPlaces(emissionInGram / 1000)} kg"
        }
        return "${emissionInGram.roundToInt()} g"
    }

    fun formatEmissionWithCO2(emissionInGram: Float, equivalent: Boolean): String {
        val e = if (equivalent) "e" else ""
        if (emissionInGram >= 1000) {
            return "${twoDecimalPlaces(emissionInGram / 1000)} kg CO2${e}"
        }
        return "${emissionInGram.roundToInt()} g CO2${e}"
    }

    private fun twoDecimalPlaces(number: Float): String {
        val factor = 100
        val integerDigits = number.toInt()
        val floatDigits = ((number - integerDigits) * factor).roundToInt()
        return "${integerDigits}.${floatDigits}"
    }
}
