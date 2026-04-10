package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.ResetPasswordRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    email: String,
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
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
            text = "Tạo mật khẩu mới 🔑",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Mã xác nhận gồm 6 chữ số đã được gửi đến email $email. Hãy nhập mã đó và thiết lập mật khẩu mới.",
            fontSize = 14.sp,
            color = TextGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // OTP Field
        Text(
            text = "Mã xác nhận (OTP)",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = otpCode,
            onValueChange = { 
                if (it.length <= 6) otpCode = it 
                errorMessage = null
            },
            placeholder = { Text("Nhập 6 số...", color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
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

        Spacer(modifier = Modifier.height(20.dp))

        // New Password Field
        Text(
            text = "Mật khẩu mới",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = newPassword,
            onValueChange = { 
                newPassword = it 
                errorMessage = null
            },
            placeholder = { Text("Tối thiểu 8 ký tự", color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

        Spacer(modifier = Modifier.height(20.dp))

        // Confirm Password Field
        Text(
            text = "Nhập lại mật khẩu mới",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                errorMessage = null
            },
            placeholder = { Text("Nhập lại mật khẩu ở trên", color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            },
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                if (otpCode.length < 6) {
                    errorMessage = "Vui lòng nhập đủ 6 số OTP"
                    return@Button
                }
                if (newPassword.length < 8) {
                    errorMessage = "Mật khẩu mới phải từ 8 ký tự"
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    errorMessage = "Mật khẩu nhập lại không khớp"
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.resetPassword(
                            ResetPasswordRequest(email, otpCode, newPassword)
                        )
                        if (response.isSuccessful && response.body()?.success == true) {
                            onResetSuccess() // Quay về màn hình Login
                        } else {
                            errorMessage = response.body()?.message ?: "OTP không đúng hoặc đã hết hạn"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Lỗi kết nối server"
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
                    text = "Cập nhật mật khẩu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}