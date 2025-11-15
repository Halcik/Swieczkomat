package com.example.swieczkomat.vievmodels

import android.content.Context
import android.content.SharedPreferences
import com.example.swieczkomat.data.Candle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

class CandlesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("candles", Context.MODE_PRIVATE)
    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()
    private val idGen = AtomicLong(System.currentTimeMillis())

    init { load() }

    private fun load() {
        val raw = prefs.getString("data", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Candle>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(Candle.fromJson(obj))
        }
        _candles.value = list.sortedByDescending { it.createdDate }
    }

    private fun persist() {
        val arr = JSONArray()
        _candles.value.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("data", arr.toString()).apply()
    }

    fun add(candle: Candle) {
        _candles.value = _candles.value + candle
        persist()
    }

    fun updateBurnTime(id: Long, minutes: Int) {
        _candles.value = _candles.value.map { if (it.id == id) it.copy(burnTimeMinutes = minutes) else it }
        persist()
    }

    fun nextId(): Long = idGen.incrementAndGet()
}

