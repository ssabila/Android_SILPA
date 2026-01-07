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
import com.example.silpa.ui.components.HistoryItem
import com.example.silpa.ui.components.SilpaTopAppBar // Import TopAppBar yang sudah dibuat
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminValidasiListScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Filter State
    var filterStatus by remember { mutableStateOf("DIAJUKAN") }
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
                    HistoryItem (izin) {
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
                    DropdownInput(filterStatus.ifEmpty { "Semua Status" }, listOf("DIAJUKAN", "PERLU_REVISI")) {
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