package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.PoseAnalyzer
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
    var seconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    
    // AI Mode specific state
    var isPoseDetected by remember { mutableStateOf(false) }
    var reps by remember { mutableIntStateOf(0) }
    var currentPose by remember { mutableStateOf<Pose?>(null) }

    // logic đếm giờ: Nếu là AI mode, chỉ đếm khi isPoseDetected = true
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (mode == "ai") {
            val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
            
            if (cameraPermissionState.status.isGranted) {
                WorkoutCameraScreen(
                    modifier = Modifier.fillMaxSize(),
                    pose = currentPose,
                    analyzer = remember {
                        PoseAnalyzer(
                            exerciseName = exerciseName,
                            onPoseDetected = { detected -> isPoseDetected = detected },
                            onRepCounted = { count -> reps = count },
                            onPoseUpdated = { updatedPose -> currentPose = updatedPose }
                        )
                    }
                )
                
                // Overlay for AI feedback
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = if (isPoseDetected) Color.Green.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isPoseDetected) "ĐANG ĐẾM GIỜ..." else "TẠM DỪNG (CHƯA NHẬN DIỆN TƯ THẾ)",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Bộ đếm Reps - Chỉ hiển thị nếu KHÔNG PHẢI bài Plank
                        if (!exerciseName.lowercase().contains("plank")) {
                            Surface(
                                color = PrimaryPurple.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("REPS: ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("$reps", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
                
                // Ảnh tư thế mẫu ở góc (Placeholder vì không có file ảnh thật)
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp, 120.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("MẪU", color = Color.White, fontSize = 10.sp)
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            Text(if (exerciseName.lowercase().contains("plank")) "Plank" else "Lunges", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (mode == "ai") Color.Transparent else Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = exerciseName + if (mode == "ai") " (AI MODE)" else "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = timerText,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (mode == "ai" && !isPoseDetected) Color.Gray else PrimaryPurple
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { isRunning = !isRunning },
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isRunning) Color(0xFFF5F5F5) else PrimaryPurple
                            )
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "Pause" else "Play",
                                tint = if (isRunning) Color.Black else Color.White
                            )
                        }

                        Button(
                            onClick = {
                                isRunning = false
                                onFinish(detailId, seconds.toInt())
                            },
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f)
                                .padding(start = 16.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("HOÀN THÀNH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
