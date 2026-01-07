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
                title = "",
                canNavigateBack = false
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Background dengan gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF5F7FF),
                                    Color(0xFFEEF2FF),
                                    Color(0xFFE8EFFF)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header dengan rounded bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(MainBlue, Color(0xFF4A90E2))
                                ),
                                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .padding(top = 24.dp, bottom = 48.dp, start = 24.dp, end = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Logo
                            Image(
                                painter = painterResource(id = R.drawable.silpafix),
                                contentDescription = "SILPA Logo",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Judul Dashboard
                            Text(
                                "Dashboard Admin",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Welcome text
                            Text(
                                "Halo Admin!",
                                fontSize = 14.sp,
                                color = SurfaceWhite.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- CARD RINGKASAN GABUNGAN ---
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-32).dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MainBlue),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Total Izin
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stats?.totalPengajuanSemuaWaktu?.toString() ?: "0",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Total Izin",
                                        fontSize = 12.sp,
                                        color = MainBlue.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Divider
                                Divider(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(60.dp)
                                        .padding(vertical = 8.dp),
                                    color = MainBlue.copy(alpha = 0.2f)
                                )

                                // Perlu Validasi
                                val menunggu = stats?.pengajuanPerluDiproses?.size ?: 0
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = menunggu.toString(),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Perlu Validasi",
                                        fontSize = 12.sp,
                                        color = MainBlue.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Divider
                                Divider(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(60.dp)
                                        .padding(vertical = 8.dp),
                                    color = MainBlue.copy(alpha = 0.2f)
                                )

                                // Hari Ini
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stats?.pengajuanHariIni?.toString() ?: "0",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MainBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Hari Ini",
                                        fontSize = 12.sp,
                                        color = MainBlue.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // --- MENU UTAMA GRID ---
                        Text(
                            "Menu Utama",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextBlack
                        )

                        val menuItems = listOf(
                            Triple(
                                "Validasi Izin",
                                Icons.Default.AssignmentTurnedIn,
                                "admin_validasi_list"
                            ),
                            Triple("Daftar Mahasiswa", Icons.Default.People, "admin_mahasiswa"),
                            Triple("Statistik Data", Icons.Default.BarChart, "admin_statistik"),
                            Triple(
                                "Notifikasi",
                                Icons.Default.Notifications,
                                "admin_notifications"
                            ),
                        )

                        // Grid Menu (2 Kolom)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            menuItems.chunked(2).forEach { rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEach { item ->
                                        AdminMenuCard(
                                            item.first,
                                            item.second,
                                            Modifier.weight(1f)
                                        ) {
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
}

