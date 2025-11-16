package com.example.swieczkomat.vievmodels

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.swieczkomat.data.Candle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger

class CandlesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("candles", Context.MODE_PRIVATE)
    private val _candles = MutableStateFlow<List<Candle>>(emptyList())
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    // Używamy Long jako generatora, ale w Candle id jest Int (Room). Rzutujemy do Int.
    private val idGen = AtomicInteger(1)

    init { load() }

    private fun nextIntId(): Int = idGen.getAndIncrement()

    private fun load() {
        val raw = prefs.getString("data", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Candle>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(candleFromJson(obj))
        }
        _candles.value = list.sortedByDescending { it.dateMade }
    }

    private fun persist() {
        val arr = JSONArray()
        _candles.value.forEach { arr.put(candleToJson(it)) }
        prefs.edit { putString("data", arr.toString()) }
    }

    fun add(candle: Candle) {
        val finalId = when (candle.id) { 0 -> nextIntId(); else -> candle.id }
        _candles.value = _candles.value + candle.copy(id = finalId)
        persist()
    }

    fun addAll(newCandles: List<Candle>) {
        val withIds = newCandles.map { c -> c.copy(id = when (c.id) { 0 -> nextIntId(); else -> c.id }) }
        _candles.value = _candles.value + withIds
        persist()
    }

    fun updateBurnTime(targetId: Int, minutes: Int) {
        _candles.value = _candles.value.map { if (it.id.equals(targetId)) it.copy(burnTimeMinutes = minutes) else it }
        persist()
    }

    // JSON helpers dopasowane do pól Candle (Room + nasze dialogi)
    private fun candleToJson(c: Candle): JSONObject = JSONObject().apply {
        put("id", c.id)
        put("containerName", c.containerName)
        put("waxName", c.waxName)
        put("fragranceName", c.fragranceName)
        put("wickName", c.wickName)
        put("dyeName", c.dyeName)
        put("concentration", c.concentration)
        put("capacity", c.capacity)
        put("cost", c.cost)
        put("forWhom", c.forWhom)
        put("dateMade", c.dateMade)
        put("dateToLight", c.dateToLight)
        put("burnTimeMinutes", c.burnTimeMinutes)
    }

    private fun candleFromJson(o: JSONObject): Candle = Candle(
        id = o.optInt("id", 0),
        containerName = o.optString("containerName", null).takeIf { it.isNotBlank() },
        waxName = o.optString("waxName", null).takeIf { it.isNotBlank() },
        fragranceName = o.optString("fragranceName", null).takeIf { it.isNotBlank() },
        wickName = o.optString("wickName", null).takeIf { it.isNotBlank() },
        dyeName = o.optString("dyeName", null).takeIf { it.isNotBlank() },
        concentration = o.optDouble("concentration", 0.0),
        capacity = o.optDouble("capacity", 0.0),
        cost = o.optDouble("cost", 0.0),
        forWhom = o.optString("forWhom", ""),
        dateMade = o.optLong("dateMade", System.currentTimeMillis()),
        dateToLight = o.optLong("dateToLight", System.currentTimeMillis()),
        burnTimeMinutes = o.optInt("burnTimeMinutes", 0)
    )
}
