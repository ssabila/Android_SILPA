package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.NotifikasiDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    var notifList by remember { mutableStateOf<List<NotifikasiDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            // Perubahan: Menghandle ApiResponse
            val response = RetrofitInstance.getApi(context).getNotifikasi()
            if (response.berhasil && response.data != null) {
                notifList = response.data
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Notifikasi" ,
                canNavigateBack = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SurfaceWhite)
        ) {
            if (notifList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MainBlue.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = MainBlue, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada notifikasi", color = TextGray, fontSize = 15.sp)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)) {
                    items(notifList) { notif ->
                        NotifItem(notif) {
                            // Jika ada ID izin, navigasi ke detail
                            if (notif.id != null) {
                                navController.navigate("detail_izin/${notif.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotifItem(notif: NotifikasiDto, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notif.pesan,
                    fontWeight = if (notif.sudahDibaca) FontWeight.Medium else FontWeight.SemiBold,
                    color = TextBlack,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    notif.waktu.take(10),
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        Divider(color = BorderGray, thickness = 1.dp)
    }
}
