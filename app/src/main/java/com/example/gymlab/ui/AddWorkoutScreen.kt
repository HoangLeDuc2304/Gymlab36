//package com.example.gymlab.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.gymlab.api.*
//import com.example.gymlab.ui.theme.PrimaryPurple
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddWorkoutScreen(
//    targetDate: String,
//    onBackClick: () -> Unit,
//    onSuccess: () -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var templates by remember { mutableStateOf<List<WorkoutTemplate>>(emptyList()) }
//    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
//    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
//    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
//
//    // Giả định userId = 1
//    val userId = 1
//
//    fun fetchExercises(catId: Int? = null) {
//        scope.launch {
//            try {
//                val res = RetrofitClient.instance.getExercises(null, catId)
//                if (res.isSuccessful) exercises = res.body() ?: emptyList()
//            } catch (e: Exception) { e.printStackTrace() }
//        }
//    }
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            try {
//                val tRes = RetrofitClient.instance.getTemplates(20)
//                if (tRes.isSuccessful) templates = tRes.body() ?: emptyList()
//
//                val cRes = RetrofitClient.instance.getCategories()
//                if (cRes.isSuccessful) categories = cRes.body() ?: emptyList()
//
//                fetchExercises()
//            } catch (e: Exception) { e.printStackTrace() }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Thêm vào: $targetDate", fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .background(Color(0xFFF8F9FA)),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            // Mẫu lịch tập
//            item {
//                Text("Mẫu lịch tập", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                Spacer(modifier = Modifier.height(8.dp))
//                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                    items(templates) { template ->
//                        TemplateCard(template) {
//                            scope.launch {
//                                try {
//                                    val res = RetrofitClient.instance.applyTemplate(ApplyTemplateRequest(userId, targetDate, template.id))
//                                    if (res.isSuccessful) onSuccess()
//                                } catch (e: Exception) { e.printStackTrace() }
//                            }
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//
//            // Bài tập lẻ
//            item {
//                Text("Bài tập lẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                Spacer(modifier = Modifier.height(8.dp))
//                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    item {
//                        CategoryChip(name = "Tất cả", isSelected = selectedCategoryId == null) {
//                            selectedCategoryId = null
//                            fetchExercises(null)
//                        }
//                    }
//                    items(categories) { cat ->
//                        CategoryChip(name = cat.name, isSelected = selectedCategoryId == cat.id) {
//                            selectedCategoryId = cat.id
//                            fetchExercises(cat.id)
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//
//            items(exercises) { exercise ->
//                ExerciseListItem(exercise) {
//                    scope.launch {
//                        try {
//                            val res = RetrofitClient.instance.addSingleExercise(AddExerciseRequest(userId, targetDate, exercise.id))
//                            if (res.isSuccessful) onSuccess()
//                        } catch (e: Exception) { e.printStackTrace() }
//                    }
//                }
//                Spacer(modifier = Modifier.height(12.dp))
//            }
//        }
//    }
//}
//
//@Composable
//fun TemplateCard(template: WorkoutTemplate, onClick: () -> Unit) {
//    Card(
//        onClick = onClick,
//        modifier = Modifier.width(160.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(2.dp)
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(template.name, fontWeight = FontWeight.Bold, maxLines = 1)
//            Text(template.description ?: "", fontSize = 12.sp, color = Color.Gray, maxLines = 2)
//        }
//    }
//}
package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.AddExerciseRequest
import com.example.gymlab.api.ApplyTemplateRequest
import com.example.gymlab.api.Category
import com.example.gymlab.api.Exercise
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.WorkoutTemplate
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    userId: Int,
    targetDate: String,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onCreateTemplateClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var templates by remember { mutableStateOf<List<WorkoutTemplate>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun fetchExercises(catId: Int? = null) {
        scope.launch {
            try {
                val res = RetrofitClient.instance.getExercises(limit = null, categoryId = catId)
                if (res.isSuccessful) {
                    exercises = res.body() ?: emptyList()
                    android.util.Log.d("AddWorkoutScreen", "Exercises loaded: ${exercises.size}")
                } else {
                    android.util.Log.e("AddWorkoutScreen", "getExercises failed: ${res.code()} - ${res.errorBody()?.string()}")
                    exercises = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("AddWorkoutScreen", "getExercises exception", e)
                e.printStackTrace()
            }
        }
    }
    fun fetchTemplates() {
        scope.launch {
            try {
                val tRes = RetrofitClient.instance.getTemplates(limit = 20)
                if (tRes.isSuccessful) {
                    templates = tRes.body() ?: emptyList()
                    android.util.Log.d("AddWorkoutScreen", "Templates loaded: ${templates.size}")
                } else {
                    android.util.Log.e(
                        "AddWorkoutScreen",
                        "getTemplates failed: ${tRes.code()} - ${tRes.errorBody()?.string()}"
                    )
                    templates = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("AddWorkoutScreen", "getTemplates exception", e)
                e.printStackTrace()
                templates = emptyList()
            }
        }
    }

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                fetchTemplates()

                val cRes = RetrofitClient.instance.getCategories()
                if (cRes.isSuccessful) {
                    categories = cRes.body() ?: emptyList()
                    android.util.Log.d("AddWorkoutScreen", "Categories loaded: ${categories.size}")
                } else {
                    android.util.Log.e("AddWorkoutScreen", "getCategories failed: ${cRes.code()} - ${cRes.errorBody()?.string()}")
                }

                fetchExercises()
            } catch (e: Exception) {
                android.util.Log.e("AddWorkoutScreen", "LaunchedEffect exception", e)
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                fetchTemplates()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm vào: $targetDate", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mẫu lịch tập", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        TextButton(onClick = onCreateTemplateClick) {
                            Text("Tạo mẫu mới")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(templates) { template ->
                            TemplateCard(template) {
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.instance.applyTemplateToDate(
                                            ApplyTemplateRequest(
                                                userId = userId,
                                                targetDate = targetDate,
                                                templateId = template.id
                                            )
                                        )
                                        if (res.isSuccessful && res.body()?.success == true) {
                                            onSuccess()
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text("Bài tập lẻ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            CategoryChip(
                                name = "Tất cả",
                                isSelected = selectedCategoryId == null
                            ) {
                                selectedCategoryId = null
                                fetchExercises(null)
                            }
                        }

                        items(categories) { cat ->
                            CategoryChip(
                                name = cat.name,
                                isSelected = selectedCategoryId == cat.id
                            ) {
                                selectedCategoryId = cat.id
                                fetchExercises(cat.id)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(exercises) { exercise ->
                    ExerciseListItem(exercise) {
                        scope.launch {
                            try {
                                val res = RetrofitClient.instance.addExerciseToSchedule(
                                    AddExerciseRequest(
                                        userId = userId,
                                        targetDate = targetDate,
                                        exerciseId = exercise.id
                                    )
                                )
                                if (res.isSuccessful && res.body()?.success == true) {
                                    onSuccess()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun TemplateCard(template: WorkoutTemplate, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(template.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(template.description ?: "", fontSize = 12.sp, color = Color.Gray, maxLines = 2)
        }
    }
}