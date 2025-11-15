package com.example.swieczkomat.vievmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swieczkomat.data.Candle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CandlesViewModel(application: Application): AndroidViewModel(application) {
    private val repo = CandlesRepository(application.applicationContext)
    val candles: StateFlow<List<Candle>> = repo.candles

    private fun todayString(): String {
        val cal = java.util.Calendar.getInstance()
        val y = cal.get(java.util.Calendar.YEAR)
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", y, m, d)
    }
    private fun addDays(base: String, days: Int): String {
        val parts = base.split('-')
        return try {
            val cal = java.util.Calendar.getInstance().apply {
                set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                add(java.util.Calendar.DAY_OF_YEAR, days)
            }
            val y = cal.get(java.util.Calendar.YEAR)
            val m = cal.get(java.util.Calendar.MONTH) + 1
            val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
            String.format("%04d-%02d-%02d", y, m, d)
        } catch (e: Exception) { base }
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
        waxAmount: Double,
        fragranceAmount: Double,
        recipient: String
    ) {
        val created = todayString()
        val fire = addDays(created, 14)
        val candle = Candle(
            id = repo.nextId(),
            createdDate = created,
            fireDate = fire,
            count = count,
            containerName = container,
            waxName = wax,
            fragranceName = fragrance,
            wickName = wick,
            dyeName = dye,
            concentration = concentration,
            totalPrice = totalPrice,
            waxAmount = waxAmount,
            fragranceAmount = fragranceAmount,
            recipient = recipient
        )
        viewModelScope.launch { repo.add(candle) }
    }
}
