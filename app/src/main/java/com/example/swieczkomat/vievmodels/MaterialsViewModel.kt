package com.example.swieczkomat.vievmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.swieczkomat.data.AppDatabase
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.data.MaterialDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.round

class MaterialsViewModel(private val materialDao: MaterialDao) : ViewModel() {

    fun getAllMaterials(): Flow<List<Material>> = materialDao.getAllMaterials()

    fun addOrUpdateMaterial(material: Material) {
        viewModelScope.launch {
            val existingMaterial = materialDao.getMaterialByName(material.name)
            if (existingMaterial != null) {
                // If material exists, just sum up the quantity and price
                val updatedMaterial = existingMaterial.copy(
                    quantity = existingMaterial.quantity + material.quantity,
                    price = existingMaterial.price + material.price // Summing total prices
                )
                materialDao.update(updatedMaterial)
            } else {
                // If it's a new material, just insert it as is.
                // The price entered by the user is the total price for the given quantity.
                materialDao.insert(material)
            }
        }
    }

    fun updateMaterial(material: Material) {
        viewModelScope.launch {
            materialDao.update(material)
        }
    }

    fun removeMaterial(material: Material, quantityToRemove: Double) {
        viewModelScope.launch {
            if (quantityToRemove >= material.quantity) {
                materialDao.delete(material)
            } else {
                val pricePerUnit = material.price / material.quantity
                val newQuantity = material.quantity - quantityToRemove
                val newPrice = round(newQuantity * pricePerUnit * 100) / 100.0
                val updatedMaterial = material.copy(quantity = newQuantity, price = newPrice)
                materialDao.update(updatedMaterial)
            }
        }
    }
}

class MaterialsViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MaterialsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MaterialsViewModel(AppDatabase.getDatabase(application).materialDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
