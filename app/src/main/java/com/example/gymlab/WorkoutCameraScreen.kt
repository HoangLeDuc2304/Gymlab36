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
    imageWidth: Int = 0,
    imageHeight: Int = 0,
    analyzer: ImageAnalysis.Analyzer? = null
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Camera Preview Placeholder",
                color = Color.White
            )
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
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = analyzer?.let {
                        ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(executor, it)
                            }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        if (imageAnalysis != null) {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } else {
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        }
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Lỗi khởi tạo camera", exc)
                    }
                }, executor)

                previewView
            }
        )

        // Draw Pose Landmarks
        if (pose != null && imageWidth > 0 && imageHeight > 0) {
            PoseOverlay(pose = pose, imageWidth = imageWidth, imageHeight = imageHeight)
        }
    }
}

@Composable
fun PoseOverlay(pose: Pose, imageWidth: Int, imageHeight: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return@Canvas

        // Tính toán tỉ lệ scale để khớp với màn hình
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        // Với Front Camera, ML Kit trả về tọa độ đã lật (mirror) theo mặc định 
        // nhưng đôi khi logic vẽ cần khớp với PreviewView đã được mirror.
        
        fun getCanvasOffset(landmark: PoseLandmark): Offset {
            // ML Kit coordinates are in image space. 
            // For Front Camera, we need to flip X to match the mirrored preview
            val flippedX = imageWidth - landmark.position.x
            return Offset(flippedX * scaleX, landmark.position.y * scaleY)
        }

        // Vẽ các điểm (Landmarks)
        landmarks.forEach { landmark ->
            if (landmark.inFrameLikelihood > 0.5f) {
                drawCircle(
                    color = Color.Cyan,
                    radius = 8f,
                    center = getCanvasOffset(landmark)
                )
            }
        }

        // Vẽ các đường nối (Skeleton)
        fun drawLineBetween(startType: Int, endType: Int) {
            val start = pose.getPoseLandmark(startType)
            val end = pose.getPoseLandmark(endType)
            if (start != null && end != null && start.inFrameLikelihood > 0.5f && end.inFrameLikelihood > 0.5f) {
                drawLine(
                    color = Color.White,
                    start = getCanvasOffset(start),
                    end = getCanvasOffset(end),
                    strokeWidth = 4f
                )
            }
        }

        // Thân trên
        drawLineBetween(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER)
        drawLineBetween(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP)
        drawLineBetween(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP)
        drawLineBetween(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)

        // Tay
        drawLineBetween(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        drawLineBetween(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        drawLineBetween(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawLineBetween(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)

        // Chân
        drawLineBetween(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        drawLineBetween(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
        drawLineBetween(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        drawLineBetween(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
    }
}
