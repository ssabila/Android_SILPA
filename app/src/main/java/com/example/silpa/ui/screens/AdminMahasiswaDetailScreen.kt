package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
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
import com.example.silpa.model.ProfilPenggunaDto
import com.example.silpa.ui.components.*
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMahasiswaDetailScreen(navController: NavController, mahasiswaId: Long) {
    val context = LocalContext.current

    // State Data
    var profil by remember { mutableStateOf<ProfilPenggunaDto?>(null) }
    var riwayatIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(mahasiswaId) {
        try {
            // Karena tidak ada endpoint getMahasiswaById khusus yang mengembalikan detail lengkap + history,
            // Kita lakukan 2 fetch:
            // 1. Ambil list semua mahasiswa, filter by ID untuk dapat profil dasar
            // 2. Ambil list semua izin, filter by mahasiswaId untuk dapat history
            // (Idealnya Backend punya endpoint: GET /api/admin/mahasiswa/{id} -> MahasiswaDetailAdminDto)

            val allMhs = RetrofitInstance.getApi(context).getAllMahasiswa()
            profil = allMhs.find { it.id == mahasiswaId }

            val allIzin = RetrofitInstance.getApi(context).getSemuaPerizinan()
            // Asumsi PerizinanDto punya field mahasiswaId atau kita filter berdasarkan nama jika ID tidak ada di DTO list
            // Di model PerizinanDto Anda ada field 'mahasiswaId'
            riwayatIzin = allIzin.filter { it.mahasiswaId == mahasiswaId }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Detail Mahasiswa",
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
        } else if (profil != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // --- KARTU PROFIL UTAMA ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue), // Border Biru sesuai request
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Besar
                        Surface(
                            shape = CircleShape,
                            color = MainBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MainBlue,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = profil!!.namaLengkap,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextBlack
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email dengan Icon Kecil
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, null, tint = TextGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = profil!!.email, fontSize = 14.sp, color = TextGray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Statistik Ringkas (Dalam Card Profil)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItemDetail("Total Izin", riwayatIzin.size.toString())
                            StatItemDetail("Disetujui", riwayatIzin.count { it.status == "DISETUJUI" }.toString())
                            StatItemDetail("Ditolak", riwayatIzin.count { it.status == "DITOLAK" }.toString())
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- SECTION RIWAYAT ---
                Text(
                    text = "Riwayat Pengajuan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextBlack,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                // List History di Card Terpisah
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (riwayatIzin.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("Belum ada riwayat pengajuan.", color = TextGray)
                                }
                            }
                        }
                    } else {
                        items(riwayatIzin) { izin ->
                            MahasiswaHistoryItem(izin)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Mahasiswa tidak ditemukan.", color = TextGray)
            }
        }
    }
}



@Composable
fun MahasiswaHistoryItem(izin: PerizinanDto) {
    // Card untuk setiap item riwayat dengan border biru
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue), // Border Biru sesuai request
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { // Menggunakan Column untuk menampung Row data dan Divider
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = izin.jenisIzin.replace("_", " "),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = izin.tanggalMulai,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                BadgeStatus(izin.status)
            }
        }
    }
}