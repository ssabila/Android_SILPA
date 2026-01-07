package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.silpa.model.PerizinanDto
import com.example.silpa.ui.components.BadgeStatus
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var listIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Fetch semua data perizinan
            listIzin = RetrofitInstance.getApi(context).getSemuaPerizinan()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Riwayat Semua Perizinan",
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                if (listIzin.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat perizinan.", color = TextGray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // URUTKAN DARI YANG PALING BARU (ID DESCENDING)
                        items(listIzin.sortedByDescending { it.id }) { izin ->
                            AdminHistoryItemColored(izin) {
                                // Navigasi ke detail (validasi) screen jika diklik
                                navController.navigate("admin_validasi/${izin.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHistoryItemColored(izin: PerizinanDto, onClick: () -> Unit) {
    // 1. Tentukan Warna Background berdasarkan Jenis Izin
    val cardBgColor = when (izin.jenisIzin) {
        "SAKIT" -> SurfaceWhite
        "DISPENSASI_INSTITUSI", "DISPENSASI" -> SurfaceWhite
        "IZIN_ALASAN_PENTING" -> SurfaceWhite
        else -> SurfaceWhite
    }

    // 2. Tentukan Warna Border (sedikit lebih gelap/transparan dari warna utama tema)
    val borderColor = when (izin.jenisIzin) {
        "SAKIT" -> IzinSakitColor.copy(alpha = 0.5f)
        "DISPENSASI_INSTITUSI", "DISPENSASI" -> IzinDispensasiColor.copy(alpha = 0.5f)
        "IZIN_ALASAN_PENTING" -> IzinPentingColor.copy(alpha = 0.5f)
        else -> BorderGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Nama & Status
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = izin.mahasiswaNama ?: "Mahasiswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextBlack
                )
                BadgeStatus(izin.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body: Jenis & Tanggal
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Label Jenis Izin Kecil
                Surface(
                    color = SurfaceWhite.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = izin.jenisIzin.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                }
                Text(
                    text = izin.detailIzin.replace("_", " "),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextBlack
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Mulai: ${izin.tanggalMulai}", fontSize = 12.sp, color = TextGray)

            // Catatan Admin (Jika ada)
            if (!izin.catatanAdmin.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = borderColor, thickness = 0.5.dp) // Divider warna sesuai border
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${izin.catatanAdmin}",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = TextGray
                )
            }
        }
    }
}
