package com.example.silpa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.PerizinanDto
import com.example.silpa.ui.components.HistoryItem
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    var listIzin by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(value = true) }



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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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