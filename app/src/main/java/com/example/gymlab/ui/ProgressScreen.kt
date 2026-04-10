package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.AchievementStats
import com.example.gymlab.api.Badge
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ProgressScreen(
    userId: Int,
    onBackClick: () -> Unit,
    onActivityClick: () -> Unit,
    onDietClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<AchievementStats?>(null) }
    var badges by remember { mutableStateOf<List<Badge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch dữ liệu từ server khi màn hình được khởi tạo
    LaunchedEffect(userId) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.getAchievements(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    stats = response.body()?.stats
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // --- Header Section ---
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
                IconButton(onClick = onNotificationClick) { // Nhớ thêm tham số onNotificationClick: () -> Unit vào hàm Composable
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Theo dõi tiến độ", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                // --- Level & EXP Section (Logic: 1000 EXP = 1 Level) ---
                val totalExp = stats?.totalExp ?: 0
                val level = stats?.level ?: 1
                // Tính % tiến trình trong Level hiện tại
                val expProgress = (totalExp % 1000).toFloat() / 1000f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Kinh nghiệm: $totalExp EXP", fontSize = 14.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                    Text("Level $level", fontSize = 14.sp, color = PrimaryPurple, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { expProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = PrimaryPurple,
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Stats Grid Section ---
                // Hàng 1: Điểm tích lũy và Chuỗi hiện tại
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Điểm tích lũy",
                        value = "${stats?.totalPoints ?: 0}",
                        valueColor = PrimaryPurple,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        title = "Chuỗi hiện tại",
                        value = "${stats?.currentStreak ?: 0} 🔥",
                        valueColor = Color(0xFFFF5722),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hàng 2: Kỷ lục chuỗi và Thời gian tập
                val totalSeconds = stats?.totalTime ?: 0
                val hours = totalSeconds / 3600
                val mins = (totalSeconds % 3600) / 60
                val timeString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Kỷ lục chuỗi",
                        value = "${stats?.longestStreak ?: 0} 🔥",
                        valueColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    StatCard(
                        title = "Thời gian tập",
                        value = timeString,
                        valueColor = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Badges Preview Section ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Huy hiệu & mở khóa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextGray)
                    TextButton(onClick = onAchievementsClick) {
                        Text("Xem tất cả", color = PrimaryPurple, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị tối đa 6 huy hiệu tiêu biểu
                val displayBadges = badges.sortedByDescending { it.isEarned }.take(6)
                BadgeGrid(displayBadges)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun BadgeGrid(badges: List<Badge>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val rows = (badges.size + 2) / 3
        for (i in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 3) {
                    val index = i * 3 + j
                    if (index < badges.size) {
                        val badge = badges[index]
                        BadgeItem(
                            name = badge.title,
                            icon = getBadgeIcon(badge.title),
                            isEarned = badge.isEarned,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BadgeItem(
    name: String,
    icon: ImageVector,
    isEarned: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) PrimaryPurple.copy(alpha = 0.15f)
                    else Color(0xFFE0E0E0) // Màu xám nhạt cho badge chưa đạt
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                // Logic đổi màu: Có màu khi đạt được, màu xám khi chưa có
                tint = if (isEarned) PrimaryPurple else Color(0xFF9E9E9E),
                modifier = Modifier
                    .size(32.dp)
                    // Giảm độ mờ nếu chưa đạt được
                    .graphicsLayer(alpha = if (isEarned) 1f else 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isEarned) FontWeight.Bold else FontWeight.Medium,
            // Chữ mờ đi nếu chưa đạt
            color = if (isEarned) Color.Black else Color.Gray,
            textAlign = TextAlign.Center
        )
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