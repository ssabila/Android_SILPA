package com.example.silpa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.NotifikasiDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current
    var notifList by remember { mutableStateOf<List<NotifikasiDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.getApi(context).getNotifikasi()
            if (response.berhasil && response.data != null) {
                notifList = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Notifikasi",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else if (notifList.isEmpty()) {
            // Tampilan Kosong
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada notifikasi", color = TextGray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifList.sortedByDescending { it.id }) { notif ->
                    NotifCardItem(notif)
                }
            }
        }
    }
}

@Composable
fun NotifCardItem(notif: NotifikasiDto) {
    val cardBgColor = if (notif.sudahDibaca) SurfaceWhite else MainBlue.copy(alpha = 0.05f)
    val iconTint = if (notif.sudahDibaca) TextGray else MainBlue
    val iconBg = if (notif.sudahDibaca) BorderGray.copy(alpha = 0.3f) else MainBlue.copy(alpha = 0.1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if(notif.sudahDibaca) BorderGray else MainBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Notifikasi Bulat
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (notif.sudahDibaca) Icons.Default.NotificationsNone else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Info Perizinan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if(notif.sudahDibaca) TextBlack else MainBlue
                    )

                    // Waktu (Tanggal)
                    Text(
                        text = notif.waktu.take(10), // Ambil YYYY-MM-DD saja
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notif.pesan,
                    fontSize = 13.sp,
                    color = if(notif.sudahDibaca) TextGray else TextBlack,
                    lineHeight = 18.sp
                )
            }

            // Indikator Belum Dibaca (Titik Biru Kecil)
            if (!notif.sudahDibaca) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AlertRed, CircleShape)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}