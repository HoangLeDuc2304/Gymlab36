package com.example.gymlab.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.UpdateNameRequest
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@Composable
fun AccountSettingsScreen(
    userId: Int,
    userName: String,
    onBackClick: () -> Unit,
    onNameUpdated: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(userName) }
    var currentName by remember { mutableStateOf(userName) }
    var isUpdating by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, shape = CircleShape)
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
                text = "Cài đặt tài khoản",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Group 1: Change Name & Password
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Badge,
                    iconContainerColor = Color(0xFFE7F0FF),
                    iconTintColor = Color(0xFF4081FF),
                    title = "Đổi tên hiển thị",
                    subtitle = "Tên hiện tại: $currentName",
                    onClick = { 
                        newName = currentName
                        showNameDialog = true 
                    }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = Color(0xFFF5F5F5)
                )
                SettingsItem(
                    icon = Icons.Default.Security,
                    iconContainerColor = Color(0xFFF3E5F5),
                    iconTintColor = Color(0xFF9C27B0),
                    title = "Đổi mật khẩu",
                    subtitle = "Bảo mật tài khoản của bạn",
                    onClick = onChangePasswordClick
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group 2: Delete Account
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.Delete,
                iconContainerColor = Color(0xFFFFEBEE),
                iconTintColor = Color(0xFFEF5350),
                title = "Xóa tài khoản vĩnh viễn",
                titleColor = Color(0xFFEF5350),
                showChevron = false,
                onClick = onDeleteAccountClick
            )
        }
    }

    // Dialog đổi tên
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showNameDialog = false },
            title = { Text("Đổi tên hiển thị", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Nhập tên mới của bạn:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newName != currentName) {
                            isUpdating = true
                            scope.launch {
                                try {
                                    val response = RetrofitClient.instance.updateName(
                                        UpdateNameRequest(userId, newName)
                                    )
                                    if (response.isSuccessful && response.body()?.success == true) {
                                        currentName = newName
                                        onNameUpdated(newName)
                                        Toast.makeText(context, "Đã đổi tên thành công!", Toast.LENGTH_SHORT).show()
                                        showNameDialog = false
                                    } else {
                                        Toast.makeText(context, "Lỗi server!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Lỗi kết nối!", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isUpdating = false
                                }
                            }
                        } else if (newName == currentName) {
                            showNameDialog = false
                        }
                    },
                    enabled = !isUpdating && newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cập nhật")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNameDialog = false }, 
                    enabled = !isUpdating
                ) {
                    Text("Hủy", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconContainerColor: Color,
    iconTintColor: Color,
    title: String,
    titleColor: Color = Color.Black,
    subtitle: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFD1D1D1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
