package com.example.swieczkomat.ui.util

import com.example.swieczkomat.data.Material

object MaterialUtils {
    fun extractCapacity(name: String): Double {
        val regex = "([0-9.]+)".toRegex()
        val match = regex.find(name)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }
    fun getPricePerUnit(material: Material?): Double {
        return material?.let { if (it.quantity > 0) it.price / it.quantity else 0.0 } ?: 0.0
    }
}
