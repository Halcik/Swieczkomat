package com.example.swieczkomat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Insert
    suspend fun insert(material: Material)

    @Update
    suspend fun update(material: Material)

    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE name = :name")
    suspend fun getMaterialByName(name: String): Material?
}
