package com.example.swieczkomat.vievmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.swieczkomat.data.AppDatabase
import com.example.swieczkomat.data.Candle
import com.example.swieczkomat.data.CandleDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CandleViewModel(private val candleDao: CandleDao) : ViewModel() {
    fun getAllCandles(): Flow<List<Candle>> = candleDao.getAllCandles()

    fun addCandles(candles: List<Candle>) {
        viewModelScope.launch {
            candles.forEach { candleDao.insert(it) }
        }
    }

    fun updateCandle(candle: Candle) {
        viewModelScope.launch { candleDao.update(candle) }
    }

    fun deleteCandle(id: Int) {
        viewModelScope.launch { candleDao.deleteById(id) }
    }

    fun addBurnTime(id: Int, minutesToAdd: Int) {
        viewModelScope.launch { candleDao.incrementBurnTime(id, minutesToAdd) }
    }

    fun setBurnTime(id: Int, minutes: Int) {
        viewModelScope.launch { candleDao.updateBurnTime(id, minutes) }
    }

    suspend fun getById(id: Int): Candle? = candleDao.getById(id)
}

class CandleViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CandleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CandleViewModel(AppDatabase.getDatabase(application).candleDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
