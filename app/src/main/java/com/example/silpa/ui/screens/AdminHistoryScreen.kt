package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*
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
                    .padding(16.dp)
            ) {
                if (listIzin.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada riwayat perizinan.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Urutkan berdasarkan ID descending (terbaru di atas) atau tanggal jika ada
                        items(listIzin.sortedByDescending { it.id }) { izin ->
                            AdminHistoryItem(izin) {
                                // Navigasi ke detail izin
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
fun AdminHistoryItem(izin: PerizinanDto, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = izin.mahasiswaNama ?: "Mahasiswa",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MainBlue
                )
                BadgeStatus(izin.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Jenis Izin", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "${izin.jenisIzin} - ${izin.detailIzin}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tanggal Mulai", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = izin.tanggalMulai,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            if (!izin.catatanAdmin.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${izin.catatanAdmin}",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.DarkGray
                )
            }
        }
    }
}
