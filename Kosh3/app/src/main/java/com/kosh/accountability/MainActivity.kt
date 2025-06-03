package com.kosh.accountability

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kosh.accountability.ui.theme.Kosh3Theme

//camera preview

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import com.kosh.accountability.ui.CameraPreview

//Navigation

import androidx.navigation.compose.*
import com.kosh.accountability.ui.MainScreen
import com.kosh.accountability.ui.CameraCaptureScreen
import com.kosh.accountability.ui.MatchScreen
import com.kosh.accountability.ui.NoMatchScreen
import com.kosh.accountability.ui.ImageReviewScreen



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1001)
        }
        setContent {
            Kosh3Theme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "main") {
                    composable("main") { MainScreen(navController) }
                    composable("camera/{mode}") { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "register"
                        CameraCaptureScreen(navController, mode)
                    }

                    composable("match") { MatchScreen() }
                    composable("no_match") { NoMatchScreen() }

                    composable("review/{mode}/{path}") { backStackEntry ->
                        val mode = backStackEntry.arguments?.getString("mode") ?: "register"
                        val path = backStackEntry.arguments?.getString("path") ?: ""
                        ImageReviewScreen(navController, mode, path)
                    }


                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Kosh3Theme {
        Greeting("Android")
    }
}

