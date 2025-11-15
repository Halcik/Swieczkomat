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

class MaterialsViewModel(private val materialDao: MaterialDao) : ViewModel() {

    fun getAllMaterials(): Flow<List<Material>> = materialDao.getAllMaterials()

    fun addOrUpdateMaterial(material: Material) {
        viewModelScope.launch {
            val existingMaterial = materialDao.getMaterialByName(material.name)
            if (existingMaterial != null) {
                val updatedMaterial = existingMaterial.copy(
                    quantity = existingMaterial.quantity + material.quantity,
                    price = existingMaterial.price + material.price
                )
                materialDao.update(updatedMaterial)
            } else {
                materialDao.insert(material)
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
