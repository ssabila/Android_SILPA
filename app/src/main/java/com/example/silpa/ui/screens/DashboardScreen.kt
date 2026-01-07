package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
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
import com.example.silpa.model.MahasiswaDashboardDto
import com.example.silpa.model.PerizinanDto
import com.example.silpa.model.ProfilPenggunaDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    var dashboardData by remember { mutableStateOf<MahasiswaDashboardDto?>(null) }
    var userProfile by remember { mutableStateOf<ProfilPenggunaDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val api = RetrofitInstance.getApi(context)
            // Load profil & dashboard
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
            Toast.makeText(context, "Gagal memuat data: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                .show()
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
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    modifier = Modifier.fillMaxSize()
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
                            .padding(vertical = 32.dp, horizontal = 24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Halo, ${
                                    userProfile?.namaLengkap?.split(" ")?.first() ?: "Mahasiswa"
                                }!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Statistik Cards - overlapping header
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .fillMaxWidth()
                            .offset(y = (-16).dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Izin
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MainBlue),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dashboardData?.totalIzinDiajukan?.toString() ?: "0",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MainBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total Izin",
                                    fontSize = 12.sp,
                                    color = MainBlue.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Perlu Revisi
                        val jumlahRevisi =
                            dashboardData?.breakdownPerStatus?.get("PERLU_REVISI") ?: 0
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, WarningYellow),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = jumlahRevisi.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningYellow
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Perlu Revisi",
                                    fontSize = 12.sp,
                                    color = WarningYellow.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Alert jika ada revisi
                    if (dashboardData?.adaPerluRevisi == true) {
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .fillMaxWidth()
                                .clickable { navController.navigate("history") },
                            colors = CardDefaults.cardColors(
                                containerColor = WarningYellow.copy(
                                    alpha = 0.08f
                                )
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                WarningYellow.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            WarningYellow.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        tint = WarningYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Ada pengajuan yang perlu direvisi",
                                        color = WarningYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Klik untuk melihat detail",
                                        color = WarningYellow.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Riwayat Terbaru",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        fontWeight = FontWeight.Bold,
                        color = TextBlack,
                        fontSize = 18.sp
                    )

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val list = dashboardData?.riwayat5IzinTerakhir ?: emptyList()
                        if (list.isEmpty()) {
                            item {
                                Text(
                                    "Belum ada pengajuan izin.",
                                    color = TextGray,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            items(list) { izin ->
                                DashboardPermissionItem(izin) {
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

// Helper Components

@Composable
fun DashboardPermissionItem(izin: PerizinanDto, onClick: () -> Unit) {
    val statusColor = when (izin.status) {
            "DISETUJUI" -> SuccessGreen
            "DITOLAK" -> AlertRed
            "PERLU_REVISI" -> WarningYellow
            else -> AccentPurple // DIAJUKAN
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                statusColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = izin.jenisIzin.replace("_", " "),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextBlack
                    )
                    Text(
                        text = izin.tanggalMulai,
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = izin.status.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

