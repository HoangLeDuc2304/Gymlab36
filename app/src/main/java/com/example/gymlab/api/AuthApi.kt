package com.example.gymlab.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// ===== Response dùng chung =====
data class ApiResponse(
    val success: Boolean,
    val message: String
)

// ===== Login / Register =====
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerializedName("full_name") val fullName: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: UserData? = null
)

data class UserData(
    @SerializedName("user_id") val userId: Int? = null,
    val username: String? = null,
    val email: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    val goal_note: String? = null
)

// ===== Quên mật khẩu =====
data class SendOtpRequest(val email: String)
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    @SerializedName("newPassword") val newPassword: String
)

// ===== Diet =====
data class DietSuggestion(
    @SerializedName("suggestion_id") val id: Int? = null,
    val title: String,
    val calories: Int,
    @SerializedName("meal_type") val mealType: String
)

data class DietListResponse(
    val success: Boolean,
    val data: List<DietSuggestion>? = emptyList()
)

// ===== Weight =====
data class WeightRecordApi(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val weight: Float,
    @SerializedName("recorded_date") val recordedDate: String? = null
)

data class WeightHistoryResponse(
    val success: Boolean,
    val data: List<WeightRecordApi>? = emptyList()
)

data class AddWeightRequest(
    val weight: Float,
    @SerializedName("user_id") val userId: Int?
)

// ===== Workout / Category / Exercise =====
data class Category(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)

data class Exercise(
    val id: Int,
    val name: String,

    @SerializedName("category_id")
    val categoryId: Int? = null,

    @SerializedName("category_name")
    val categoryName: String? = null,

    val description: String? = null,
    val calories: Int? = 0,

    @SerializedName("difficulty_level")
    val difficultyLevel: String? = null,

    @SerializedName("duration_seconds")
    val durationSeconds: Int? = 0,

    @SerializedName("video_url")
    val videoUrl: String? = null
)

data class WorkoutTemplate(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("is_global") val isGlobal: Int? = 0
)

data class TemplateExerciseRequest(
    @SerializedName("exerciseId") val exerciseId: Int,
    @SerializedName("exerciseName") val exerciseName: String,
    var sets: Int,
    var reps: Int,
    @SerializedName("orderIndex") val orderIndex: Int
)

data class WorkoutTemplateRequest(
    @SerializedName("user_id") val userId: Int? = 1,
    val name: String,
    val description: String,
    val exercises: List<TemplateExerciseRequest>
)

data class ApplyTemplateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("target_date") val targetDate: String,
    @SerializedName("template_id") val templateId: Int
)

data class AddExerciseRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("target_date") val targetDate: String,
    @SerializedName("exercise_id") val exerciseId: Int
)

data class WorkoutResultRequest(
    @SerializedName("detail_id") val detailId: Int,
    val duration: Int,
    val mode: String,
    @SerializedName("effort_feedback") val effortFeedback: Int
)

data class WorkoutResponse(
    val success: Boolean,
    val message: String,
    val calories: Int? = null,
    val exp: Int? = null
)

// ===== Daily schedule =====
data class WorkoutExercise(
    @SerializedName(value = "detailId", alternate = ["detail_id", "id"])
    val detailId: Int,

    @SerializedName(value = "name", alternate = ["exercise_name"])
    val name: String,

    val duration: String,

    @SerializedName(value = "isCompleted", alternate = ["is_completed"])
    val isCompleted: Boolean,

    val calories: Int
)

data class DailyScheduleResponse(
    @SerializedName(value = "totalCalories", alternate = ["total_calories"])
    val totalCalories: Int = 0,

    @SerializedName(value = "completedCount", alternate = ["completed_count"])
    val completedCount: Int = 0,

    @SerializedName(value = "totalCount", alternate = ["total_count"])
    val totalCount: Int = 0,

    val exercises: List<WorkoutExercise> = emptyList()
)

data class AchievementStats(
    val level: Int = 1,
    @SerializedName("total_exp") val totalExp: Int = 0, // Dùng để tính Level
    @SerializedName("current_streak") val currentStreak: Int = 0,
    @SerializedName("longest_streak") val longestStreak: Int = 0,
    @SerializedName("total_points") val totalPoints: Int = 0, // Tổng điểm tích lũy
    @SerializedName("total_time") val totalTime: Int = 0,    // Thêm trường này (tính bằng giây)
    @SerializedName("last_activity_date") val lastActivityDate: String? = null,
    @SerializedName("last_workout_date") val lastWorkoutDate: String? = null
)

data class Badge(
    @SerializedName("badge_id") val badgeId: Int,
    val title: String,
    val description: String,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("required_points") val requiredPoints: Int,
    @SerializedName("required_streak") val requiredStreak: Int,
    @SerializedName("is_earned") val isEarned: Boolean = false
)

data class AchievementsResponse(
    val success: Boolean,
    val message: String?,
    val stats: AchievementStats?,
    val badges: List<Badge>? = emptyList()
)

interface AuthApi {

    // ===== Auth =====
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<ApiResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse>

    // ===== Diet =====
    @GET("diet/{day}")
    suspend fun getDietByDay(@Path("day") day: String): Response<DietListResponse>

    @POST("diet/add")
    suspend fun addDiet(@Body diet: DietSuggestion): Response<ApiResponse>

    @DELETE("diet/{id}")
    suspend fun deleteDiet(@Path("id") id: Int): Response<ApiResponse>

    // ===== Weight =====
    @GET("weight/history/{userId}")
    suspend fun getWeightHistory(@Path("userId") userId: Int): Response<WeightHistoryResponse>

    @POST("weight/add")
    suspend fun addWeight(@Body request: AddWeightRequest): Response<ApiResponse>

    // ===== Workout =====
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("exercises")
    suspend fun getExercises(
        @Query("limit") limit: Int? = null,
        @Query("category_id") categoryId: Int? = null
    ): Response<List<Exercise>>

    @GET("templates")
    suspend fun getTemplates(
        @Query("limit") limit: Int? = null
    ): Response<List<WorkoutTemplate>>

    @POST("workout-templates")
    suspend fun createWorkoutTemplate(
        @Body request: WorkoutTemplateRequest
    ): Response<ApiResponse>

    @POST("apply-template")
    suspend fun applyTemplateToDate(
        @Body request: ApplyTemplateRequest
    ): Response<ApiResponse>

    @POST("add-exercise")
    suspend fun addExerciseToSchedule(
        @Body request: AddExerciseRequest
    ): Response<ApiResponse>

    @GET("daily-schedule")
    suspend fun getDailySchedule(
        @Query("user_id") userId: Int,
        @Query("date") date: String
    ): Response<DailyScheduleResponse>

    @POST("complete-exercise")
    suspend fun completeExercise(
        @Body request: WorkoutResultRequest
    ): Response<WorkoutResponse>

    @GET("achievements/{userId}")
    suspend fun getAchievements(
        @Path("userId") userId: Int
    ): Response<AchievementsResponse>
}