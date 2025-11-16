package com.example.swieczkomat.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CandleDao {
    @Insert
    suspend fun insert(candle: Candle)

    @Update
    suspend fun update(candle: Candle)

    @Query("SELECT * FROM candles ORDER BY dateMade DESC")
    fun getAllCandles(): Flow<List<Candle>>

    @Query("UPDATE candles SET burnTimeMinutes = :minutes WHERE id = :id")
    suspend fun updateBurnTime(id: Int, minutes: Int)
}
