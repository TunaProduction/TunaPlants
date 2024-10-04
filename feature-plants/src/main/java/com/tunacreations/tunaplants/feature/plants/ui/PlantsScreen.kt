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

package com.tunacreations.tunaplants.feature.plants.ui

import android.net.Uri
import androidx.compose.foundation.Image
import com.tunacreations.tunaplants.core.ui.MyApplicationTheme
import com.tunacreations.tunaplants.feature.plants.ui.PlantsUiState.Success
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.tunacreations.tunaplants.core.ui.R
import com.tunacreations.tunaplants.core.ui.commonComponents.camera.CameraScreen

@Composable
fun PlantsScreen(navController: NavHostController, modifier: Modifier = Modifier, viewModel: PlantsViewModel = hiltViewModel()) {
    val items by viewModel.uiState.collectAsStateWithLifecycle()
    if (items is Success) {
        PlantsScreen(
            navController = navController,
            items = (items as Success).data,
            onSave = { name -> viewModel.addPlants(name) },
            modifier = modifier
        )
    }
}

@Composable
internal fun PlantsScreen(
    navController: NavHostController,
    items: List<String>,
    onSave: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {

    val plantsName = stringResource(id = R.string.plants_name)
    var namePlants by remember { mutableStateOf(plantsName) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Observe the saved state handle for the image URI
    savedStateHandle?.getLiveData<Uri>("imageUri")?.observe(LocalLifecycleOwner.current) { uri ->
        capturedImageUri = uri
    }

    LazyColumn(modifier) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = namePlants,
                    onValueChange = { namePlants = it }
                )

                Button(modifier = Modifier.width(96.dp), onClick = { onSave(namePlants) }) {
                    Text("Save")
                }
            }
        }

        item {
            capturedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
            } ?: Text("No Image Captured", modifier = Modifier.padding(16.dp))

            Button(onClick = {
                navController.navigate("camera")
            }) {
                Text("Open Camera")
            }
        }

        /*items.forEach {
            Text("Saved item: $it")
        }*/
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MyApplicationTheme {
        //PlantsScreen(listOf("Compose", "Room", "Kotlin"), onSave = {})
    }
}

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun PortraitPreview() {
    MyApplicationTheme {
       // PlantsScreen(listOf("Compose", "Room", "Kotlin"), onSave = {})
    }
}
