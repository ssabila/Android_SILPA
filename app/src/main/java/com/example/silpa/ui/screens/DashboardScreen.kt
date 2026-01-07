package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.MahasiswaDashboardDto
import com.example.silpa.model.PerizinanDto
import com.example.silpa.model.ProfilPenggunaDto
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import com.example.silpa.ui.components.*

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var dashboardData by remember { mutableStateOf<MahasiswaDashboardDto?>(null) }
    var userProfile by remember { mutableStateOf<ProfilPenggunaDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val api = RetrofitInstance.getApi(context)
            // Load profil & dashboard secara parallel idealnya, tapi sequential cukup utk simple app
            val profileRes = api.getProfil()
            if (profileRes.berhasil) {
                userProfile = profileRes.data
            }
            dashboardData = api.getDashboard()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
            // Toast.makeText(context, "Gagal memuat data: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("submit_izin") },
                containerColor = MainBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajukan Izin")
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            // Gunakan Box untuk menumpuk Header di belakang konten
            Box(modifier = Modifier.fillMaxSize()) {
                // --- HEADER GRADASI SEPERTI ADMIN DASHBOARD ---
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
                    // Pattern Lingkaran Dekoratif
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

                    // Konten Header (Judul Dashboard Mahasiswa)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dashboard Mahasiswa",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Halo, ${userProfile?.namaLengkap ?: "Mahasiswa"}!",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
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
                            Text("Ringkasan Izin Anda", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Total Izin
                                StatCard(
                                    title = "Total Izin",
                                    value = dashboardData?.totalIzinDiajukan?.toString() ?: "0",
                                    bgColor = BackgroundLight,
                                    accentColor = MainBlue,
                                    modifier = Modifier.weight(1f)
                                )

                                // Perlu Revisi
                                val jumlahRevisi = dashboardData?.breakdownPerStatus?.get("PERLU_REVISI") ?: 0
                                StatCard(
                                    title = "Perlu Revisi",
                                    value = jumlahRevisi.toString(),
                                    bgColor = BackgroundLight,
                                    accentColor = WarningYellow,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Alert jika ada revisi
                    if (dashboardData?.adaPerluRevisi == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("history") },
                            colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellow),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = WarningYellow)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Perlu Tindakan", color = WarningYellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Ada pengajuan yang perlu direvisi. Ketuk untuk melihat.", fontSize = 12.sp, color = TextGray)
                                }
                            }
                        }
                    }

                    // --- RIWAYAT TERBARU ---
                    Text(
                        text = "Riwayat Terbaru",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        fontSize = 18.sp
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp), // Extra padding for FAB
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val list = dashboardData?.riwayat5IzinTerakhir ?: emptyList()
                        if (list.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Belum ada pengajuan izin.", color = TextGray)
                                }
                            }
                        } else {
                            items(list) { izin ->
                                HistoryItem(izin) {
                                    navController.navigate("detail_izin/${izin.id}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
