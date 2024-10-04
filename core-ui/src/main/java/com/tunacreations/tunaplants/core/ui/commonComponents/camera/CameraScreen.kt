package com.tunacreations.tunaplants.core.ui.commonComponents.camera

import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import java.io.File

@Composable
fun CameraScreen(navController: NavHostController, onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create a PreviewView instance
    val previewView = remember { PreviewView(context) }

    val cameraProvider = remember {
        CameraProvider.Builder(context, lifecycleOwner)
            .setPreview(Preview.Builder().build()) // Optional: Customize the preview if needed
            .setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA) // Optional: Customize the selector if needed
            .setImageCapture(ImageCapture.Builder().build()) // Add ImageCapture to the builder
            .build()
    }

    Column {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.8f)
        ) { view ->
            cameraProvider.preview.setSurfaceProvider(view.surfaceProvider)
        }

        LaunchedEffect(cameraProvider) {
            try {
                cameraProvider.cameraProvider.unbindAll()
                cameraProvider.cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraProvider.cameraSelector,
                    cameraProvider.preview,
                    cameraProvider.imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Button(
            onClick = {
                val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                cameraProvider.imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                            Toast.makeText(context, "Photo saved: $savedUri", Toast.LENGTH_SHORT).show()
                            onImageCaptured(savedUri)
                            navController.popBackStack()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            exception.printStackTrace()
                            Toast.makeText(context, "Failed to capture photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Capture Photo")
        }
    }

}