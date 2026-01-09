package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.silpa.ui.components.*
import com.example.silpa.ui.theme.*
import com.example.silpa.utils.toReadableFormat

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
        },
        containerColor = BackgroundLight
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                //  KARTU UTAMA INFORMASI
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MainBlue), // Border Biru
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header: Status Pengajuan
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Status Pengajuan", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)
                            BadgeStatus(izin.status)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderGray)

                        // Body: Detail Informasi
                        Text("Informasi Izin", fontWeight = FontWeight.SemiBold, color = TextBlack, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("Jenis Izin", izin.jenisIzin.toReadableFormat())
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Detail", izin.detailIzin.toReadableFormat())
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Tanggal Mulai", izin.tanggalMulai)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Alasan:", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = izin.deskripsi,
                            fontSize = 14.sp,
                            color = TextBlack,
                            lineHeight = 20.sp
                        )

                        // Footer: Catatan Admin
                        if (!izin.catatanAdmin.isNullOrEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderGray)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = WarningYellow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Catatan Admin", fontWeight = FontWeight.SemiBold, color = TextBlack, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Tampilkan catatan dalam box agar lebih jelas
                            Surface(
                                color = BackgroundLight,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = izin.catatanAdmin,
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 14.sp,
                                    color = TextBlack
                                )
                            }
                        }
                    }
                }

                if (!izin.daftarSesi.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderGray),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Jadwal Sesi Izin", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            izin.daftarSesi.forEachIndexed { index, sesi ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sesi.tanggal, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextBlack)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(sesi.namaMataKuliah, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MainBlue)
                                        Text(sesi.namaDosen, fontSize = 12.sp, color = TextGray)
                                    }
                                }
                                if (index < izin.daftarSesi.size - 1) {
                                    Divider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }

                //  TOMBOL AKSI
                if (izin.status == "PERLU_REVISI") {
                    Button(
                        onClick = { navController.navigate("revisi_izin/${izin.id}") },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LAKUKAN REVISI SEKARANG", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else if (izin.status == "DIAJUKAN") {
                    // Info text saja jika masih diajukan
                    Text(
                        text = "Pengajuan sedang dalam proses validasi oleh admin.",
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Data perizinan tidak ditemukan", color = TextGray)
            }
        }
    }
}