package com.example.silpa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.AdminDashboardDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, sessionManager: SessionManager) {
    val context = LocalContext.current

    var stats by remember { mutableStateOf<AdminDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.getApi(context).getAdminDashboard()
            stats = response
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MainBlue,
                                    Color(0xFF64B5F6),
                                    Color.White
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .offset(x = (-50).dp, y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 30.dp, y = 30.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(80.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(3.dp, Color.White)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.silpafix),
                                contentDescription = "Logo SILPA",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Panel Admin",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Kelola data perizinan mahasiswa",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        sessionManager.clearSession()
                        navController.navigate("landing") { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 220.dp)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = padding.calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //  RINGKASAN DATA
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Ringkasan Aktivitas", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Total Pengajuan
                                StatCard(
                                    title = "Total Izin",
                                    value = stats?.totalPengajuanSemuaWaktu?.toString() ?: "0",
                                    bgColor = BackgroundLight,
                                    accentColor = MainBlue,
                                    modifier = Modifier.weight(1f)
                                )

                                // Menunggu Validasi
                                val menunggu = stats?.pengajuanPerluDiproses?.size ?: 0
                                StatCard(
                                    title = "Perlu Validasi",
                                    value = menunggu.toString(),
                                    bgColor = BackgroundLight,
                                    accentColor = WarningYellow,
                                    modifier = Modifier.weight(1f)
                                )

                                // Izin Hari Ini
                                StatCard(
                                    title = "Masuk Hari Ini",
                                    value = stats?.pengajuanHariIni?.toString() ?: "0",
                                    bgColor = BackgroundLight,
                                    accentColor = SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    //  MENU UTAMA GRID
                    Text("Menu Utama", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 18.sp)

                    val menuItems = listOf(
                        Triple("Validasi Izin", Icons.Default.AssignmentTurnedIn, "admin_validasi_list"),
                        Triple("Daftar Mahasiswa", Icons.Default.People, "admin_mahasiswa"),
                        Triple("Statistik Data", Icons.Default.BarChart, "admin_statistik"),
                        Triple("Notifikasi", Icons.Default.Notifications, "admin_notifications"),
                        Triple("Profil Saya", Icons.Default.Person, "admin_profile"),
                    )
                    val rows = menuItems.chunked(2)
                    rows.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->
                                MenuCard(item.first, item.second, Modifier.weight(1f)) {
                                    navController.navigate(item.third)
                                }
                            }
                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
