package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.gymlab.api.ChangePasswordRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    email: String,
    onBackClick: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Đổi mật khẩu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Vui lòng nhập mật khẩu cũ và thiết lập mật khẩu mới cho tài khoản của bạn.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Mật khẩu hiện tại
        Text(
            text = "Mật khẩu hiện tại",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PasswordTextField(
            value = oldPassword,
            onValueChange = { oldPassword = it; errorMessage = null },
            placeholder = "Nhập mật khẩu cũ",
            isVisible = isOldPasswordVisible,
            onToggleVisibility = { isOldPasswordVisible = !isOldPasswordVisible },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mật khẩu mới
        Text(
            text = "Mật khẩu mới",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PasswordTextField(
            value = newPassword,
            onValueChange = { newPassword = it; errorMessage = null },
            placeholder = "Tối thiểu 8 ký tự",
            isVisible = isPasswordVisible,
            onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nhập lại mật khẩu mới
        Text(
            text = "Nhập lại mật khẩu mới",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            placeholder = "Nhập lại mật khẩu mới ở trên",
            isVisible = isConfirmPasswordVisible,
            onToggleVisibility = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (oldPassword.isEmpty()) {
                    errorMessage = "Vui lòng nhập mật khẩu hiện tại"
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
                        val response = RetrofitClient.instance.changePassword(
                            ChangePasswordRequest(email, oldPassword, newPassword)
                        )
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body?.success == true) {
                                onResetSuccess() 
                            } else {
                                errorMessage = body?.message ?: "Lỗi khi đổi mật khẩu"
                            }
                        } else {
                            // Xử lý khi mã lỗi HTTP không phải 2xx (ví dụ 400, 500)
                            errorMessage = "Lỗi hệ thống: ${response.code()}"
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
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Cập nhật mật khẩu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.LightGray) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = PrimaryPurple,
            cursorColor = PrimaryPurple
        )
    )
}
