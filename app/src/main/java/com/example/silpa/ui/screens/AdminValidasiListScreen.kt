package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.example.silpa.ui.components.DropdownInput
import com.example.silpa.ui.components.SilpaTopAppBar // Import TopAppBar yang sudah dibuat
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

// Gunakan warna dari tema global, bukan lokal lagi
// private val ValBlue = Color(0xFF1976D2) ... (Hapus ini jika sudah ada di Color.kt)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminValidasiListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Filter State
    var filterStatus by remember { mutableStateOf("") } // Kosong = Semua
    var filterJenis by remember { mutableStateOf("") }
    var filterNama by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    var listIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val statusParam = if(filterStatus.isNotEmpty()) filterStatus else null
                val jenisParam = if(filterJenis.isNotEmpty()) filterJenis else null
                val namaParam = if(filterNama.isNotEmpty()) filterNama else null

                listIzin = RetrofitInstance.getApi(context).filterPerizinan(
                    status = statusParam,
                    jenisIzin = jenisParam,
                    namaMahasiswa = namaParam
                )
            } catch (e: Exception) { e.printStackTrace() }
            finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Daftar Perizinan",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, "Filter", tint = MainBlue) // Sesuaikan warna icon jika background putih
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(BackgroundLight)) {
            // Search Bar Sederhana
            OutlinedTextField(
                value = filterNama,
                onValueChange = { filterNama = it },
                label = { Text("Cari Nama Mahasiswa") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                trailingIcon = {
                    IconButton(onClick = { loadData() }) { Icon(Icons.Default.Search, null) }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MainBlue,
                    unfocusedBorderColor = BorderBlue
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MainBlue)
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listIzin) { izin ->
                    // Memanggil komponen lokal yang sudah diperbarui warnanya
                    AdminPermissionItemLocal(izin) {
                        navController.navigate("admin_validasi/${izin.id}")
                    }
                }
                if (listIzin.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Data tidak ditemukan.", color = TextGray)
                        }
                    }
                }
            }
        }

        // Bottom Sheet Filter
        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, containerColor = SurfaceWhite) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text("Filter Data", style = MaterialTheme.typography.titleLarge, color = TextBlack)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Status", fontWeight = FontWeight.Bold, color = TextBlack)
                    DropdownInput(filterStatus.ifEmpty { "Semua Status" }, listOf("Semua Status", "DIAJUKAN", "DISETUJUI", "DITOLAK", "PERLU_REVISI")) {
                        filterStatus = if(it == "Semua Status") "" else it
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Jenis Izin", fontWeight = FontWeight.Bold, color = TextBlack)
                    DropdownInput(filterJenis.ifEmpty { "Semua Jenis" }, listOf("Semua Jenis", "SAKIT", "DISPENSASI_INSTITUSI", "IZIN_ALASAN_PENTING")) {
                        filterJenis = if(it == "Semua Jenis") "" else it
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showFilterSheet = false
                            loadData()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Terapkan Filter") }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- Komponen Lokal (Dengan Warna Kategori) ---

@Composable
fun AdminPermissionItemLocal(izin: PerizinanDto, onClick: () -> Unit) {
    // Tentukan warna background kartu berdasarkan jenis izin
    val cardBackgroundColor = when (izin.jenisIzin) {
        "SAKIT" -> SurfaceWhite
        "DISPENSASI_INSTITUSI" -> SurfaceWhite // Sesuaikan dengan enum backend
        "IZIN_ALASAN_PENTING" -> SurfaceWhite
        else -> SurfaceWhite
    }

    // Tentukan warna border/aksen (opsional, bisa pakai versi lebih gelap dari background)
    val borderColor = when (izin.jenisIzin) {
        "SAKIT" -> IzinSakitColor.copy(alpha = 0.5f)
        "DISPENSASI_INSTITUSI" -> IzinDispensasiColor.copy(alpha = 0.5f)
        "IZIN_ALASAN_PENTING" -> IzinPentingColor.copy(alpha = 0.5f)
        else -> BorderGray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat design minimalis
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = izin.mahasiswaNama ?: "Mahasiswa",
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    fontSize = 16.sp
                )
                BadgeStatus(izin.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Detail Izin
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Label Jenis Izin (misal: SAKIT)
                Surface(
                    color = Color.White.copy(alpha = 0.6f),
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

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Aksi Minimalis
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Lihat Detail & Validasi", fontSize = 12.sp, color = MainBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
