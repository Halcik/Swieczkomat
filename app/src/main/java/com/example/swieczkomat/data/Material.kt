package com.example.swieczkomat.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val price: Double,
    val category: String,
    val preferredWickName: String? = null,
    val preferredConcentration: Double? = null
)
