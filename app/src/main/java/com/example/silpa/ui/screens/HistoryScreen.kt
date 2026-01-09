package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.silpa.ui.components.HistoryItem
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import com.example.silpa.utils.toReadableFormat
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var listIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(value = true) }
    
    // Filter states
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedJenis by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Filtered list
    val filteredList = remember(listIzin, selectedStatus, selectedJenis) {
        listIzin.filter { izin ->
            val matchesStatus = selectedStatus == null || izin.status == selectedStatus
            val matchesJenis = selectedJenis == null || izin.jenisIzin == selectedJenis
            
            matchesStatus && matchesJenis
        }.sortedByDescending { it.id }
    }

    val activeFilterCount = listOf(selectedStatus, selectedJenis).count { it != null }



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
            },
            containerColor = BackgroundLight
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
                    // Filter Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Filter Riwayat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextBlack,
                            fontFamily = poppinsFont
                        )
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(containerColor = AlertRed) {
                                        Text("$activeFilterCount", fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            FilledTonalButton(
                                onClick = { showFilterDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MainBlue.copy(alpha = 0.1f)
                                )
                            ) {
                                Icon(Icons.Default.FilterList, null, tint = MainBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Filter", color = MainBlue, fontFamily = poppinsFont)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Active Filters Chips
                    if (activeFilterCount > 0) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            item {
                                if (selectedStatus != null) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { selectedStatus = null },
                                        label = { Text("Status: ${selectedStatus?.toReadableFormat()}", fontSize = 12.sp, fontFamily = poppinsFont) },
                                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MainBlue.copy(alpha = 0.15f),
                                            selectedLabelColor = MainBlue
                                        )
                                    )
                                }
                            }
                            item {
                                if (selectedJenis != null) {
                                    FilterChip(
                                        selected = true,
                                        onClick = { selectedJenis = null },
                                        label = { Text("Jenis: ${selectedJenis?.replace("_", " ")}", fontSize = 12.sp, fontFamily = poppinsFont) },
                                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MainBlue.copy(alpha = 0.15f),
                                            selectedLabelColor = MainBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    if (filteredList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tidak ada hasil yang cocok.", color = TextGray, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (activeFilterCount > 0) {
                                    TextButton(onClick = {
                                        selectedStatus = null
                                        selectedJenis = null
                                    }) {
                                        Text("Reset Filter", color = MainBlue, fontFamily = poppinsFont)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredList) { izin ->
                                HistoryItem(izin){
                                    navController.navigate("detail_izin/${izin.id}")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Filter Dialog
        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filter Riwayat", fontWeight = FontWeight.Bold, fontFamily = poppinsFont) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Status Filter
                        Text("Status:", fontWeight = FontWeight.Medium, fontSize = 14.sp, fontFamily = poppinsFont)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("DISETUJUI", "DITOLAK", "PERLU_REVISI").forEach { status ->
                                FilterChip(
                                    selected = selectedStatus == status,
                                    onClick = { 
                                        selectedStatus = if (selectedStatus == status) null else status 
                                    },
                                    label = { Text(status.toReadableFormat(), fontSize = 12.sp, fontFamily = poppinsFont) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MainBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        Divider()
                        
                        // Jenis Izin Filter
                        Text("Jenis Izin:", fontWeight = FontWeight.Medium, fontSize = 14.sp, fontFamily = poppinsFont)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SAKIT", "DISPENSASI_INSTITUSI", "IZIN_ALASAN_PENTING").forEach { jenis ->
                                FilterChip(
                                    selected = selectedJenis == jenis,
                                    onClick = { 
                                        selectedJenis = if (selectedJenis == jenis) null else jenis 
                                    },
                                    label = { Text(jenis.toReadableFormat(), fontSize = 12.sp, fontFamily = poppinsFont) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MainBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Terapkan", color = MainBlue, fontWeight = FontWeight.Bold, fontFamily = poppinsFont)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        selectedStatus = null
                        selectedJenis = null
                        showFilterDialog = false
                    }) {
                        Text("Reset", color = TextGray, fontFamily = poppinsFont)
                    }
                }
            )
        }
    }