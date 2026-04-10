package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.Badge
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun AchievementsScreen(
    userId: Int, // Đã đồng bộ nhận userId từ MainActivity
    onBackClick: () -> Unit,
    onActivityClick: () -> Unit,
    onDietClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // State để quản lý huy hiệu đang được chọn để xem chi tiết
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(userId) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getAchievements(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    badges = response.body()?.badges ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = {},
                onActivityClick = onActivityClick,
                onDietClick = onDietClick,
                onScheduleClick = onScheduleClick
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        Text("Quay lại", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    }
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Danh sách thành tích", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryPurple)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(badges) { badge ->
                            AchievementCard(
                                title = badge.title,
                                description = badge.description,
                                icon = getBadgeIcon(badge.title),
                                isEarned = badge.isEarned,
                                onClick = { selectedBadge = badge } // Gán badge được chọn
                            )
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }

            // --- POPUP CHI TIẾT THÀNH TÍCH ---
            selectedBadge?.let { badge ->
                BadgeDetailDialog(
                    badge = badge,
                    onDismiss = { selectedBadge = null }
                )
            }
        }
    }
}

@Composable
fun BadgeDetailDialog(badge: Badge, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Biểu tượng huy hiệu trong Popup
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (badge.isEarned) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getBadgeIcon(badge.title),
                        contentDescription = null,
                        tint = if (badge.isEarned) PrimaryPurple else Color.Gray,
                        modifier = Modifier.size(40.dp).graphicsLayer(alpha = if (badge.isEarned) 1f else 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = badge.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = badge.description,
                    fontSize = 15.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị Yêu cầu
                Text("YÊU CẦU ĐẠT ĐƯỢC:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                if (badge.requiredPoints > 0) {
                    Text("• Tích lũy đủ: ${badge.requiredPoints} pts", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
                if (badge.requiredStreak > 0) {
                    Text("• Chuỗi tập luyện: ${badge.requiredStreak} ngày", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị Trạng thái
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("TRẠNG THÁI: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                    Text(
                        text = if (badge.isEarned) "Đã đạt được" else "Chưa hoàn thành",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isEarned) Color(0xFF4CAF50) else Color.Red
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("ĐÓNG", fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AchievementCard(
    title: String,
    description: String,
    icon: ImageVector,
    isEarned: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isEarned) Color.White else Color(0xFFF1F1F1)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isEarned) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isEarned) PrimaryPurple.copy(alpha = 0.1f) else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEarned) PrimaryPurple else Color.Gray,
                    modifier = Modifier.size(28.dp).graphicsLayer(alpha = if (isEarned) 1f else 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isEarned) Color.Black else Color.Gray
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    maxLines = 1 // Ở danh sách chỉ hiện 1 dòng cho gọn
                )
            }
            if (isEarned) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun getBadgeIcon(title: String): ImageVector {
    return when (title.lowercase()) {
        "tập sự" -> Icons.Default.FitnessCenter
        "duy trì" -> Icons.Default.Timeline
        "nâng cao" -> Icons.AutoMirrored.Filled.TrendingUp
        "chuỗi" -> Icons.Default.FlashOn
        "thử sức" -> Icons.Default.EmojiEvents
        "khỏe mạnh" -> Icons.Default.Favorite
        "bền bỉ" -> Icons.AutoMirrored.Filled.DirectionsRun
        "nâng tạ" -> Icons.Default.Sports
        else -> Icons.Default.Star
    }
}