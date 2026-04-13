package com.example.gymlab.ui

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

data class Exercise(
    val name: String,
    val duration: String,
    val videoUrl: String
)

data class WorkoutCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTintColor: Color,
    val exercises: List<Exercise>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: Int,
    userName: String,
    onProfileClick: () -> Unit,
    onDietClick: () -> Unit,
    onActivityClick: () -> Unit,
    onScheduleClick: () -> Unit,
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
                Exercise("Jumping Jacks", "2 mins", "https://www.youtube.com/watch?v=yDSMdd8hiFg"),
                Exercise("Burpees", "52s", "https://www.youtube.com/watch?v=auBLPXO8Fww"),
                Exercise("Mountain Climbers", "1 min", "https://www.youtube.com/watch?v=nmwgirgXLYM"),
                Exercise("High Knees", "55s", "https://www.youtube.com/watch?v=oDdkytliOqE"),
                Exercise("Butt Kicks", "31s", "https://www.youtube.com/watch?v=-dtvAxibgYQ")
            )
        ),
        WorkoutCategory(
            title = "Tập bụng săn chắc",
            subtitle = "15 phút • Đốt 120 kcal",
            icon = Icons.Default.FitnessCenter,
            iconBgColor = Color(0xFFFFF7E6),
            iconTintColor = Color(0xFFFFA900),
            exercises = listOf(
                Exercise("Plank", "1 min", "https://www.youtube.com/watch?v=pvIjsG5Svck"),
                Exercise("Crunches", "20 reps", "https://www.youtube.com/watch?v=Xyd_fa5zoEU"),
                Exercise("Leg Raises", "15 reps", "https://www.youtube.com/watch?v=JB2oyawG9KI"),
                Exercise("Russian Twists", "30 reps", "https://www.youtube.com/watch?v=JyUqwkVpsi8"),
                Exercise("Bicycle Crunches", "20 reps", "https://www.youtube.com/watch?v=cbKIDZ_XyjY")
            )
        ),
        WorkoutCategory(
            title = "Yoga giãn cơ tối",
            subtitle = "20 phút • Phục hồi",
            icon = Icons.Default.SelfImprovement,
            iconBgColor = Color(0xFFE6F7ED),
            iconTintColor = Color(0xFF00C04B),
            exercises = listOf(
                Exercise("Cat Cow Pose", "1 min", "https://www.youtube.com/watch?v=vuyUwtHl694"),
                Exercise("Child's Pose", "2 mins", "https://www.youtube.com/watch?v=31Fe9sxZJ6U"),
                Exercise("Downward Dog", "1 min", "https://www.youtube.com/watch?v=J8QhVr5Pvig"),
                Exercise("Cobra Pose", "1 min", "https://www.youtube.com/watch?v=JDcdhTuycOI"),
                Exercise("Corpse Pose", "5 mins", "https://www.youtube.com/watch?v=TcO40hEcVl4")
            )
        )
    )

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = onProfileClick,
                onActivityClick = onActivityClick,
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
                        text = "Chào mừng, $userName",
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
fun WorkoutCard(
    category: WorkoutCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = category.subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = Color.Gray
            )
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
                    .background(Color(0xFFFFF2E6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color(0xFFFF9500),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
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
    onScheduleClick: () -> Unit
) {
    NavigationBar(
        containerColor = PrimaryPurple,
        contentColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, "home"),
            Triple("Activity", Icons.Default.BarChart, "activity"),
            Triple("Schedule", Icons.Default.CalendarMonth, "schedule"),
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
                        "schedule" -> onScheduleClick()
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

@Composable
fun ExerciseListContent(
    workout: WorkoutCategory,
    onExerciseClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
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
                    .clickable { onExerciseClick(exercise.videoUrl) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = exercise.name, fontWeight = FontWeight.Medium)
                    Text(text = exercise.duration, fontSize = 12.sp, color = Color.Gray)
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
