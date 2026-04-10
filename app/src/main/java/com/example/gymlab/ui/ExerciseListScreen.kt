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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.Category
import com.example.gymlab.api.Exercise
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    onBackClick: () -> Unit,
    onExerciseClick: (Exercise) -> Unit
) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun fetchExercises(categoryId: Int? = null) {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getExercises(
                    limit = null,
                    categoryId = categoryId
                )
                if (response.isSuccessful) {
                    exercises = response.body() ?: emptyList()
                } else {
                    exercises = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                exercises = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val catResponse = RetrofitClient.instance.getCategories()
                if (catResponse.isSuccessful) {
                    categories = catResponse.body() ?: emptyList()
                }
                fetchExercises()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh sách bài tập", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            // Categories
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        name = "Tất cả",
                        isSelected = selectedCategoryId == null,
                        onClick = {
                            selectedCategoryId = null
                            fetchExercises(null)
                        }
                    )
                }
                items(categories) { category ->
                    CategoryChip(
                        name = category.name,
                        isSelected = selectedCategoryId == category.id,
                        onClick = {
                            selectedCategoryId = category.id
                            fetchExercises(category.id)
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryPurple)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exercises) { exercise ->
                        ExerciseListItem(exercise = exercise, onClick = { onExerciseClick(exercise) })
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PrimaryPurple else Color(0xFFE0E7FF),
            contentColor = if (isSelected) Color.White else Color(0xFF4F46E5)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ExerciseListItem(exercise: Exercise, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
                    .size(60.dp)
                    .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for image
                Text("IMG", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = exercise.description ?: "Không có mô tả",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}