package com.example.gymlab.ui

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gymlab.api.RetrofitClient
import com.example.gymlab.api.UpdateAvatarRequest
import com.example.gymlab.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

// Hàm hỗ trợ chuyển đổi URI sang chuỗi Base64
fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun ProfileScreen(
    userId: Int,
    userName: String,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onProgressClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAvatarUpdated: (String) -> Unit
) {
    // Sử dụng Any? để có thể lưu Uri (local) hoặc ByteArray (Base64 từ server)
    var imageUri by remember { mutableStateOf<Any?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. Tải dữ liệu người dùng và ảnh đại diện từ Server
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.instance.getUserInfo(userId)
            if (response.isSuccessful) {
                val profileImageUrl = response.body()?.user?.profileImage
                if (!profileImageUrl.isNullOrEmpty()) {
                    if (profileImageUrl.startsWith("http") || profileImageUrl.startsWith("content")) {
                        imageUri = Uri.parse(profileImageUrl)
                    } else {
                        // Nếu không phải URL, giả định đó là chuỗi Base64 và decode sang ByteArray cho Coil
                        try {
                            imageUri = Base64.decode(profileImageUrl, Base64.DEFAULT)
                        } catch (e: Exception) {
                            imageUri = profileImageUrl
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri // Hiển thị ảnh cục bộ ngay lập tức
            
            scope.launch {
                try {
                    // Chuyển ảnh sang Base64
                    val base64Image = uriToBase64(context, uri)
                    
                    if (base64Image != null) {
                        val response = RetrofitClient.instance.updateAvatar(
                            UpdateAvatarRequest(userId, base64Image)
                        )
                        if (response.isSuccessful && response.body()?.success == true) {
                            onAvatarUpdated(uri.toString())
                            Toast.makeText(context, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Lỗi khi lưu ảnh vào server", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Không thể xử lý ảnh", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onBackClick, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
                    .clickable { 
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else if (imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default Profile",
                        tint = Color.LightGray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Thành viên Pro",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Menu Items
        ProfileMenuItem(
            icon = Icons.Default.ShowChart,
            title = "Theo dõi tiến độ (Progress)",
            onClick = onProgressClick
        )
        ProfileMenuItem(
            icon = Icons.Default.Stars,
            title = "Thành tích cá nhân",
            onClick = onAchievementsClick
        )
        ProfileMenuItem(
            icon = Icons.Default.Settings,
            title = "Cài đặt tài khoản",
            onClick = onSettingsClick
        )
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Trợ giúp & Phản hồi",
            onClick = {}
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
        ) {
            Text(
                text = "Đăng Xuất",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D1D1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
