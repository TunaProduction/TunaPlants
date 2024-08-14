package com.tunacreations.tunaplants.core.ui.commonComponents.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner

class CameraProvider private constructor(
    val cameraProvider: ProcessCameraProvider,
    val preview: Preview,
    val cameraSelector: CameraSelector,
    val imageCapture: ImageCapture,
    val lifecycleOwner: LifecycleOwner
) {
    data class Builder(
        private val context: Context,
        private val lifecycleOwner: LifecycleOwner
    ) {
        private var preview: Preview = Preview.Builder().build()
        private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        private var imageCapture: ImageCapture = ImageCapture.Builder().build()

        fun setPreview(preview: Preview) = apply { this.preview = preview }
        fun setCameraSelector(cameraSelector: CameraSelector) = apply { this.cameraSelector = cameraSelector }
        fun setImageCapture(imageCapture: ImageCapture) = apply { this.imageCapture = imageCapture }

        fun build(): CameraProvider {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val cameraProvider = cameraProviderFuture.get()

            return CameraProvider(cameraProvider, preview, cameraSelector, imageCapture, lifecycleOwner)
        }
    }
}