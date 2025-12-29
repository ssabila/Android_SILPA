package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var listIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // Mengambil riwayat izin milik user yang sedang login
            listIzin = RetrofitInstance.getApi(context).getRiwayatIzin()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Riwayat Perizinan",
                canNavigateBack = false
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceWhite)
                    .padding(padding)
            ) {
                if (listIzin.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Belum ada riwayat perizinan.", color = TextGray, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ajukan izin baru untuk memulai.", fontSize = 12.sp, color = TextGray)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        // Urutkan dari yang terbaru
                        items(listIzin.sortedByDescending { it.id }) { izin ->
                            HistoryItem(izin){
                                navController.navigate("detail_izin/${izin.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(izin: PerizinanDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp)
    ) {
        // Baris Atas: Jenis Izin & Status
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = izin.jenisIzin.replace("_", " "),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = izin.detailIzin.replace("_", " "),
                    fontSize = 13.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            BadgeStatus(izin.status)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info Tanggal & Deskripsi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tanggal Mulai", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                Text(
                    text = izin.tanggalMulai,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextBlack
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Alasan", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                Text(
                    text = izin.deskripsi,
                    fontSize = 13.sp,
                    color = TextBlack,
                    maxLines = 1
                )
            }
        }

        // Tampilkan Catatan Admin jika ada (Penting untuk Status Ditolak/Revisi)
        if (!izin.catatanAdmin.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = WarningYellow.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Catatan Admin:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = izin.catatanAdmin,
                        fontSize = 12.sp,
                        color = TextGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
        
        Divider(color = BorderGray, thickness = 1.dp)
    }
}