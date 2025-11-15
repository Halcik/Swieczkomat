package com.example.swieczkomat.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMaterialType(materialType: MaterialType): String {
        return when (materialType) {
            is MaterialType.Wick -> "Wick,${materialType.lengthInMeters}"
            is MaterialType.Other -> "Other,${materialType.quantity},${materialType.unit}"
        }
    }

    @TypeConverter
    fun toMaterialType(value: String): MaterialType {
        val parts = value.split(",")
        return when (parts[0]) {
            "Wick" -> MaterialType.Wick(parts[1].toDouble())
            "Other" -> MaterialType.Other(parts[1].toDouble(), parts[2])
            else -> throw IllegalArgumentException("Unknown MaterialType")
        }
    }
}