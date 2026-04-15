package com.example.gymlab.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.PoseAnalyzer
import com.example.gymlab.R
import com.example.gymlab.WorkoutCameraScreen
import com.example.gymlab.ui.theme.PrimaryPurple
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.pose.Pose
import kotlinx.coroutines.delay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WorkoutTimerScreen(
    detailId: Int,
    exerciseName: String,
    mode: String = "normal",
    onFinish: (Int, Int) -> Unit // detailId, duration
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    DisposableEffect(exerciseName) {
        val activity = context as? Activity
        if (exerciseName.lowercase().contains("plank")) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var seconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    
    // AI Mode specific state
    var isPoseDetected by remember { mutableStateOf(false) }
    var reps by remember { mutableIntStateOf(0) }
    var currentPose by remember { mutableStateOf<Pose?>(null) }
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    // logic đếm giờ
    LaunchedEffect(isRunning, isPoseDetected, mode) {
        while (isRunning) {
            if (mode == "ai") {
                if (isPoseDetected) {
                    delay(1000)
                    seconds++
                } else {
                    delay(500)
                }
            } else {
                delay(1000)
                seconds++
            }
        }
    }

    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val timerText = String.format("%02d:%02d", minutes, remainingSeconds)
    val isLunge = exerciseName.lowercase().contains("lunge")
    val isPlank = exerciseName.lowercase().contains("plank")

    Box(modifier = Modifier.fillMaxSize()) {
        if (mode == "ai") {
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
            
            if (cameraPermissionState.status.isGranted) {
                WorkoutCameraScreen(
                    modifier = Modifier.fillMaxSize(),
                    pose = currentPose,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    analyzer = remember {
                        PoseAnalyzer(
                            exerciseName = exerciseName,
                            onPoseDetected = { detected -> isPoseDetected = detected },
                            onRepCounted = { count -> reps = count },
                            onPoseUpdated = { updatedPose, width, height -> 
                                currentPose = updatedPose
                                imageWidth = width
                                imageHeight = height
                            }
                        )
                    }
                )
                
                // Overlay AI feedback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isLandscape) 16.dp else 24.dp),
                    contentAlignment = if (isLandscape) Alignment.TopEnd else Alignment.TopCenter
                ) {
                    Surface(
                        color = if (isPoseDetected) Color.Green.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isPoseDetected) "ĐANG ĐẾM GIỜ..." else "CHƯA NHẬN DIỆN TƯ THẾ",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isLandscape) 14.sp else 16.sp
                        )
                    }
                }
                
                // Tư thế mẫu - Chỉ hiện ảnh, bỏ khung và chữ "MẪU"
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = if (isLandscape) Alignment.TopStart else Alignment.TopEnd
                ) {
                    val imageRes = if (isLunge) R.drawable.img_lunge else if (isPlank) R.drawable.img_plank else null
                    
                    if (imageRes != null) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Sample Pose",
                            modifier = Modifier
                                .size(if (isLandscape) 100.dp else 120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow, 
                            contentDescription = null, 
                            tint = Color.White.copy(alpha = 0.5f), 
                            modifier = Modifier.size(if (isLandscape) 30.dp else 40.dp)
                        )
                    }
                }

            } else {
                Column(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cần quyền Camera để tập với AI", color = Color.White)
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Cấp quyền")
                    }
                }
            }
        }

        // Main Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (mode == "ai") Color.Transparent else Color.White)
                .padding(if (isLandscape) 16.dp else 24.dp),
            horizontalAlignment = if (isLandscape) Alignment.End else Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.Bottom
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(24.dp),
                modifier = if (isLandscape) Modifier.width(280.dp) else Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(if (isLandscape) 16.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = exerciseName + if (mode == "ai") " (AI MODE)" else "",
                        fontSize = if (isLandscape) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

                    Text(
                        text = if (isLunge) "$reps REPS" else timerText,
                        fontSize = if (isLandscape) 40.sp else 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (mode == "ai" && !isPoseDetected) Color.Gray else PrimaryPurple
                    )
                    
                    if (isLunge) {
                        Text(text = "Thời gian: $timerText", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier.size(if (isLandscape) 48.dp else 56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isRunning) Color(0xFFF5F5F5) else PrimaryPurple
                            )
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isRunning) Color.Black else Color.White
                            )
                        }

                        Button(
                            onClick = {
                                isRunning = false
                                onFinish(detailId, seconds.toInt())
                            },
                            modifier = Modifier
                                .height(if (isLandscape) 48.dp else 56.dp)
                                .weight(1f)
                                .padding(start = 12.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("HOÀN THÀNH", fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 12.sp else 14.sp)
                        }
                    }
                }
            }
        }
    }
}
