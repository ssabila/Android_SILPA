package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.AdminDashboardDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.components.*
import kotlinx.coroutines.launch

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
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            // Gunakan Box untuk menumpuk Header di belakang konten
            Box(modifier = Modifier.fillMaxSize()) {
                // --- HEADER GRADASI SEPERTI LANDING PAGE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp) // Tinggi header
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MainBlue,
                                    Color(0xFF64B5F6), // Biru lebih muda
                                    Color.White        // Putih di bawah
                                )
                            )
                        )
                ) {
                    // Pattern Lingkaran Dekoratif (Optional, agar sama persis dengan landing)
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

                    // Konten Header (Judul Panel Admin)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 32.dp), // Angkat sedikit agar tidak tertutup card
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

                // Tombol Logout di pojok kanan atas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, end = 16.dp), // Sesuaikan padding status bar
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        sessionManager.clearSession()
                        navController.navigate("landing") { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    }
                }

                // Konten Utama (di depan header)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 220.dp) // Turunkan konten agar mulai dari bawah judul header
                        .padding(horizontal = 16.dp)
                        .padding(bottom = padding.calculateBottomPadding()), // Padding bawah navbar
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- RINGKASAN DATA ---
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

                    // --- MENU UTAMA GRID ---
                    Text("Menu Utama", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 18.sp)

                    val menuItems = listOf(
                        Triple("Validasi Izin", Icons.Default.AssignmentTurnedIn, "admin_validasi_list"),
                        Triple("Daftar Mahasiswa", Icons.Default.People, "admin_mahasiswa"),
                        Triple("Statistik Data", Icons.Default.BarChart, "admin_statistik"),
                        Triple("Notifikasi", Icons.Default.Notifications, "admin_notifications"),
                        Triple("Profil Saya", Icons.Default.Person, "admin_profile"),
                    )

                    // Grid Menu (2 Kolom) - Menggunakan Column manual
                    val rows = menuItems.chunked(2)
                    rows.forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->
                                MenuCard(item.first, item.second, Modifier.weight(1f)) {
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

// --- Component Helper ---
