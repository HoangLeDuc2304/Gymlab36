package com.example.gymlab.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.api.AddWeightRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.WeightRecordApi
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

@Composable
fun ActivityScreen(
    userId: Int, // Thêm tham số userId thực tế
    onBackClick: () -> Unit
) {
    var newWeightInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val historyList = remember { mutableStateListOf<WeightRecordApi>() }
    var isLoading by remember { mutableStateOf(false) }

    // Tự động cập nhật cân nặng hiển thị ở ô tím từ bản ghi đầu tiên trong lịch sử
    val currentWeightDisplay by remember {
        derivedStateOf {
            if (historyList.isNotEmpty()) historyList.first().weight.toString() else "0.0"
        }
    }

    fun loadWeightHistory() {
        isLoading = true
        scope.launch {
            try {
                val response = RetrofitClient.instance.getWeightHistory(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    historyList.clear()
                    response.body()?.data?.let { historyList.addAll(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) { // Tải lại khi userId thay đổi
        loadWeightHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Chỉ số cơ thể",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            IconButton(onClick = { /* TODO */ }) {
                Icon(imageVector = Icons.Default.MoreHoriz, contentDescription = "More")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Weight Card (Ô MÀU TÍM)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryPurple)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cân nặng hiện tại",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "$currentWeightDisplay kg",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Input section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newWeightInput,
                onValueChange = { newWeightInput = it },
                placeholder = { Text("Nhập số cân mới (VD: 66.2)", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = PrimaryPurple,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    val weightValue = newWeightInput.toFloatOrNull()
                    if (weightValue != null) {
                        scope.launch {
                            try {
                                val response = RetrofitClient.instance.addWeight(
                                    AddWeightRequest(weightValue, userId)
                                )
                                if (response.isSuccessful) {
                                    newWeightInput = ""
                                    loadWeightHistory() // Cập nhật lại danh sách và ô màu tím
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(text = "Lưu", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Biểu đồ tiến độ",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            if (historyList.isNotEmpty()) {
                // Đảo ngược danh sách để vẽ biểu đồ từ cũ đến mới (trái sang phải)
                WeightChart(historyList.reversed())
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có dữ liệu biểu đồ", color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Lịch sử gần đây",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            historyList.forEachIndexed { index, record ->
                val previousWeight = if (index < historyList.size - 1) historyList[index + 1].weight else record.weight
                val diff = record.weight - previousWeight

                HistoryItem(
                    date = record.recordedDate?.take(10) ?: "N/A",
                    weight = "${record.weight} kg",
                    change = String.format("%.1fkg", abs(diff)),
                    isDown = diff <= 0
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun HistoryItem(date: String, weight: String, change: String, isDown: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = date, fontSize = 16.sp, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = weight, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isDown) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (isDown) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = change,
                    fontSize = 14.sp,
                    color = if (isDown) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun WeightChart(data: List<WeightRecordApi>) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontSize = 10.sp,
        color = Color.Gray
    )

    Canvas(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 24.dp, top = 24.dp, bottom = 40.dp)) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingBottom = 20.dp.toPx()
        val chartWidth = width - paddingLeft
        val chartHeight = height - paddingBottom

        if (data.isEmpty()) return@Canvas

        val maxWeight = (data.maxByOrNull { it.weight }?.weight ?: 100f) + 1f
        val minWeight = (data.minByOrNull { it.weight }?.weight ?: 0f) - 1f
        val range = if (maxWeight == minWeight) 1f else maxWeight - minWeight

        val steps = 5
        for (i in 0..steps) {
            val y = chartHeight - (i.toFloat() / steps) * chartHeight
            val label = String.format("%.1f", minWeight + (i.toFloat() / steps) * range)

            drawLine(
                color = Color(0xFFEEEEEE),
                start = Offset(paddingLeft, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                style = textStyle,
                topLeft = Offset(0f, y - 7.dp.toPx())
            )
        }

        val points = data.mapIndexed { index, record ->
            val x = paddingLeft + (index.toFloat() / (if (data.size > 1) data.size - 1 else 1)) * chartWidth
            val y = chartHeight - ((record.weight - minWeight) / range) * chartHeight
            Offset(x, y)
        }

        if (points.size > 1) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            val fillPath = Path().apply {
                addPath(path)
                lineTo(points.last().x, chartHeight)
                lineTo(points.first().x, chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryPurple.copy(alpha = 0.2f), Color.Transparent)
                )
            )

            drawPath(
                path = path,
                color = PrimaryPurple,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        points.forEachIndexed { index, offset ->
            drawCircle(PrimaryPurple, radius = 4.dp.toPx(), center = offset)
            drawCircle(Color.White, radius = 2.dp.toPx(), center = offset)

            // Hiển thị nhãn ngày dưới trục X (định dạng MM-DD)
            val fullDate = data[index].recordedDate ?: ""
            if (fullDate.length >= 10) {
                val dateLabel = fullDate.substring(5, 10).replace("-", "/")
                
                // Chỉ hiển thị nhãn nếu không quá dày đặc
                if (data.size <= 7 || index % (data.size / 5 + 1) == 0 || index == data.size - 1) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = dateLabel,
                        style = textStyle,
                        topLeft = Offset(offset.x - 12.dp.toPx(), chartHeight + 10.dp.toPx())
                    )
                }
            }
        }
    }
}
