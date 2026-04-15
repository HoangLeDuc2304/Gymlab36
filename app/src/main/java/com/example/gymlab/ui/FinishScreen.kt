package com.example.gymlab.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.WorkoutResultRequest
import com.example.gymlab.api.WorkoutSuggestion
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.util.Log


@Composable
fun FinishScreen(
    detailId: Int,
    duration: Int,
    mode: String = "normal",
    onContinueClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedFeedback by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    var calories by remember { mutableIntStateOf(0) }
    var exp by remember { mutableIntStateOf(0) }
    var suggestions by remember { mutableStateOf<List<WorkoutSuggestion>>(emptyList()) }

    fun submitWorkout() {
        if (selectedFeedback == null) return
        isSubmitting = true

        scope.launch {
            try {
                val request = WorkoutResultRequest(
                    detailId = detailId,
                    duration = duration,
                    mode = mode,
                    effortFeedback = selectedFeedback!!
                )

                val response = RetrofitClient.instance.completeExercise(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    calories = response.body()?.calories ?: 0
                    exp = response.body()?.exp ?: 0
                    suggestions = response.body()?.suggestions ?: emptyList()
                    isSuccess = true
                } else {
                    Log.e("GymlabDebug", "API Lỗi")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isSuccess) {
            Text(
                text = "Tuyệt vời! Bạn cảm thấy\nbài tập này thế nào?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            FeedbackButton("Dễ (Dưới sức)", selectedFeedback == -1) { selectedFeedback = -1 }
            Spacer(modifier = Modifier.height(16.dp))
            FeedbackButton("Bình thường (Vừa sức)", selectedFeedback == 0) { selectedFeedback = 0 }
            Spacer(modifier = Modifier.height(16.dp))
            FeedbackButton("Khó (Quá sức)", selectedFeedback == 1) { selectedFeedback = 1 }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { submitWorkout() },
                enabled = selectedFeedback != null && !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("HOÀN THÀNH", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

        } else {
            // HIỂN THỊ KẾT QUẢ VÀ GỢI Ý
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hoàn tất!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Dữ liệu đã được lưu lại", color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StatItem(label = "Calo", value = "$calories", color = Color(0xFFFF9800))
                        StatItem(label = "Kinh nghiệm", value = "+$exp", color = Color(0xFF2196F3))
                    }
                }

                if (suggestions.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            "GỢI Ý TỪ AI DÀNH CHO BẠN:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryPurple,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    items(suggestions) { exercise ->
                        SuggestionCard(exercise)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onContinueClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("TIẾP TỤC", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionCard(exercise: WorkoutSuggestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${exercise.calories} Calo • ${exercise.difficulty ?: "Vừa"}", fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD600), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun FeedbackButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) PrimaryPurple else Color.Gray
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PrimaryPurple else Color.LightGray)
    ) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}