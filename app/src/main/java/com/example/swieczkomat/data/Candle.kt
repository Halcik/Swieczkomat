package com.example.swieczkomat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "candles")
data class Candle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val containerName: String?,
    val waxName: String?,
    val fragranceName: String?,
    val wickName: String?,
    val dyeName: String?,
    val concentration: Double,
    val capacity: Double,
    val cost: Double,
    val forWhom: String,
    val dateMade: Long,
    val dateToLight: Long,
    val burnTimeMinutes: Int = 0
)
