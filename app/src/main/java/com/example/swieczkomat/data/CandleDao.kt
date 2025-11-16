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

    @Query("SELECT * FROM candles WHERE id = :id")
    suspend fun getById(id: Int): Candle?

    @Query("DELETE FROM candles WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE candles SET burnTimeMinutes = :minutes WHERE id = :id")
    suspend fun updateBurnTime(id: Int, minutes: Int)

    @Query("UPDATE candles SET burnTimeMinutes = burnTimeMinutes + :delta WHERE id = :id")
    suspend fun incrementBurnTime(id: Int, delta: Int)
}
