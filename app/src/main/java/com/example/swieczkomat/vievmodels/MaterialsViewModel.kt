package com.example.swieczkomat.vievmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.swieczkomat.data.AppDatabase
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.data.MaterialDao
import com.example.swieczkomat.data.MaterialType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.round

class MaterialsViewModel(private val materialDao: MaterialDao) : ViewModel() {

    fun getAllMaterials(): Flow<List<Material>> = materialDao.getAllMaterials()

    fun addOrUpdateMaterial(material: Material) {
        viewModelScope.launch {
            val existingMaterial = materialDao.getMaterialByName(material.name)
            if (existingMaterial != null) {
                val updatedMaterial = when {
                    existingMaterial.materialType is MaterialType.Wick && material.materialType is MaterialType.Wick -> {
                        val newLength = existingMaterial.materialType.lengthInMeters + material.materialType.lengthInMeters
                        existingMaterial.copy(
                            price = existingMaterial.price + material.price,
                            materialType = MaterialType.Wick(newLength)
                        )
                    }
                    existingMaterial.materialType is MaterialType.Other && material.materialType is MaterialType.Other -> {
                        val newQuantity = existingMaterial.materialType.quantity + material.materialType.quantity
                        existingMaterial.copy(
                            price = existingMaterial.price + material.price,
                            materialType = MaterialType.Other(newQuantity, existingMaterial.materialType.unit)
                        )
                    }
                    else -> {
                        // Different types with the same name, replace the old with the new
                        material.copy(id = existingMaterial.id)
                    }
                }
                materialDao.update(updatedMaterial)
            } else {
                // If it's a new material, just insert it.
                materialDao.insert(material)
            }
        }
    }

    fun removeMaterial(material: Material, amountToRemove: Double) {
        viewModelScope.launch {
            when (val type = material.materialType) {
                is MaterialType.Wick -> {
                    if (amountToRemove >= type.lengthInMeters) {
                        materialDao.delete(material)
                    } else {
                        val pricePerMeter = material.price / type.lengthInMeters
                        val newLength = type.lengthInMeters - amountToRemove
                        val newPrice = round(newLength * pricePerMeter * 100) / 100.0
                        val updatedMaterial = material.copy(
                            price = newPrice,
                            materialType = MaterialType.Wick(newLength)
                        )
                        materialDao.update(updatedMaterial)
                    }
                }
                is MaterialType.Other -> {
                    if (amountToRemove >= type.quantity) {
                        materialDao.delete(material)
                    } else {
                        val pricePerUnit = material.price / type.quantity
                        val newQuantity = type.quantity - amountToRemove
                        val newPrice = round(newQuantity * pricePerUnit * 100) / 100.0
                        val updatedMaterial = material.copy(
                            price = newPrice,
                            materialType = MaterialType.Other(newQuantity, type.unit)
                        )
                        materialDao.update(updatedMaterial)
                    }
                }
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
