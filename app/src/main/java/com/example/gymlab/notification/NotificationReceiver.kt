//package com.example.gymlab.notification
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import com.example.gymlab.R
//
//class NotificationReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "gymlab_reminder"
//
//        // Tạo Channel cho Android 8.0+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, "Nhắc nhở tập luyện", NotificationManager.IMPORTANCE_HIGH)
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(context, channelId)
//            .setContentTitle("Gymlab nhắc bạn!")
//            .setContentText("Đến giờ tập rồi Nam ơi! Cháy hết mình hôm nay nhé 🔥")
//            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Thay bằng icon app của bạn sau
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .build()
//
//        manager.notify(1, notification)
//    }
//}

package com.example.gymlab.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "gymlab_reminder"

        // 1. Tạo Channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Nhắc nhở tập luyện", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        // 2. Build thông báo
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Gymlab nhắc bạn!")
            .setContentText("Đến giờ tập rồi Nam ơi! Cháy hết mình hôm nay nhé 🔥")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Nhớ thay bằng icon app của bạn (VD: R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 3. Hiển thị thông báo
        manager.notify(1, notification)

        // ==========================================
        // 4. LOGIC TỰ ĐỘNG LẶP LẠI (XUYÊN DOZE MODE)
        // ==========================================
        val repeatOption = intent.getStringExtra("REPEAT_OPTION") ?: "Một lần"

        if (repeatOption != "Một lần") {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Tạo lại Intent mang theo dữ liệu lặp lại
            val nextIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("REPEAT_OPTION", repeatOption)
            }

            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                nextIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Cộng thêm ngày tùy theo lựa chọn
            val nextCalendar = Calendar.getInstance().apply {
                if (repeatOption == "Hàng ngày") {
                    add(Calendar.DATE, 1)
                } else if (repeatOption == "Hàng tuần") {
                    add(Calendar.DATE, 7)
                }
            }

            // Tiếp tục đặt báo thức cho lần kế tiếp
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextCalendar.timeInMillis,
                nextPendingIntent
            )
        }
    }
}