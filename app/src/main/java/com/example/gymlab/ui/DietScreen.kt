package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.DietSuggestion
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun DayTab(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(45.dp).clip(RoundedCornerShape(12.dp)),
        color = if (isSelected) PrimaryPurple else Color(0xFFF5F5F5)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = day, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
        }
    }
}

@Composable
fun DietMealCard(title: String, subtitle: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFF9C4)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = subtitle, fontSize = 13.sp, color = Color.Gray)
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun MealSection(sectionTitle: String, meals: List<DietSuggestion>, onDeleteMeal: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(text = sectionTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(12.dp))
        if (meals.isEmpty()) {
            Text(text = "Chưa có món ăn", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))
        } else {
            meals.forEach { meal ->
                val displayTitle = meal.title.substringAfter(": ").trim()
                DietMealCard(
                    title = displayTitle,
                    subtitle = "${meal.calories} Kcal • 5 phút",
                    onDelete = { meal.id?.let { onDeleteMeal(it) } }
                )
            }
        }
    }
}

@Composable
fun DietScreen(onBackClick: () -> Unit) {
    var selectedDay by remember { mutableStateOf("T3") }
    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7")
    
    var showDialog by remember { mutableStateOf(false) }
    var newMealTitle by remember { mutableStateOf("") }
    var newMealCalo by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Bữa Sáng") }

    val scope = rememberCoroutineScope()
    val meals = remember { mutableStateListOf<DietSuggestion>() }
    var isLoading by remember { mutableStateOf(false) }

    val totalCalories = remember(meals.toList()) { meals.sumOf { it.calories } }

    fun loadDiet(day: String) {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getDietByDay(day)
                if (response.isSuccessful && response.body()?.success == true) {
                    meals.clear()
                    response.body()?.data?.let { meals.addAll(it) }
                }
            } catch (e: Exception) { } finally { isLoading = false }
        }
    }

    fun deleteMeal(id: Int) {
        scope.launch {
            try {
                val response = RetrofitClient.instance.deleteDiet(id)
                if (response.isSuccessful) {
                    loadDiet(selectedDay)
                }
            } catch (e: Exception) { }
        }
    }

    LaunchedEffect(selectedDay) { loadDiet(selectedDay) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = PrimaryPurple, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)).padding(innerPadding).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = "Thực đơn của bạn", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day -> DayTab(day = day, isSelected = selectedDay == day, onClick = { selectedDay = day }) }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F2))) {
                Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(text = "$totalCalories Kcal", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
                        Text(text = "Tổng lượng calo hiện tại", fontSize = 12.sp, color = TextGray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Pro: 150g", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Carb: 200g", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PrimaryPurple) }
            } else {
                MealSection("Bữa Sáng", meals.filter { it.title.contains("sáng", ignoreCase = true) }, onDeleteMeal = { deleteMeal(it) })
                MealSection("Bữa Trưa", meals.filter { it.title.contains("trưa", ignoreCase = true) }, onDeleteMeal = { deleteMeal(it) })
                MealSection("Bữa Phụ", meals.filter { it.title.contains("phụ", ignoreCase = true) }, onDeleteMeal = { deleteMeal(it) })
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Thêm món cho $selectedDay") },
            text = {
                Column {
                    OutlinedTextField(value = newMealTitle, onValueChange = { newMealTitle = it }, label = { Text("Tên món ăn") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMealCalo, onValueChange = { newMealCalo = it }, label = { Text("Số Calo") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loại bữa:", fontWeight = FontWeight.Bold)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = mealType == "Bữa Sáng", onClick = { mealType = "Bữa Sáng" }); Text("Sáng") }
                        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = mealType == "Bữa Trưa", onClick = { mealType = "Bữa Trưa" }); Text("Trưa") }
                        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = mealType == "Bữa Phụ", onClick = { mealType = "Bữa Phụ" }); Text("Phụ") }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newMealTitle.isNotBlank()) {
                        scope.launch {
                            val response = RetrofitClient.instance.addDiet(DietSuggestion(title = "$mealType: $newMealTitle", calories = newMealCalo.toIntOrNull() ?: 0, mealType = selectedDay))
                            if (response.isSuccessful) { loadDiet(selectedDay); showDialog = false; newMealTitle = ""; newMealCalo = "" }
                        }
                    }
                }) { Text("Lưu") }
            }
        )
    }
}