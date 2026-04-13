package com.example.gymlab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gymlab.api.DietSuggestion
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.ui.theme.PrimaryPurple
import com.example.gymlab.ui.theme.TextGray
import kotlinx.coroutines.launch

@Composable
fun DietScreen(userId: Int, onBackClick: () -> Unit) {
    var selectedDay by remember { mutableStateOf("T3") }
    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7")

    var showDialog by remember { mutableStateOf(false) }
    var newMealTitle by remember { mutableStateOf("") }
    var newMealCalo by remember { mutableStateOf("") }
    var newMealImageUrl by remember { mutableStateOf("") }
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
                MealSection("Bữa Sáng", meals.filter { it.mealType == "Breakfast" }, onDeleteMeal = { deleteMeal(it) })
                MealSection("Bữa Trưa", meals.filter { it.mealType == "Lunch" }, onDeleteMeal = { deleteMeal(it) })
                MealSection("Bữa Phụ", meals.filter { it.mealType == "Snack" }, onDeleteMeal = { deleteMeal(it) })
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newMealImageUrl, onValueChange = { newMealImageUrl = it }, label = { Text("Link hình ảnh món ăn") }, modifier = Modifier.fillMaxWidth())
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
                            val dietRequest = DietSuggestion(
                                title = newMealTitle,
                                calories = newMealCalo.toIntOrNull() ?: 0,
                                thumbnailUrl = if(newMealImageUrl.isBlank()) null else newMealImageUrl,
                                mealType = if(mealType == "Bữa Sáng") "Breakfast" else if(mealType == "Bữa Trưa") "Lunch" else "Snack",
                                dayOfWeek = selectedDay
                            )
                            val response = RetrofitClient.instance.addDiet(dietRequest)
                            if (response.isSuccessful) {
                                loadDiet(selectedDay)
                                showDialog = false
                                newMealTitle = ""
                                newMealCalo = ""
                                newMealImageUrl = ""
                            }
                        }
                    }
                }) { Text("Lưu") }
            }
        )
    }
}

@Composable
fun DayTab(day: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSelected) PrimaryPurple else Color.White).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = day, color = if (isSelected) Color.White else TextGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MealSection(title: String, meals: List<DietSuggestion>, onDeleteMeal: (Int) -> Unit) {
    Column {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(12.dp))
        if (meals.isEmpty()) {
            Text(text = "Chưa có món ăn", fontSize = 14.sp, color = Color.LightGray)
        } else {
            meals.forEach { meal ->
                MealItem(meal, onDeleteMeal)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MealItem(meal: DietSuggestion, onDeleteMeal: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh món ăn (Dùng Coil để load)
            AsyncImage(
                model = meal.thumbnailUrl ?: "https://cdn-icons-png.flaticon.com/512/706/706164.png",
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = meal.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${meal.calories} Kcal", fontSize = 14.sp, color = TextGray)
            }

            IconButton(onClick = { meal.id?.let { onDeleteMeal(it) } }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
