package com.example.gymlab.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymlab.notification.NotificationReceiver // Đảm bảo đúng package này
import com.example.gymlab.ui.theme.PrimaryPurple
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    userId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt nhắc nhở", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
        ) {
            // Mục: Bật/Tắt thông báo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = PrimaryPurple)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Nhắc nhở tập luyện hàng ngày", modifier = Modifier.weight(1f), fontSize = 16.sp)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mục: Chọn thời gian
            if (isEnabled) {
                Card(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                reminderTime = String.format("%02d:%02d", hour, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = PrimaryPurple)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Thời gian nhắc nhở", fontSize = 14.sp, color = Color.Gray)
                            Text(reminderTime, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isEnabled) {
                        // Lên lịch thông báo
                        scheduleNotification(context, reminderTime)
                        android.widget.Toast.makeText(context, "Đã đặt nhắc nhở lúc $reminderTime", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        // Hủy thông báo nếu người dùng tắt Switch
                        cancelNotification(context)
                        android.widget.Toast.makeText(context, "Đã tắt nhắc nhở", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text("LƯU CÀI ĐẶT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- HÀM HỖ TRỢ LÊN LỊCH ---
private fun scheduleNotification(context: Context, timeString: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // KIỂM TRA QUYỀN (Dành cho Android 12+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            // Nếu chưa có quyền, mở cài đặt hệ thống để người dùng cho phép
            val intent = Intent().apply {
                action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            }
            context.startActivity(intent)
            android.widget.Toast.makeText(context, "Vui lòng cho phép quyền Báo thức chính xác để nhận thông báo!", android.widget.Toast.LENGTH_LONG).show()
            return // Dừng lại, không chạy lệnh setExact bên dưới để tránh crash
        }
    }

    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        101,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val timeParts = timeString.split(":")
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        set(Calendar.MINUTE, timeParts[1].toInt())
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DATE, 1)
        }
    }

    // Đặt báo thức
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
}

// --- HÀM HỖ TRỢ HỦY LỊCH ---
private fun cancelNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        101,
        intent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE else PendingIntent.FLAG_UPDATE_CURRENT
    )
    if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
    }
}