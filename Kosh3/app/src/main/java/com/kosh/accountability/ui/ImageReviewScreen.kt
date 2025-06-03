package com.kosh.accountability.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kosh.accountability.network.FaceRecognitionClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun ImageReviewScreen(navController: NavController, mode: String, path: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val imageFile = remember(path) { File(path) }
    val bitmap = remember(path) {
        BitmapFactory.decodeFile(imageFile.absolutePath)
    }

    val base64Image = remember(path) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
        val encoded = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
        "data:image/jpeg;base64,$encoded"
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Face",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        FaceRecognitionClient.sendImages(
                            listOf(base64Image),
                            mode,
                            object : FaceRecognitionClient.Callback {
                                override fun onSuccess(match: Boolean) {
                                    if (mode == "register") {
                                        navController.navigate("main")
                                    } else {
                                        navController.navigate(if (match) "match" else "no_match")
                                    }
                                }

                                override fun onError(error: String) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error: $error")
                                    }
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    if (mode == "register") "Confirm & Register"
                    else "Send for Validation"
                )
            }

            SnackbarHost(snackbarHostState)
        }
    }
}
