package com.example.silpa.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.PerizinanDto
import com.example.silpa.model.StatistikPerBulanDto
import com.example.silpa.model.StatistikPerJenisDto
import com.example.silpa.model.StatistikTrendDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatistikScreen(navController: NavController) {
    val context = LocalContext.current
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Jenis Izin", "Bulanan", "Trend")

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Statistik Data",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = SurfaceWhite,
                contentColor = MainBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        color = MainBlue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(title, fontWeight = if(tabIndex==index) FontWeight.Bold else FontWeight.Normal) }
                    )
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
    var dataJenis by remember { mutableStateOf<List<StatistikPerJenisDto>>(emptyList()) }
    var rawData by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val responseJenis = RetrofitInstance.getApi(context).getStatistikPerJenis()
            if (responseJenis.berhasil && responseJenis.data != null) {
                dataJenis = responseJenis.data
            }
            rawData = RetrofitInstance.getApi(context).getSemuaPerizinan()
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MainBlue)
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Analisis Kategori", fontSize = 14.sp, color = MainBlue, fontWeight = FontWeight.Bold)
                    Text("Total", fontSize = 14.sp, color = MainBlue, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MainBlue.copy(alpha = 0.3f), thickness = 1.dp)

                if(dataJenis.isEmpty()) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data statistik.", color = TextGray)
                    }
                } else {
                    val maxVal = dataJenis.maxOfOrNull { it.jumlahPengajuan.toInt() } ?: 1
                    dataJenis.forEachIndexed { index, item ->
                        val topMonth = calculateTopMonthForType(item.jenisIzin, rawData)
                        StatJenisItemDetailed(item, maxVal, topMonth)
                        if (index < dataJenis.size - 1) {
                            Divider(
                                color = MainBlue.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculateTopMonthForType(jenisIzin: String, allData: List<PerizinanDto>): String {
    if (allData.isEmpty()) return "-"
    val filtered = allData.filter { it.jenisIzin == jenisIzin }
    if (filtered.isEmpty()) return "-"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val grouped = filtered.groupingBy {
                try { LocalDate.parse(it.tanggalMulai).month } catch (e: Exception) { null }
            }.eachCount()
            val topEntry = grouped.filterKeys { it != null }.maxByOrNull { it.value }
            return topEntry?.key?.getDisplayName(TextStyle.FULL, Locale("id", "ID")) ?: "-"
        } catch (e: Exception) { return "-" }
    }
    return "-"
}

@Composable
fun StatJenisItemDetailed(item: StatistikPerJenisDto, maxVal: Int, topMonth: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.namaJenisIzin ?: item.jenisIzin.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextBlack
                )
                if (topMonth != "-") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = AccentPink, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Paling banyak di bulan $topMonth", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
            Surface(
                color = MainBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${item.jumlahPengajuan}",
                    fontWeight = FontWeight.Bold,
                    color = MainBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 16.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val percentage = if(maxVal > 0) (item.jumlahPengajuan.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier.fillMaxWidth().height(8.dp).background(BackgroundLight, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(percentage).fillMaxHeight().background(
                    Brush.horizontalGradient(listOf(MainBlue, AccentPurple)),
                    RoundedCornerShape(4.dp)
                )
            )
        }
    }
}

@Composable
fun StatistikBulananContent() {
    val context = LocalContext.current
    var data by remember { mutableStateOf<List<StatistikPerBulanDto>>(emptyList()) }
    var rawData by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.getApi(context).getStatistikBulanan()
            if (response.berhasil && response.data != null) {
                data = response.data
            }
            rawData = RetrofitInstance.getApi(context).getSemuaPerizinan()
        } catch (e: Exception) { e.printStackTrace() }
        finally { isLoading = false }
    }

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MainBlue)
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bulan", fontSize = 14.sp, color = MainBlue, fontWeight = FontWeight.Bold)
                    Text("Total Izin", fontSize = 14.sp, color = MainBlue, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = MainBlue.copy(alpha = 0.3f), thickness = 1.dp)

                if(data.isEmpty()) {
                    Box(modifier = Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data statistik bulanan.", color = TextGray)
                    }
                } else {
                    val maxVal = data.maxOfOrNull { it.jumlahPengajuan.toInt() } ?: 1

                    data.forEachIndexed { index, item ->
                        val topType = calculateTopTypeForMonth(item.bulan, item.tahun, rawData)
                        StatBulanItemDetailed(item, maxVal, topType)

                        if (index < data.size - 1) {
                            Divider(
                                color = Color(0xFF607D8B).copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun calculateTopTypeForMonth(bulan: Int, tahun: Int, allData: List<PerizinanDto>): String {
    if (allData.isEmpty()) return "-"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            val filtered = allData.filter {
                try {
                    val date = LocalDate.parse(it.tanggalMulai)
                    date.monthValue == bulan && date.year == tahun
                } catch (e: Exception) { false }
            }
            if (filtered.isEmpty()) return "-"

            val grouped = filtered.groupingBy { it.jenisIzin }.eachCount()
            val topEntry = grouped.maxByOrNull { it.value }

            return topEntry?.key?.replace("_", " ") ?: "-"
        } catch (e: Exception) { return "-" }
    }
    return "-"
}

@Composable
fun StatBulanItemDetailed(item: StatistikPerBulanDto, maxVal: Int, topType: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.namaBulanTahun,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack
                )
                if (topType != "-") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Equalizer, null, tint = AccentPurple, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Didominasi oleh: $topType", fontSize = 12.sp, color = TextGray)
                    }
                }
            }
            Surface(
                color = MainBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${item.jumlahPengajuan}",
                    fontWeight = FontWeight.Bold,
                    color = MainBlue,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 14.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        val percentage = if(maxVal > 0) (item.jumlahPengajuan.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier.fillMaxWidth().height(8.dp).background(BackgroundLight, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(listOf(MainBlue, SuccessGreen)),
                        RoundedCornerShape(4.dp)
                    )
            )
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

    if(isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MainBlue)
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                if (trend != null) {
                    val safeTrend = trend!!
                    val isNaik = safeTrend.persentasePerubahan >= 0

                    Text("Analisis Tren Pengajuan", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextBlack)
                    Spacer(modifier = Modifier.height(24.dp))

                    //  KARTU INDIKATOR NAIK/TURUN
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if(isNaik) AlertRed.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if(isNaik) AlertRed else SuccessGreen
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if(isNaik) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if(isNaik) AlertRed else SuccessGreen,
                                    modifier = Modifier.size(64.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if(isNaik) "TREN MENINGKAT" else "TREN MENURUN",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if(isNaik) AlertRed else SuccessGreen,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center // Pastikan text align center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Persentase
                                val persenStr = String.format("%.1f", safeTrend.persentasePerubahan)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center, // Pastikan row content centered
                                    modifier = Modifier.fillMaxWidth() // Row mengisi lebar agar arrangement center bekerja
                                ) {
                                    Icon(
                                        imageVector = if(isNaik) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if(isNaik) AlertRed else SuccessGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$persenStr% dari bulan lalu",
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Deskripsi Text
                    Text(
                        text = safeTrend.deskripsiPerubahan,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = TextBlack,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Detail Angka
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatSimpleBox("Bulan Ini", safeTrend.jumlahBulanIni.toString())
                        // Divider Vertikal Kecil
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderGray))
                        StatSimpleBox("Bulan Lalu", safeTrend.jumlahBulanLalu.toString())
                    }
                } else {
                    Text("Gagal memuat data trend.", color = AlertRed)
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage, fontSize = 12.sp, color = TextGray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatSimpleBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}