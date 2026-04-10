package com.example.gymlab

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymlab.ui.*
import com.example.gymlab.ui.theme.GymlabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Xin quyền thông báo cho Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GymlabTheme(dynamicColor = false) {
                GymlabApp()
            }
        }
    }
}

@Composable
fun GymlabApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- MÀN HÌNH CÀI ĐẶT THÔNG BÁO ---
            composable(
                route = "notification_settings/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                NotificationSettingsScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 1. MÀN HÌNH ĐĂNG NHẬP
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { user ->
                        navController.navigate("home/${user.userId}/${user.username}") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate("register") },
                    onForgotPasswordClick = { navController.navigate("forgot_password") }
                )
            }

            // 2. MÀN HÌNH ĐĂNG KÝ
            composable("register") {
                RegisterScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegisterSuccess = { user ->
                        // SỬA TẠI ĐÂY: Thay vì vào thẳng Home, quay về Login
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }

            // 3. TRANG CHỦ (HOME)
            composable(
                route = "home/{userId}/{userName}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val userName = backStackEntry.arguments?.getString("userName") ?: "Người dùng"
                HomeScreen(
                    onProfileClick = { navController.navigate("profile/$userId/$userName") },
                    onDietClick = { navController.navigate("diet") },
                    onActivityClick = { navController.navigate("activity") },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

            // 4. TRANG CÁ NHÂN (PROFILE)
            composable(
                route = "profile/{userId}/{userName}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val userName = backStackEntry.arguments?.getString("userName") ?: "Người dùng"
                ProfileScreen(
                    userName = userName,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    onAchievementsClick = { navController.navigate("achievements/$userId") },
                    onProgressClick = { navController.navigate("progress/$userId") },
                    onSettingsClick = { navController.navigate("account_settings/$userName") }
                )
            }

            // 5. TRANG TIẾN ĐỘ (PROGRESS)
            composable(route = "progress/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                ProgressScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onAchievementsClick = { navController.navigate("achievements/$userId") },
                    onActivityClick = { navController.navigate("activity") },
                    onDietClick = { navController.navigate("diet") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

            // 6. TRANG THÀNH TỰU (ACHIEVEMENTS)
            composable(
                route = "achievements/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                AchievementsScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onActivityClick = { navController.navigate("activity") },
                    onDietClick = { navController.navigate("diet") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

            // 7. LỊCH TẬP (WORKOUT SCHEDULE)
            composable(
                route = "workout_schedule/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                WorkoutScheduleScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onAddWorkoutClick = { date -> navController.navigate("add_workout/$userId/$date") },
                    onExerciseClick = { exercise ->
                        navController.navigate("select_mode/$userId/${exercise.detailId}/${exercise.name}")
                    }
                )
            }

            // 8. CHỌN CHẾ ĐỘ (SELECT MODE)
            composable(
                route = "select_mode/{userId}/{detailId}/{name}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("detailId") { type = NavType.IntType },
                    navArgument("name") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val detailId = backStackEntry.arguments?.getInt("detailId") ?: 0
                val name = backStackEntry.arguments?.getString("name") ?: ""
                SelectModeScreen(
                    exerciseName = name,
                    onBackClick = { navController.popBackStack() },
                    onSelectMode = { isAI ->
                        val mode = if (isAI) "ai" else "normal"
                        navController.navigate("workout_timer/$userId/$detailId/$name/$mode")
                    }
                )
            }

            // 9. BẮT ĐẦU TẬP (WORKOUT TIMER)
            composable(
                route = "workout_timer/{userId}/{detailId}/{exerciseName}/{mode}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("detailId") { type = NavType.IntType },
                    navArgument("exerciseName") { type = NavType.StringType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val detailId = backStackEntry.arguments?.getInt("detailId") ?: 0
                val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: ""
                val mode = backStackEntry.arguments?.getString("mode") ?: "normal"

                WorkoutTimerScreen(
                    detailId = detailId,
                    exerciseName = exerciseName,
                    onFinish = { id, duration ->
                        navController.navigate("finish_workout/$userId/$id/$duration/$mode") {
                            popUpTo("workout_schedule/$userId") { inclusive = false }
                        }
                    }
                )
            }

            // 10. KẾT THÚC BÀI TẬP (FINISH SCREEN)
            composable(
                route = "finish_workout/{userId}/{detailId}/{duration}/{mode}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("detailId") { type = NavType.IntType },
                    navArgument("duration") { type = NavType.IntType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val detailId = backStackEntry.arguments?.getInt("detailId") ?: 0
                val duration = backStackEntry.arguments?.getInt("duration") ?: 0
                val mode = backStackEntry.arguments?.getString("mode") ?: "normal"

                FinishScreen(
                    detailId = detailId,
                    duration = duration,
                    mode = mode,
                    onContinueClick = {
                        navController.popBackStack("workout_schedule/$userId", inclusive = false)
                    }
                )
            }

            // 11. THÊM BÀI TẬP (ADD WORKOUT)
            composable(
                route = "add_workout/{userId}/{date}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val date = backStackEntry.arguments?.getString("date") ?: ""
                AddWorkoutScreen(
                    userId = userId,
                    targetDate = date,
                    onBackClick = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() },
                    onCreateTemplateClick = { navController.navigate("create_template") }
                )
            }

            // --- CÁC ROUTE KHÁC ---
            composable("diet") { DietScreen(onBackClick = { navController.popBackStack() }) }
            composable("activity") { ActivityScreen(onBackClick = { navController.popBackStack() }) }
            composable("create_template") { CreateTemplateScreen(onBackClick = { navController.popBackStack() }, onSuccess = { navController.popBackStack() }) }
            
            // QUÊN MẬT KHẨU
            composable("forgot_password") { 
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() }, 
                    onCodeSentSuccess = { email ->
                        navController.navigate("reset_password/$email")
                    }, 
                    onLoginNowClick = { navController.navigate("login") }
                ) 
            }
            
            // ĐẶT LẠI MẬT KHẨU MỚI
            composable(
                route = "reset_password/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetPasswordScreen(
                    email = email,
                    onBackClick = { navController.popBackStack() },
                    onResetSuccess = {
                        // Quay về màn hình đăng nhập và xóa stack cũ
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}