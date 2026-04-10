package com.example.gymlab.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.ui.theme.PrimaryPurple

@Composable
fun AccountSettingsScreen(
    userName: String,
    onBackClick: () -> Unit,
    onChangeNameClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
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
                    subtitle = "Tên hiển thị hiện tại: $userName",
                    onClick = onChangeNameClick
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
