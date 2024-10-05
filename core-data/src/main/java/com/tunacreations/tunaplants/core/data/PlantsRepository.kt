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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.tunacreations.tunaplants.core.database.Plants
import com.tunacreations.tunaplants.core.database.PlantsDao
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
}

class DefaultPlantsRepository @Inject constructor(
    private val plantsDao: PlantsDao
) : PlantsRepository {

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    // Function to upload plant data
    suspend fun uploadPlantData(
        plantName: String,
        imageUri: Uri,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Save to local database first
            plantsDao.insertPlants(Plants(name = plantName, imageUri = imageUri.toString()))

            // Try uploading to Firebase
            val imageFileName = "plants/${UUID.randomUUID()}.jpg"
            val imageRef = storageReference.child(imageFileName)

            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val plantData = hashMapOf("name" to plantName, "imageUrl" to imageUrl)

                        firestore.collection("plants")
                            .add(plantData)
                            .addOnSuccessListener {
                                // Update local database to mark this plant as synced
                                plantsDao.updatePlant(Plants(name = plantName, imageUri = imageUrl, isSynced = true))
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                onFailure(exception)
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override val plantss: Flow<List<String>> =
        plantsDao.getPlants().map { items -> items.map { it.name } }

    override suspend fun add(name: String) {
        //plantsDao.insertPlants(Plants(name = name))
    }
}
