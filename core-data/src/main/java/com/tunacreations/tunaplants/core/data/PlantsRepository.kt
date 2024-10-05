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

package com.tunacreations.tunaplants.core.data

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.tunacreations.tunaplants.core.database.Plants
import com.tunacreations.tunaplants.core.database.PlantsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

interface PlantsRepository {
    val plantss: Flow<List<String>>

    suspend fun add(name: String)
    suspend fun uploadPlantData(
        plantName: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
    suspend fun syncUnsyncedPlants()
}

class DefaultPlantsRepository @Inject constructor(
    private val plantsDao: PlantsDao,
    private val storageReference: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : PlantsRepository {

    //private val storageReference = storageReference.reference
    //private val firestore = FirebaseFirestore.getInstance()

    // Function to upload plant data
    override suspend fun uploadPlantData(
        plantName: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Save to local database
            plantsDao.insertPlants(Plants(name = plantName, imageUri = imageUri.toString()))

            // Upload to Firebase Storage
            val imageFileName = "plants/${UUID.randomUUID()}.jpg"
            val imageRef = storageReference.reference.child(imageFileName)

            // Use await() to handle the Firebase task as a suspend function
            imageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = imageRef.downloadUrl.await()
            val imageUrl = downloadUrl.toString()

            // Prepare data for Firestore
            val plantData = hashMapOf("name" to plantName, "imageUrl" to imageUrl)

            // Save plant data to Firestore
            firestore.collection("plants").add(plantData).await()

            // Mark the plant as synced in the local database
            plantsDao.updatePlant(Plants(name = plantName, imageUri = imageUrl, isSynced = true))

            // Call onSuccess callback
            onSuccess()
        } catch (e: Exception) {
            // Call onFailure callback
            onFailure(e)
        }
    }

    // Function to sync unsynced plants with Firebase
    override suspend fun syncUnsyncedPlants() {
        try {
            // Get all unsynced plants
            val unsyncedPlants = plantsDao.getUnsyncedPlants()

            for (plant in unsyncedPlants) {
                try {
                    // Upload image to Firebase Storage
                    val imageFileName = "plants/${UUID.randomUUID()}.jpg"
                    val imageRef = storageReference.reference.child(imageFileName)

                    imageRef.putFile(Uri.parse(plant.imageUri)).await()

                    // Get the download URL
                    val downloadUrl = imageRef.downloadUrl.await()
                    val imageUrl = downloadUrl.toString()

                    // Prepare data for Firestore
                    val plantData = hashMapOf("name" to plant.name, "imageUrl" to imageUrl)

                    // Save plant data to Firestore
                    firestore.collection("plants").add(plantData).await()

                    // Mark the plant as synced in the local database
                    plantsDao.updatePlant(plant.copy(isSynced = true))
                } catch (e: Exception) {
                    // Log the exception for the current plant, continue to the next one
                    Log.e("PlantRepository", "Error syncing plant ${plant.name}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("PlantRepository", "Error syncing unsynced plants: ${e.message}")
            throw e  // Optionally, rethrow the exception if needed
        }
    }

    override val plantss: Flow<List<String>> =
        plantsDao.getPlants().map { items -> items.map { it.name } }

    override suspend fun add(name: String) {
        //plantsDao.insertPlants(Plants(name = name))
    }
}
