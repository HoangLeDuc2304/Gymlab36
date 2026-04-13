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
                        navController.navigate("home/${user.userId}/${user.username}/${user.email}") {
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
                        navController.navigate("home/${user.userId}/${user.username}/${user.email}") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // 3. TRANG CHỦ (HOME)
            composable(
                route = "home/{userId}/{userName}/{email}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val userNameParam = backStackEntry.arguments?.getString("userName") ?: "Người dùng"
                val email = backStackEntry.arguments?.getString("email") ?: ""
                
                // Sử dụng state để cập nhật tên hiển thị ngay lập tức
                var currentUserName by remember { mutableStateOf(userNameParam) }

                HomeScreen(
                    userId = userId,
                    userName = currentUserName,
                    onProfileClick = { navController.navigate("profile/$userId/$currentUserName/$email") },
                    onDietClick = { navController.navigate("diet/$userId") },
                    onActivityClick = { navController.navigate("activity/$userId") },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

            // 4. TRANG CÁ NHÂN (PROFILE)
            composable(
                route = "profile/{userId}/{userName}/{email}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val userNameParam = backStackEntry.arguments?.getString("userName") ?: "Người dùng"
                val email = backStackEntry.arguments?.getString("email") ?: ""

                var currentUserName by remember { mutableStateOf(userNameParam) }

                ProfileScreen(
                    userId = userId,
                    userName = currentUserName,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    },
                    onAchievementsClick = { navController.navigate("achievements/$userId") },
                    onProgressClick = { navController.navigate("progress/$userId") },
                    onSettingsClick = { navController.navigate("account_settings/$userId/$currentUserName/$email") },
                    onAvatarUpdated = { /* Xử lý nếu cần cập nhật state toàn cục */ }
                )
            }

            // 11. CÀI ĐẶT TÀI KHOẢN (ACCOUNT SETTINGS)
            composable(
                route = "account_settings/{userId}/{userName}/{email}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("email") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val userNameParam = backStackEntry.arguments?.getString("userName") ?: ""
                val email = backStackEntry.arguments?.getString("email") ?: ""

                AccountSettingsScreen(
                    userId = userId,
                    userName = userNameParam,
                    onBackClick = { navController.popBackStack() },
                    onNameUpdated = { newName ->
                        // Khi đổi tên thành công, quay lại Home với tên mới
                        navController.navigate("home/$userId/$newName/$email") {
                            popUpTo("home/$userId/$userNameParam/$email") { inclusive = true }
                        }
                    },
                    onChangePasswordClick = { 
                        navController.navigate("change_password/$email")
                    },
                    onDeleteAccountClick = { /* Xử lý xóa tài khoản */ }
                )
            }

            // Các màn hình khác giữ nguyên...
            composable(
                route = "progress/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                ProgressScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onAchievementsClick = { navController.navigate("achievements/$userId") },
                    onActivityClick = { navController.navigate("activity/$userId") },
                    onDietClick = { navController.navigate("diet/$userId") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

            composable(
                route = "achievements/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                AchievementsScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onNotificationClick = { navController.navigate("notification_settings/$userId") },
                    onActivityClick = { navController.navigate("activity/$userId") },
                    onDietClick = { navController.navigate("diet/$userId") },
                    onScheduleClick = { navController.navigate("workout_schedule/$userId") }
                )
            }

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
                    mode = mode,
                    onFinish = { id, duration ->
                        navController.navigate("finish_workout/$userId/$id/$duration/$mode") {
                            popUpTo("workout_schedule/$userId") { inclusive = false }
                        }
                    }
                )
            }

            composable(
                route = "finish_workout/{userId}/{id}/{duration}/{mode}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.IntType },
                    navArgument("id") { type = NavType.IntType },
                    navArgument("duration") { type = NavType.IntType },
                    navArgument("mode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val duration = backStackEntry.arguments?.getInt("duration") ?: 0
                val mode = backStackEntry.arguments?.getString("mode") ?: "normal"
                FinishScreen(
                    detailId = id,
                    duration = duration,
                    mode = mode,
                    onContinueClick = {
                        navController.popBackStack("workout_schedule/$userId", inclusive = false)
                    }
                )
            }

            composable(
                route = "change_password/{email}",
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ResetPasswordScreen(
                    email = email,
                    onBackClick = { navController.popBackStack() },
                    onResetSuccess = {
                        navController.popBackStack()
                    }
                )
            }

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

            composable(
                route = "diet/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                DietScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "activity/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                ActivityScreen(
                    userId = userId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("create_template") { CreateTemplateScreen(onBackClick = { navController.popBackStack() }, onSuccess = { navController.popBackStack() }) }
            composable("forgot_password") { 
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() }, 
                    onCodeSentSuccess = { email -> 
                    }, 
                    onLoginNowClick = { navController.navigate("login") }
                ) 
            }
        }
    }
}
