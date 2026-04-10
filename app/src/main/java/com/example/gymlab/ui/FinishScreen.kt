//package com.example.gymlab.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.gymlab.api.RetrofitClient
//import com.example.gymlab.api.WorkoutResultRequest
//import com.example.gymlab.ui.theme.PrimaryPurple
//import kotlinx.coroutines.launch
//
//@Composable
//fun FinishScreen(
//    detailId: Int,
//    duration: Int,
//    onContinueClick: () -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var calories by remember { mutableIntStateOf(0) }
//    var exp by remember { mutableIntStateOf(0) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            try {
//                val response = RetrofitClient.instance.completeExercise(
//                    WorkoutResultRequest(detailId, duration)
//                )
//                if (response.isSuccessful && response.body()?.success == true) {
//                    calories = response.body()?.calories ?: 0
//                    exp = response.body()?.exp ?: 0
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//        Icon(
//            imageVector = Icons.Default.CheckCircle,
//            contentDescription = "Success",
//            tint = Color(0xFF4CAF50),
//            modifier = Modifier.size(100.dp)
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Text(
//            text = "Tuyệt vời!",
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Black
//        )
//
//        Text(
//            text = "Bạn đã hoàn thành bài tập",
//            fontSize = 16.sp,
//            color = Color.Gray
//        )
//
//        Spacer(modifier = Modifier.height(48.dp))
//
//        if (isLoading) {
//            CircularProgressIndicator(color = PrimaryPurple)
//        } else {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly
//            ) {
//                StatItem(label = "Calo", value = "$calories", color = Color(0xFFFF9800))
//                StatItem(label = "Kinh nghiệm", value = "+$exp", color = Color(0xFF2196F3))
//            }
//        }
//
//        Spacer(modifier = Modifier.height(64.dp))
//
//        Button(
//            onClick = onContinueClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
//        ) {
//            Text("TIẾP TỤC", fontWeight = FontWeight.Bold, fontSize = 16.sp)
//        }
//    }
//}
//
//@Composable
//fun StatItem(label: String, value: String, color: Color) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(
//            text = value,
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Bold,
//            color = color
//        )
//        Text(
//            text = label,
//            fontSize = 14.sp,
//            color = Color.Gray
//        )
//    }
//}
package com.example.gymlab.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.util.Log


@Composable
fun FinishScreen(
    detailId: Int,
    duration: Int,
    mode: String = "normal", // Thêm mode mặc định để không bị lỗi thiếu parameter
    onContinueClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Quản lý trạng thái UI
    var selectedFeedback by remember { mutableStateOf<Int?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    // Quản lý dữ liệu trả về từ server
    var calories by remember { mutableIntStateOf(0) }
    var exp by remember { mutableIntStateOf(0) }

    // Hàm gọi API gửi kết quả
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

                // IN RA DỮ LIỆU CHUẨN BỊ GỬI
                Log.d("GymlabDebug", "Đang gửi Request: $request")

                val response = RetrofitClient.instance.completeExercise(request)

                // IN RA KẾT QUẢ TỪ SERVER TRẢ VỀ
                Log.d("GymlabDebug", "Server trả về Code: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    calories = response.body()?.calories ?: 0
                    exp = response.body()?.exp ?: 0
                    isSuccess = true
                    Log.d("GymlabDebug", "Thành công! Calo: $calories, Exp: $exp")
                } else {
                    // NẾU SERVER BÁO LỖI 400, 500, IN CHI TIẾT LỖI RA
                    val errorBody = response.errorBody()?.string()
                    Log.e("GymlabDebug", "API Lỗi! Nội dung: $errorBody")
                    android.widget.Toast.makeText(context, "Lỗi server! Check Logcat", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // NẾU APP KHÔNG GỌI ĐƯỢC API (Ví dụ: rớt mạng, sai địa chỉ IP)
                Log.e("GymlabDebug", "App bị Crash/Không gọi được mạng: ${e.message}")
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
            // ==========================================
            // BƯỚC 1: MÀN HÌNH CHỌN MỨC ĐỘ ĐÁNH GIÁ
            // ==========================================
            Text(
                text = "Tuyệt vời! Bạn cảm thấy\nbài tập này thế nào?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Nút: Dưới sức
            FeedbackButton(
                text = "Dễ (Dưới sức)",
                isSelected = selectedFeedback == -1,
                onClick = { selectedFeedback = -1 }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nút: Vừa sức
            FeedbackButton(
                text = "Bình thường (Vừa sức)",
                isSelected = selectedFeedback == 0,
                onClick = { selectedFeedback = 0 }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Nút: Quá sức
            FeedbackButton(
                text = "Khó (Quá sức)",
                isSelected = selectedFeedback == 1,
                onClick = { selectedFeedback = 1 }
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { submitWorkout() },
                enabled = selectedFeedback != null && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("HOÀN THÀNH", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }

        } else {
            // ==========================================
            // BƯỚC 2: MÀN HÌNH THÔNG BÁO THÀNH CÔNG
            // ==========================================
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Hoàn tất!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Dữ liệu tập luyện đã được lưu lại",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "Calo (điểm" +
                        " nhận)", value = "$calories", color = Color(0xFFFF9800))
                StatItem(label = "Kinh nghiệm", value = "+$exp", color = Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("TIẾP TỤC", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// Component vẽ nút bấm chọn trạng thái để tái sử dụng
@Composable
fun FeedbackButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) PrimaryPurple else Color.Gray
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryPurple else Color.LightGray
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}