package com.example.silpa.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.launch
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont

import com.example.silpa.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, sessionManager: SessionManager) {
    val context = LocalContext.current

    // State data dashboard dari backend
    var stats by remember { mutableStateOf<AdminDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Backend mengembalikan AdminDashboardDto langsung
            val response = RetrofitInstance.getApi(context).getAdminDashboard()
            stats = response
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Panel Admin",
                canNavigateBack = false
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(SurfaceWhite)
            ) {
                // Header dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MainBlue)
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                ) {
                    Column {
                        Text(
                            "Dashboard Admin",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SurfaceWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Kelola semua perizinan mahasiswa",
                            fontSize = 13.sp,
                            color = SurfaceWhite.copy(alpha = 0.85f)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- RINGKASAN DATA ---
                    Text("Ringkasan Aktivitas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Pengajuan
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stats?.totalPengajuanSemuaWaktu?.toString() ?: "0",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MainBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Total Izin",
                                    fontSize = 11.sp,
                                    color = MainBlue.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Menunggu Validasi
                        val menunggu = stats?.pengajuanPerluDiproses?.size ?: 0
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = menunggu.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningYellow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Perlu Validasi",
                                    fontSize = 11.sp,
                                    color = WarningYellow.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Izin Hari Ini
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stats?.pengajuanHariIni?.toString() ?: "0",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Masuk Hari Ini",
                                    fontSize = 11.sp,
                                    color = SuccessGreen.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // --- MENU UTAMA GRID ---
                    Text("Menu Utama", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)

                    val menuItems = listOf(
                        Triple("Validasi Izin", Icons.Default.AssignmentTurnedIn, "admin_validasi_list"),
                        Triple("Daftar Mahasiswa", Icons.Default.People, "admin_mahasiswa"),
                        Triple("Statistik Data", Icons.Default.BarChart, "admin_statistik"),
                        Triple("Notifikasi", Icons.Default.Notifications, "admin_notifications"),
                    )

                    // Grid Menu (2 Kolom)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        menuItems.chunked(2).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                rowItems.forEach { item ->
                                    AdminMenuCard(item.first, item.second, Modifier.weight(1f)) {
                                        navController.navigate(item.third)
                                    }
                                }
                                // Spacer jika item ganjil agar rapi
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

