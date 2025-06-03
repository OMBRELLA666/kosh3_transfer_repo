package com.kosh.accountability.ui

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

import android.content.Context
import java.io.File

import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext
import com.kosh.accountability.network.FaceRecognitionClient
import java.util.Base64
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun CameraCaptureScreen(navController: NavController, mode: String) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val executor: Executor = ContextCompat.getMainExecutor(context)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    Box(modifier = Modifier.fillMaxSize()) {

        SnackbarHost(hostState = snackbarHostState)

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val capture = ImageCapture.Builder().build()
                    imageCapture = capture

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, capture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraCaptureScreen", "Binding failed", e)
                    }

                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Capture Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        ) {
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        Log.i("DEBUG", "Starting capture...")
                        val file = createTempImageFile(context)  // 🔵 Save file for later
                        Log.i("DEBUG", "File path: ${file.absolutePath}")
                        val output = ImageCapture.OutputFileOptions.Builder(file).build()  // 🔧 Use it here



                        capture.takePicture(
                            output,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exc: ImageCaptureException) {
                                    Log.e("CameraCapture", "Capture failed: ${exc.message}", exc)
                                }

                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    Log.i("CameraCapture", "Image saved at: ${file.absolutePath}")
                                    navController.navigate("review/$mode/${file.absolutePath}")
                                }
                            }
                        )
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .size(80.dp)
            ) {}
        }
    }
}


fun createTempImageFile(context: Context): File {
    val outputDir = context.cacheDir
    return File.createTempFile("face_capture_", ".jpg", outputDir)
}

fun showSnackbar(snackbarHostState: SnackbarHostState, scope: CoroutineScope, message: String) {
    scope.launch(Dispatchers.Main) {
        snackbarHostState.showSnackbar(message)
    }
}
