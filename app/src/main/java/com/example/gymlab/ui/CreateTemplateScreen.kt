//package com.example.gymlab.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Delete
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
//// Model cho yêu cầu tạo mẫu
////data class TemplateExerciseRequest(
////    val exerciseId: Int,
////    val exerciseName: String,
////    var sets: Int,
////    var reps: Int,
////    val orderIndex: Int
////)
////
////data class WorkoutTemplateRequest(
////    val name: String,
////    val description: String,
////    val exercises: List<TemplateExerciseRequest>
////)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateTemplateScreen(
//    onBackClick: () -> Unit,
//    onSuccess: () -> Unit
//) {
//    val scope = rememberCoroutineScope()
//    var templateName by remember { mutableStateOf("") }
//    var templateDescription by remember { mutableStateOf("") }
//
//    val selectedExercises = remember { mutableStateListOf<TemplateExerciseRequest>() }
//    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
//    var allExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
//    var filteredExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
//    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
//
//    LaunchedEffect(Unit) {
//        scope.launch {
//            try {
//                val catRes = RetrofitClient.instance.getCategories()
//                if (catRes.isSuccessful) categories = catRes.body() ?: emptyList()
//
//                val exRes = RetrofitClient.instance.getExercises(null, null)
//                if (exRes.isSuccessful) {
//                    allExercises = exRes.body() ?: emptyList()
//                    filteredExercises = allExercises
//                }
//            } catch (e: Exception) { e.printStackTrace() }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Tạo mẫu lịch tập", fontWeight = FontWeight.Bold) },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .background(Color(0xFFF8F9FA))
//        ) {
//            LazyColumn(
//                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    OutlinedTextField(
//                        value = templateName,
//                        onValueChange = { templateName = it },
//                        label = { Text("Tên mẫu (VD: Tập ngực thứ 2)") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                    OutlinedTextField(
//                        value = templateDescription,
//                        onValueChange = { templateDescription = it },
//                        label = { Text("Mô tả (Không bắt buộc)") },
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                }
//
//                item {
//                    Text("Bài tập đã chọn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                    if (selectedExercises.isEmpty()) {
//                        Text("Chưa có bài tập nào", color = Color.Gray, fontSize = 14.sp)
//                    }
//                }
//
//                items(selectedExercises) { item ->
//                    SelectedExerciseCard(item) {
//                        selectedExercises.remove(item)
//                    }
//                }
//
//                item {
//                    Divider()
//                    Text("Danh sách bài tập hệ thống", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        item {
//                            CategoryChip(name = "Tất cả", isSelected = selectedCategoryId == null) {
//                                selectedCategoryId = null
//                                filteredExercises = allExercises
//                            }
//                        }
//                        items(categories) { cat ->
//                            CategoryChip(name = cat.name, isSelected = selectedCategoryId == cat.id) {
//                                selectedCategoryId = cat.id
//                                filteredExercises = allExercises.filter { it.categoryId == cat.id }
//                            }
//                        }
//                    }
//                }
//
//                items(filteredExercises) { exercise ->
//                    ExerciseListItem(exercise) {
//                        if (selectedExercises.none { it.exerciseId == exercise.id }) {
//                            selectedExercises.add(
//                                TemplateExerciseRequest(
//                                    exerciseId = exercise.id,
//                                    exerciseName = exercise.name,
//                                    sets = 3,
//                                    reps = 12,
//                                    orderIndex = selectedExercises.size + 1
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//
//            Button(
//                onClick = {
//                    if (templateName.isBlank() || selectedExercises.isEmpty()) return@Button
//
//                    scope.launch {
//                        try {
//                            val request = WorkoutTemplateRequest(
//                                name = templateName,
//                                description = templateDescription,
//                                exercises = selectedExercises.toList()
//                            )
//
//                            val response = RetrofitClient.instance.createWorkoutTemplate(request)
//                            if (response.isSuccessful && response.body()?.success == true) {
//                                onSuccess()
//                            }
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//                    .height(56.dp),
//                shape = RoundedCornerShape(16.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
//            ) {
//                Text("LƯU MẪU LỊCH TẬP", fontWeight = FontWeight.Bold)
//            }
//        }
//    }
//}
//
//@Composable
//fun SelectedExerciseCard(item: TemplateExerciseRequest, onDelete: () -> Unit) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp).fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Column(modifier = Modifier.weight(1f)) {
//                Text(item.exerciseName, fontWeight = FontWeight.Bold)
//                Text("${item.sets} hiệp x ${item.reps} lần", fontSize = 14.sp, color = Color.Gray)
//            }
//            IconButton(onClick = onDelete) {
//                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
//            }
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.Category
import com.example.gymlab.api.Exercise
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.TemplateExerciseRequest
import com.example.gymlab.api.WorkoutTemplateRequest
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var templateName by remember { mutableStateOf("") }
    var templateDescription by remember { mutableStateOf("") }

    val selectedExercises = remember { mutableStateListOf<TemplateExerciseRequest>() }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var allExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var filteredExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val catRes = RetrofitClient.instance.getCategories()
                if (catRes.isSuccessful) {
                    categories = catRes.body() ?: emptyList()
                }

                val exRes = RetrofitClient.instance.getExercises(limit = null, categoryId = null)
                if (exRes.isSuccessful) {
                    allExercises = exRes.body() ?: emptyList()
                    filteredExercises = allExercises
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo mẫu lịch tập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("Tên mẫu (VD: Tập ngực thứ 2)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = templateDescription,
                        onValueChange = { templateDescription = it },
                        label = { Text("Mô tả (Không bắt buộc)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Text("Bài tập đã chọn", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (selectedExercises.isEmpty()) {
                        Text("Chưa có bài tập nào", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                items(selectedExercises) { item ->
                    SelectedExerciseCard(item) {
                        selectedExercises.remove(item)
                    }
                }

                item {
                    HorizontalDivider()
                    Text("Danh sách bài tập hệ thống", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            CategoryChip(
                                name = "Tất cả",
                                isSelected = selectedCategoryId == null
                            ) {
                                selectedCategoryId = null
                                filteredExercises = allExercises
                            }
                        }

                        items(categories) { cat ->
                            CategoryChip(
                                name = cat.name,
                                isSelected = selectedCategoryId == cat.id
                            ) {
                                selectedCategoryId = cat.id
                                filteredExercises = allExercises.filter { it.categoryId == cat.id }
                            }
                        }
                    }
                }

                items(filteredExercises) { exercise ->
                    ExerciseListItem(exercise) {
                        if (selectedExercises.none { it.exerciseId == exercise.id }) {
                            selectedExercises.add(
                                TemplateExerciseRequest(
                                    exerciseId = exercise.id,
                                    exerciseName = exercise.name,
                                    sets = 3,
                                    reps = 12,
                                    orderIndex = selectedExercises.size + 1
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (templateName.isBlank() || selectedExercises.isEmpty() || isSaving) return@Button

                    isSaving = true
                    scope.launch {
                        try {
                            val request = WorkoutTemplateRequest(
                                name = templateName,
                                description = templateDescription,
                                exercises = selectedExercises.toList()
                            )

                            val response = RetrofitClient.instance.createWorkoutTemplate(request)
                            if (response.isSuccessful && response.body()?.success == true) {
                                onSuccess()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(
                    if (isSaving) "ĐANG LƯU..." else "LƯU MẪU LỊCH TẬP",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SelectedExerciseCard(item: TemplateExerciseRequest, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.exerciseName, fontWeight = FontWeight.Bold)
                Text("${item.sets} hiệp x ${item.reps} lần", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}