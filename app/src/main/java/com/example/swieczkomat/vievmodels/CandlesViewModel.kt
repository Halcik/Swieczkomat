package com.example.swieczkomat.vievmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swieczkomat.data.Candle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class CandlesViewModel(application: Application): AndroidViewModel(application) {
    private val repo = CandlesRepository(application.applicationContext)
    val candles: StateFlow<List<Candle>> = repo.candles

    private fun extractCapacity(name: String?): Double {
        if (name.isNullOrBlank()) return 0.0
        val regex = "([0-9]+(\\.[0-9]+)?)".toRegex()
        val match = regex.find(name)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    fun addCandle(
        count: Int,
        container: String?,
        wax: String?,
        fragrance: String?,
        wick: String?,
        dye: String?,
        concentration: Double,
        totalPrice: Double,
        waxAmount: Double, // nieużywane - pozostawione dla kompatybilności
        fragranceAmount: Double, // nieużywane - pozostawione dla kompatybilności
        recipient: String
    ) {
        val singleCost = if (count > 0) totalPrice / count else 0.0
        val capacity = extractCapacity(container)
        val now = System.currentTimeMillis()
        val dateToLight = now + 14L * 24 * 60 * 60 * 1000 // +14 dni
        val list = (1..count).map {
            Candle(
                id = 0, // repo nada ID
                containerName = container,
                waxName = wax,
                fragranceName = fragrance,
                wickName = wick,
                dyeName = dye,
                concentration = concentration,
                capacity = capacity,
                cost = singleCost,
                forWhom = recipient,
                dateMade = now,
                dateToLight = dateToLight,
                burnTimeMinutes = 0
            )
        }
        viewModelScope.launch { repo.addAll(list) }
    }
}
