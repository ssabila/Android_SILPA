package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
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
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailIzinScreen(navController: NavController, perizinanId: Long) {
    val context = LocalContext.current
    var data by remember { mutableStateOf<PerizinanDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(perizinanId) {
        try {
            data = RetrofitInstance.getApi(context).getPerizinanById(perizinanId)
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Detail Perizinan",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else if (data != null) {
            val izin = data!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(SurfaceWhite)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header dengan Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (izin.status) {
                                "DISETUJUI" -> SuccessGreen.copy(alpha = 0.1f)
                                "DITOLAK" -> AlertRed.copy(alpha = 0.1f)
                                "PERLU_REVISI" -> WarningYellow.copy(alpha = 0.1f)
                                else -> AccentPurple.copy(alpha = 0.1f)
                            }
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = izin.jenisIzin.replace("_", " "),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BadgeStatusLarge(izin.status)
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                    // JIKA PERLU REVISI, TAMPILKAN ALERT
                    if (izin.status == "PERLU_REVISI") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = WarningYellow.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellow.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(WarningYellow.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = WarningYellow, modifier = Modifier.size(20.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Perlu Revisi", fontWeight = FontWeight.Bold, color = WarningYellow, fontSize = 14.sp)
                                    Text(
                                        izin.catatanAdmin ?: "Mohon perbaiki data Anda.",
                                        fontSize = 12.sp,
                                        color = TextGray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("revisi_izin/${izin.id}") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lakukan Revisi", fontWeight = FontWeight.Bold)
                        }
                    } else if (izin.status == "DITOLAK" && !izin.catatanAdmin.isNullOrEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Alasan Penolakan", fontWeight = FontWeight.Bold, color = AlertRed, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(izin.catatanAdmin, fontSize = 12.sp, color = TextGray, lineHeight = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Detail Info
                    Text("Informasi Pengajuan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DetailRow("Jenis Izin", izin.jenisIzin.replace("_", " "))
                            Divider(color = BorderGray.copy(alpha = 0.2f))
                            DetailRow("Kategori", izin.detailIzin.replace("_", " "))
                            Divider(color = BorderGray.copy(alpha = 0.2f))
                            DetailRow("Tanggal Mulai", izin.tanggalMulai)
                            Divider(color = BorderGray.copy(alpha = 0.2f))
                            DetailRow("Alasan", izin.deskripsi)
                        }
                    }

                    // Info Sesi (Jika ada)
                    if (!izin.daftarSesi.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Jadwal Sesi Izin", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
                        Spacer(modifier = Modifier.height(12.dp))

                        izin.daftarSesi.forEach { sesi ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(sesi.tanggal, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MainBlue)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(sesi.namaMataKuliah, fontSize = 12.sp, color = TextBlack, fontWeight = FontWeight.Medium)
                                    Text(sesi.namaDosen, fontSize = 12.sp, color = TextGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Data tidak ditemukan") }
        }
    }
}
