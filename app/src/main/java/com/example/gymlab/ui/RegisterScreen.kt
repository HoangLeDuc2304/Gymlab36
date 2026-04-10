package com.example.gymlab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.RegisterRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch
import com.example.gymlab.api.UserData

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: (UserData) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tạo tài khoản mới",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Full Name Field
        Text(text = "Họ và Tên", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text("Ví dụ: Hoàng Lê Đức", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        Text(text = "Email", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Nhập email...", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        Text(text = "Mật khẩu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Tạo mật khẩu...", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        Text(text = "Nhập lại Mật khẩu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Xác nhận mật khẩu...", color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

//        Button(
//            onClick = {
//                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
//                    errorMessage = "Vui lòng điền đầy đủ thông tin!"
//                    return@Button
//                }
//                if (password != confirmPassword) {
//                    errorMessage = "Mật khẩu nhập lại không khớp!"
//                    return@Button
//                }
//
//                isLoading = true
//                scope.launch {
//                    try {
//                        val request = RegisterRequest(
//                            username = email.split("@")[0], // Tạm lấy phần trước @ làm username
//                            email = email,
//                            password = password,
//                            fullName = fullName
//                        )
//                        val response = RetrofitClient.instance.register(request)
//                        if (response.isSuccessful && response.body()?.success == true) {
//                            onRegisterSuccess(fullName)
//                        } else {
//                            errorMessage = response.body()?.message ?: "Đăng ký thất bại!"
//                        }
//                    } catch (e: Exception) {
//                        errorMessage = "Lỗi kết nối: ${e.message}"
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth().height(56.dp),
//            enabled = !isLoading,
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
//            } else {
//                Text(text = "Đăng Ký Ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//            }
//        }
        Button(
            onClick = {
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Vui lòng điền đầy đủ thông tin!"
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Mật khẩu nhập lại không khớp!"
                    return@Button
                }

                isLoading = true
                scope.launch {
                    try {
                        val request = RegisterRequest(
                            username = email.split("@")[0],
                            email = email,
                            password = password,
                            fullName = fullName
                        )
                        val response = RetrofitClient.instance.register(request)

                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body?.success == true && body.user != null) {
                                // TRUYỀN ĐỐI TƯỢNG USER VỀ MAIN ACTIVITY
                                onRegisterSuccess(body.user)
                            } else {
                                errorMessage = body?.message ?: "Đăng ký thất bại!"
                            }
                        } else {
                            errorMessage = "Email đã tồn tại hoặc lỗi server!"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Lỗi kết nối: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Đăng Ký Ngay", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

