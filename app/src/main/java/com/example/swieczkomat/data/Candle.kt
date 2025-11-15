package com.example.swieczkomat.data

import org.json.JSONObject

// Daty przechowywane jako String (format yyyy-MM-dd) dla kompatybilności z API < 26

data class Candle(
    val id: Long,
    val createdDate: String, // yyyy-MM-dd
    val burnTimeMinutes: Int = 0,
    val recipient: String = "",
    val fireDate: String, // yyyy-MM-dd (createdDate + 14 dni)
    val count: Int,
    val containerName: String?,
    val waxName: String?,
    val fragranceName: String?,
    val wickName: String?,
    val dyeName: String?,
    val concentration: Double,
    val totalPrice: Double,
    val waxAmount: Double,
    val fragranceAmount: Double
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("createdDate", createdDate)
        put("burnTimeMinutes", burnTimeMinutes)
        put("recipient", recipient)
        put("fireDate", fireDate)
        put("count", count)
        put("containerName", containerName ?: JSONObject.NULL)
        put("waxName", waxName ?: JSONObject.NULL)
        put("fragranceName", fragranceName ?: JSONObject.NULL)
        put("wickName", wickName ?: JSONObject.NULL)
        put("dyeName", dyeName ?: JSONObject.NULL)
        put("concentration", concentration)
        put("totalPrice", totalPrice)
        put("waxAmount", waxAmount)
        put("fragranceAmount", fragranceAmount)
    }

    companion object {
        fun fromJson(obj: JSONObject): Candle = Candle(
            id = obj.getLong("id"),
            createdDate = obj.getString("createdDate"),
            burnTimeMinutes = obj.optInt("burnTimeMinutes", 0),
            recipient = obj.optString("recipient", ""),
            fireDate = obj.getString("fireDate"),
            count = obj.optInt("count", 1),
            containerName = obj.opt("containerName").let { if (it == JSONObject.NULL) null else it as String },
            waxName = obj.opt("waxName").let { if (it == JSONObject.NULL) null else it as String },
            fragranceName = obj.opt("fragranceName").let { if (it == JSONObject.NULL) null else it as String },
            wickName = obj.opt("wickName").let { if (it == JSONObject.NULL) null else it as String },
            dyeName = obj.opt("dyeName").let { if (it == JSONObject.NULL) null else it as String },
            concentration = obj.optDouble("concentration", 0.0),
            totalPrice = obj.optDouble("totalPrice", 0.0),
            waxAmount = obj.optDouble("waxAmount", 0.0),
            fragranceAmount = obj.optDouble("fragranceAmount", 0.0)
        )
    }
}
