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
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.delay

@Composable
fun WorkoutTimerScreen(
    detailId: Int,
    exerciseName: String,
    onFinish: (Int, Int) -> Unit // detailId, duration
) {
    var seconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            seconds++
        }
    }

    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val timerText = String.format("%02d:%02d", minutes, remainingSeconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = exerciseName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(PrimaryPurple.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timerText,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning) Color(0xFFF5F5F5) else PrimaryPurple
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Play",
                    tint = if (isRunning) Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Button(
                onClick = { 
                    isRunning = false
                    onFinish(detailId, seconds.toInt()) 
                },
                modifier = Modifier
                    .height(72.dp)
                    .width(160.dp),
                shape = RoundedCornerShape(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("HOÀN THÀNH", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}