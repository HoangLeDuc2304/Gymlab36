//package com.example.gymlab.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.withStyle
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.gymlab.api.LoginRequest
//import com.example.gymlab.api.RetrofitClient
//import com.example.gymlab.ui.theme.PrimaryPurple
//import com.example.gymlab.ui.theme.TextGray
//import kotlinx.coroutines.launch
//
//@Composable
//fun LoginScreen(
//    onLoginSuccess: (String) -> Unit,
//    onRegisterClick: () -> Unit,
//    onForgotPasswordClick: () -> Unit
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    val scope = rememberCoroutineScope()
//
//    // Danh sách tài khoản giả lập kèm theo tên hiển thị
//    val mockUsers = mapOf(
//        "admin@gmail.com" to ("123456" to "Quản trị viên"),
//        "user@gmail.com" to ("password123" to "Người dùng"),
//        "gymlab@gmail.com" to ("654321" to "Gymlab Member"),
//        "hoangducpro121" to ("123456" to "Hoàng Lê Đức")
//    )
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.height(60.dp))
//
//        Text(
//            text = "Gymlab",
//            fontSize = 32.sp,
//            fontWeight = FontWeight.Bold,
//            color = PrimaryPurple
//        )
//
//        Text(
//            text = "Sẵn sàng cho phiên bản tốt nhất của bạn!",
//            fontSize = 14.sp,
//            color = TextGray,
//            modifier = Modifier.padding(top = 8.dp)
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        Column(modifier = Modifier.fillMaxWidth()) {
//            Text(
//                text = "Email hoặc Số điện thoại",
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 14.sp,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            OutlinedTextField(
//                value = email,
//                onValueChange = {
//                    email = it
//                    errorMessage = null
//                },
//                placeholder = { Text("Nhập email của bạn...", color = Color.LightGray) },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isLoading,
//                shape = RoundedCornerShape(12.dp),
//                isError = errorMessage != null,
//                colors = OutlinedTextFieldDefaults.colors(
//                    unfocusedBorderColor = Color(0xFFEEEEEE),
//                    focusedBorderColor = PrimaryPurple,
//                    cursorColor = PrimaryPurple
//                )
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text(
//                text = "Mật khẩu",
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 14.sp,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            OutlinedTextField(
//                value = password,
//                onValueChange = {
//                    password = it
//                    errorMessage = null
//                },
//                placeholder = { Text("Nhập mật khẩu...", color = Color.LightGray) },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isLoading,
//                visualTransformation = PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                shape = RoundedCornerShape(12.dp),
//                isError = errorMessage != null,
//                colors = OutlinedTextFieldDefaults.colors(
//                    unfocusedBorderColor = Color(0xFFEEEEEE),
//                    focusedBorderColor = PrimaryPurple,
//                    cursorColor = PrimaryPurple
//                )
//            )
//
//            if (errorMessage != null) {
//                Text(
//                    text = errorMessage!!,
//                    color = Color.Red,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(top = 8.dp)
//                )
//            }
//
//            TextButton(
//                onClick = onForgotPasswordClick,
//                enabled = !isLoading,
//                modifier = Modifier.align(Alignment.End)
//            ) {
//                Text(
//                    text = "Quên mật khẩu?",
//                    color = PrimaryPurple,
//                    fontSize = 12.sp
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        Button(
//            onClick = {
//                if (email.isEmpty() || password.isEmpty()) {
//                    errorMessage = "Vui lòng nhập đầy đủ thông tin!"
//                    return@Button
//                }
//
//                // 1. Kiểm tra nhanh với danh sách giả lập
//                val mockUser = mockUsers[email]
//                if (mockUser != null && mockUser.first == password) {
//                    onLoginSuccess(mockUser.second)
//                    return@Button
//                }
//
//                // 2. Gọi API thật
//                isLoading = true
//                scope.launch {
//                    try {
//                        val response = RetrofitClient.instance.login(LoginRequest(email, password))
//                        if (response.isSuccessful) {
//                            val body = response.body()
//                            if (body?.success == true) {
//                                onLoginSuccess(body.user?.fullName ?: "Người dùng")
//                            } else {
//                                errorMessage = body?.message ?: "Đăng nhập thất bại!"
//                            }
//                        } else {
//                            errorMessage = "Thông tin tài khoản hoặc mật khẩu không đúng"
//                        }
//                    } catch (e: Exception) {
//                        errorMessage = "Lỗi kết nối hoặc tài khoản không tồn tại!"
//                    } finally {
//                        isLoading = false
//                    }
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            enabled = !isLoading,
//            shape = RoundedCornerShape(12.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
//            } else {
//                Text(
//                    text = "Đăng Nhập",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        TextButton(onClick = onRegisterClick, enabled = !isLoading) {
//            Text(
//                text = buildAnnotatedString {
//                    append("Chưa có tài khoản? ")
//                    withStyle(style = SpanStyle(color = PrimaryPurple, fontWeight = FontWeight.Bold)) {
//                        append("Đăng ký ngay")
//                    }
//                },
//                fontSize = 14.sp,
//                color = Color.Black
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//    }
//}
package com.example.gymlab.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.LoginRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.UserData // Thêm import này
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    // 1. Thay đổi từ (String) sang (UserData) để truyền ID và Username đi tiếp
    onLoginSuccess: (UserData) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Cập nhật danh sách giả lập để bao gồm userId
    val mockUsers = mapOf(
        "admin@gmail.com" to UserData(userId = 1, username = "admin", fullName = "Quản trị viên"),
        "user@gmail.com" to UserData(userId = 2, username = "user", fullName = "Người dùng"),
        "hoangducpro121" to UserData(userId = 3, username = "hoangduc", fullName = "Hoàng Lê Đức")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "Gymlab",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple
        )

        Text(
            text = "Sẵn sàng cho phiên bản tốt nhất của bạn!",
            fontSize = 14.sp,
            color = TextGray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Email hoặc Số điện thoại",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = { Text("Nhập email của bạn...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedBorderColor = PrimaryPurple,
                    cursorColor = PrimaryPurple
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mật khẩu",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text("Nhập mật khẩu...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFEEEEEE),
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

            TextButton(
                onClick = onForgotPasswordClick,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Quên mật khẩu?",
                    color = PrimaryPurple,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Vui lòng nhập đầy đủ thông tin!"
                    return@Button
                }

                // 2. Kiểm tra nhanh với danh sách giả lập
                if (mockUsers.containsKey(email) && password == "123456") {
                    onLoginSuccess(mockUsers[email]!!)
                    return@Button
                }

                // 3. Gọi API thật
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.login(LoginRequest(email, password))
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body?.success == true && body.user != null) {
                                // Truyền toàn bộ object user sang MainActivity
                                onLoginSuccess(body.user)
                            } else {
                                errorMessage = body?.message ?: "Đăng nhập thất bại!"
                            }
                        } else {
                            errorMessage = "Thông tin tài khoản hoặc mật khẩu không đúng"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Lỗi kết nối hoặc tài khoản không tồn tại!"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Đăng Nhập",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onRegisterClick, enabled = !isLoading) {
            Text(
                text = buildAnnotatedString {
                    append("Chưa có tài khoản? ")
                    withStyle(style = SpanStyle(color = PrimaryPurple, fontWeight = FontWeight.Bold)) {
                        append("Đăng ký ngay")
                    }
                },
                fontSize = 14.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}