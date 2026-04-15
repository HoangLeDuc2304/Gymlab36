package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.ui.theme.PrimaryPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectModeScreen(
    exerciseName: String,
    onBackClick: () -> Unit,
    onSelectMode: (Boolean) -> Unit // true for AI, false for Normal
) {
    // Hỗ trợ AI cho cả Lunge và Plank
    val isAiSupported = exerciseName.lowercase().contains("lunge") ||
                        exerciseName.lowercase().contains("plank")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn chế độ tập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = exerciseName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            ModeButton(
                title = "CHẾ ĐỘ THƯỜNG",
                description = "Tập luyện với đồng hồ bấm giờ truyền thống",
                color = PrimaryPurple,
                onClick = { onSelectMode(false) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ModeButton(
                title = if (isAiSupported) "CHẾ ĐỘ AI" else "CHẾ ĐỘ AI (SẮP RA MẮT)",
                description = if (isAiSupported) 
                    "Sử dụng Camera để AI kiểm tra tư thế và đếm giờ/số lần"
                    else "Hiện tại chỉ hỗ trợ bài tập Lunges và Plank. Các bài tập khác đang được cập nhật.",
                color = if (isAiSupported) Color(0xFF00C853) else Color.LightGray,
                enabled = isAiSupported,
                onClick = { onSelectMode(true) }
            )
        }
    }
}

@Composable
fun ModeButton(
    title: String,
    description: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = Color(0xFFE0E0E0)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (enabled) Color.White else Color.Gray
            )
            Text(
                text = description,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (enabled) Color.White.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}
