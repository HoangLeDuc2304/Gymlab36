package com.example.gymlab.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray

// Dùng tên khác để tránh trùng với API model
data class HomeExercise(
    val name: String,
    val duration: String,
    val youtubeUrl: String
)

data class WorkoutCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val exercises: List<HomeExercise>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProfileClick: () -> Unit,
    onDietClick: () -> Unit,
    onActivityClick: () -> Unit,
    onScheduleClick: () -> Unit, // Đổi tên cho rõ ràng
    onNotificationClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedWorkout by remember { mutableStateOf<WorkoutCategory?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val workoutCategories = listOf(
        WorkoutCategory(
            title = "Cardio đốt mỡ",
            subtitle = "25 phút • Đốt 200 kcal",
            icon = Icons.Default.DirectionsRun,
            iconBgColor = Color(0xFFE7F0FF),
            iconTintColor = Color(0xFF4081FF),
            exercises = listOf(
                HomeExercise("Jumping Jacks", "45s", "https://www.youtube.com/watch?v=yDSMdd8hiFg"),
                HomeExercise("Burpees", "30s", "https://www.youtube.com/watch?v=auBLPXO8Fww"),
                HomeExercise("Mountain Climbers", "45s", "https://www.youtube.com/watch?v=nmwgirgXLYM"),
                HomeExercise("High Knees", "45s", "https://www.youtube.com/watch?v=ZZZ0S3H3Aow"),
                HomeExercise("Butt Kicks", "45s", "https://www.youtube.com/watch?v=-dtvAXibgqE")
            )
        ),
        WorkoutCategory(
            title = "Tập bụng săn chắc",
            subtitle = "15 phút • Đốt 120 kcal",
            icon = Icons.Default.FitnessCenter,
            iconBgColor = Color(0xFFFFF7E6),
            iconTintColor = Color(0xFFFFA900),
            exercises = listOf(
                HomeExercise("Plank", "60s", "https://www.youtube.com/watch?v=pSHjTRCQxIw"),
                HomeExercise("Crunches", "20 reps", "https://www.youtube.com/watch?v=Xyd_fa5zoEU"),
                HomeExercise("Leg Raises", "15 reps", "https://www.youtube.com/watch?v=l4kQd9eWclE"),
                HomeExercise("Russian Twists", "30 reps", "https://www.youtube.com/watch?v=wkD8rjkodUI"),
                HomeExercise("Bicycle Crunches", "20 reps", "https://www.youtube.com/watch?v=9FGilxCbdz8")
            )
        ),
        WorkoutCategory(
            title = "Yoga giãn cơ tối",
            subtitle = "20 phút • Phục hồi",
            icon = Icons.Default.SelfImprovement,
            iconBgColor = Color(0xFFE6F7ED),
            iconTintColor = Color(0xFF00C04B),
            exercises = listOf(
                HomeExercise("Cat Cow Pose", "1 min", "https://www.youtube.com/watch?v=kqnua4rHVIc"),
                HomeExercise("Child's Pose", "2 mins", "https://www.youtube.com/watch?v=2MJGgGjkheA"),
                HomeExercise("Downward Dog", "1 min", "https://www.youtube.com/watch?v=j97P6M1m2Sg"),
                HomeExercise("Cobra Pose", "1 min", "https://www.youtube.com/watch?v=fOdrW7nf9gw"),
                HomeExercise("Corpse Pose", "5 mins", "https://www.youtube.com/watch?v=1VX7L293hS8")
            )
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = onProfileClick,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Trang chủ",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Mục tiêu: Giảm 2kg trong tháng",
                        fontSize = 14.sp,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Workout Suggestions
            Text(
                text = "Gợi ý bài tập hôm nay",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            workoutCategories.forEach { category ->
                WorkoutCard(
                    category = category,
                    onClick = {
                        selectedWorkout = category
                        showBottomSheet = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nutrition Suggestions
            Text(
                text = "Thực đơn dinh dưỡng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            NutritionCard(
                title = "Gợi ý Eat Clean",
                subtitle = "1200 kcal • 2 bữa chính, 1 phụ",
                onClick = onDietClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showBottomSheet && selectedWorkout != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ExerciseListContent(
                    workout = selectedWorkout!!,
                    onExerciseClick = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseListContent(
    workout: WorkoutCategory,
    onExerciseClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = workout.title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        workout.exercises.forEach { exercise ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExerciseClick(exercise.youtubeUrl) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(workout.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = workout.iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = exercise.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = exercise.duration,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun WorkoutCard(
    category: WorkoutCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.iconTintColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = category.subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = PrimaryPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun NutritionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF2E6))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = PrimaryPurple
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onProfileClick: () -> Unit,
    onActivityClick: () -> Unit,
    onDietClick: () -> Unit,
    onScheduleClick: () -> Unit // Thêm tham số này
) {
    NavigationBar(
        containerColor = PrimaryPurple,
        contentColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, "home"),
            Triple("Activity", Icons.Default.BarChart, "activity"),
            Triple("Schedule", Icons.Default.CalendarMonth, "schedule"), // Thêm vào cạnh Activity
            Triple("Diet", Icons.Default.Restaurant, "diet"),
            Triple("Profile", Icons.Default.Person, "profile")
        )

        items.forEach { (label, icon, route) ->
            val selected = route == "home"
            NavigationBarItem(
                selected = selected,
                onClick = { 
                    when (route) {
                        "profile" -> onProfileClick()
                        "activity" -> onActivityClick()
                        "diet" -> onDietClick()
                        "schedule" -> onScheduleClick() // Xử lý nhấn Lịch tập
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                },
                label = {
                    Text(
                        text = label,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryPurple.copy(alpha = 0.1f)
                )
            )
        }
    }
}