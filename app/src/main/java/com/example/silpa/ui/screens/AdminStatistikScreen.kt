package com.example.silpa.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.StatistikPerBulanDto
import com.example.silpa.model.StatistikPerJenisDto
import com.example.silpa.model.StatistikTrendDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatistikScreen(navController: NavController) {
    val context = LocalContext.current
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Jenis Izin", "Bulanan", "Trend")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik Data", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MainBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(SurfaceWhite)) {
            TabRow(selectedTabIndex = tabIndex, containerColor = Color.White, contentColor = MainBlue) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(title) })
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                when (tabIndex) {
                    0 -> StatistikJenisContent()
                    1 -> StatistikBulananContent()
                    2 -> StatistikTrendContent()
                }
            }
        }
    }
}

@Composable
fun StatistikJenisContent() {
    val context = LocalContext.current
    var data by remember { mutableStateOf<List<StatistikPerJenisDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // PERBAIKAN: Handle ApiResponse
            val response = RetrofitInstance.getApi(context).getStatistikPerJenis()
            if (response.berhasil && response.data != null) {
                data = response.data
            }
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MainBlue)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(data) { item ->
                StatBarRow(
                    label = item.namaJenisIzin ?: item.jenisIzin,
                    value = item.jumlahPengajuan.toInt(), // Konversi Long ke Int untuk UI
                    maxVal = data.maxOfOrNull { it.jumlahPengajuan.toInt() } ?: 1
                )
            }
            if(data.isEmpty()) item { Text("Tidak ada data statistik.", color = Color.Gray) }
        }
    }
}

@Composable
fun StatistikBulananContent() {
    val context = LocalContext.current
    var data by remember { mutableStateOf<List<StatistikPerBulanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.getApi(context).getStatistikBulanan()
            if (response.berhasil && response.data != null) {
                data = response.data
            }
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MainBlue)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(data) { item ->
                StatBarRow(
                    label = item.namaBulanTahun,
                    value = item.jumlahPengajuan.toInt(),
                    maxVal = data.maxOfOrNull { it.jumlahPengajuan.toInt() } ?: 1
                )
            }
            if(data.isEmpty()) item { Text("Tidak ada data statistik bulanan.", color = Color.Gray) }
        }
    }
}

@Composable
fun StatistikTrendContent() {
    val context = LocalContext.current
    var trend by remember { mutableStateOf<StatistikTrendDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.getApi(context).getStatistikTrend()
            if (response.berhasil && response.data != null) {
                trend = response.data
            } else {
                errorMessage = "Data trend kosong atau gagal diambil."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Error: ${e.message}"
        }
        finally { isLoading = false }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top=32.dp)) {
        if(isLoading) {
            CircularProgressIndicator(color = MainBlue)
        } else if(trend != null) {
            // Safe unwrap
            val safeTrend = trend!!

            // LOGIKA PENGGANTI field 'trenNaik' yang tidak ada di Backend Java
            // Kita tentukan sendiri berdasarkan persentase
            val isNaik = safeTrend.persentasePerubahan >= 0

            Text("Trend Pengajuan Izin", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if(isNaik) "NAIK 📈" else "TURUN 📉",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if(isNaik) AlertRed else SuccessGreen
            )

            val persenStr = String.format("%.1f", safeTrend.persentasePerubahan)
            Text("$persenStr% dari bulan lalu", fontSize = 18.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = safeTrend.deskripsiPerubahan,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatSimpleBox("Bulan Ini", safeTrend.jumlahBulanIni.toString())
                StatSimpleBox("Bulan Lalu", safeTrend.jumlahBulanLalu.toString())
            }
        } else {
            Text("Gagal memuat data trend.", color = Color.Red)
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        }
    }
}
