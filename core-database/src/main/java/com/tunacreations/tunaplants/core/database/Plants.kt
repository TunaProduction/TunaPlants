/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunacreations.tunaplants.core.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity
data class Plants(
    val name: String,
    val imageUri: String,
    val isSynced: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

@Dao
interface PlantsDao {
    @Query("SELECT * FROM plants ORDER BY uid DESC LIMIT 10")
    fun getPlants(): Flow<List<Plants>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(item: Plants)

    @Update
    suspend fun updatePlant(plant: Plants)
}
