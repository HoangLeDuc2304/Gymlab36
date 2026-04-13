package com.example.gymlab

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

@Composable
fun WorkoutCameraScreen(
    modifier: Modifier = Modifier,
    pose: Pose? = null,
    analyzer: ImageAnalysis.Analyzer? = null
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(text = "Camera Preview Placeholder", color = Color.White)
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            update = { previewView ->
                val executor = ContextCompat.getMainExecutor(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                    val imageAnalysis = analyzer?.let {
                        ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetRotation(previewView.display.rotation)
                            .build()
                            .also { analysis -> analysis.setAnalyzer(executor, it) }
                    }

                    // THỬ ĐỔI SANG BACK CAMERA NẾU FRONT VẪN HIỆN VIRTUAL SCENE
                    // Bạn có thể đổi LENS_FACING_BACK thành LENS_FACING_FRONT nếu muốn thử lại
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK) 
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            *(imageAnalysis?.let { arrayOf(it) } ?: emptyArray())
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Lỗi bind camera: ${exc.message}")
                    }
                }, executor)
            }
        )

        pose?.let { detectedPose ->
            PoseOverlay(pose = detectedPose)
        }
    }
}

@Composable
fun PoseOverlay(pose: Pose) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return@Canvas
        landmarks.forEach { landmark ->
            if (landmark.inFrameLikelihood > 0.5f) {
                drawCircle(color = Color.Cyan, radius = 8f, center = Offset(landmark.position.x, landmark.position.y))
            }
        }
    }
}
