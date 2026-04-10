package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.SendOtpRequest
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onCodeSentSuccess: (String) -> Unit,
    onLoginNowClick: () -> Unit
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF5F5F5), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Quên mật khẩu? 🔒",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Đừng lo lắng! Hãy nhập email bạn đã đăng ký tài khoản. Chúng tôi sẽ gửi mã xác minh 6 số để giúp bạn đặt lại mật khẩu mới.",
            fontSize = 14.sp,
            color = TextGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Địa chỉ Email",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { 
                emailOrPhone = it 
                errorMessage = null
            },
            placeholder = { Text("Ví dụ: duc@gmail.com", color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF8F9FA),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = PrimaryPurple,
                cursorColor = PrimaryPurple
            )
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { 
                if (emailOrPhone.isEmpty()) {
                    errorMessage = "Vui lòng nhập Email"
                    return@Button
                }
                
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.sendOtp(SendOtpRequest(emailOrPhone))
                        if (response.isSuccessful) {
                            if (response.body()?.success == true) {
                                onCodeSentSuccess(emailOrPhone)
                            } else {
                                errorMessage = response.body()?.message ?: "Lỗi không xác định"
                            }
                        } else {
                            // Lấy thông báo lỗi từ errorBody (cho trường hợp 404, 400...)
                            val errorJson = response.errorBody()?.string()
                            val message = try {
                                JSONObject(errorJson).getString("message")
                            } catch (e: Exception) {
                                "Lỗi Server (${response.code()})"
                            }
                            errorMessage = message
                        }
                    } catch (e: Exception) {
                        errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Gửi mã xác nhận",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onLoginNowClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Bạn đã nhớ mật khẩu? ")
                    withStyle(style = SpanStyle(color = PrimaryPurple, fontWeight = FontWeight.Bold)) {
                        append("Đăng nhập ngay")
                    }
                },
                fontSize = 14.sp,
                color = TextGray
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
