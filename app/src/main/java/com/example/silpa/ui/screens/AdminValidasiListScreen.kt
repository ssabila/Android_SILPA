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
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch
import com.example.silpa.ui.components.*

// Definisi warna lokal agar aman
private val ValBlue = Color(0xFF1976D2)
private val ValGreen = Color(0xFF4CAF50)
private val ValRed = Color(0xFFE53935)
private val ValOrange = Color(0xFFFF9800)
private val ValDark = Color(0xFF0D47A1)
private val ValBg = Color(0xFFF5F7FA)

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
                // Filter hanya status DIAJUKAN (belum divalidasi)
                val statusParam = "DIAJUKAN"
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
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(ValBg)) {
            // Search Bar Sederhana
            OutlinedTextField(
                value = filterNama,
                onValueChange = { filterNama = it },
                label = { Text("Cari Nama Mahasiswa") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                trailingIcon = {
                    IconButton(onClick = { loadData() }) { Icon(Icons.Default.Search, null) }
                },
                singleLine = true
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = ValBlue)
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listIzin) { izin ->
                    // Memanggil komponen lokal yang didefinisikan di bawah
                    AdminPermissionItemLocal(izin) {
                        navController.navigate("admin_validasi/${izin.id}")
                    }
                }
                if (listIzin.isEmpty() && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Data tidak ditemukan.", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Bottom Sheet Filter
        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false }, containerColor = Color.White) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text("Filter Data", style = MaterialTheme.typography.titleLarge, color = ValDark)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Status", fontWeight = FontWeight.Bold)
                    DropdownInput(filterStatus.ifEmpty { "Semua Status" }, listOf("Semua Status", "DIAJUKAN", "DISETUJUI", "DITOLAK", "PERLU_REVISI")) {
                        filterStatus = if(it == "Semua Status") "" else it
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Jenis Izin", fontWeight = FontWeight.Bold)
                    DropdownInput(filterJenis.ifEmpty { "Semua Jenis" }, listOf("Semua Jenis", "SAKIT", "DISPENSASI_INSTITUSI", "IZIN_ALASAN_PENTING")) {
                        filterJenis = if(it == "Semua Jenis") "" else it
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            showFilterSheet = false
                            loadData()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ValBlue)
                    ) { Text("Terapkan Filter") }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- Komponen Lokal (Redefinisi agar tidak error) ---

@Composable
fun AdminPermissionItemLocal(izin: PerizinanDto, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(izin.mahasiswaNama ?: "Mahasiswa", fontWeight = FontWeight.Bold, color = ValDark)
                BadgeStatus(izin.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${izin.jenisIzin} - ${izin.detailIzin}", fontSize = 14.sp)
            Text("Mulai: ${izin.tanggalMulai}", fontSize = 12.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(
                    onClick = onClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ValBlue)
                ) { Text("Detail / Validasi", fontSize = 12.sp) }
            }
        }
    }
}