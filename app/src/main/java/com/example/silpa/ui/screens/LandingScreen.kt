package com.example.silpa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.InfoJenisIzinDto
import com.example.silpa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    // Cek status login
    val isLoggedIn = remember { sessionManager.getToken() != null }

    var jenisIzinList by remember { mutableStateOf<List<InfoJenisIzinDto>>(emptyList()) }
    var selectedJenisIzin by remember { mutableStateOf<InfoJenisIzinDto?>(null) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val api = RetrofitInstance.getApi(context)
            val response = api.getInfoJenisIzin()
            if (response.berhasil && response.data != null) {
                jenisIzinList = response.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onIzinClicked(jenis: InfoJenisIzinDto) {
        selectedJenisIzin = jenis
        showBottomSheet = true
    }

    Scaffold(
        floatingActionButton = {
            if (!isLoggedIn) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("login") },
                    containerColor = MainBlue,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Login, "Login") },
                    text = { Text("Login / Daftar") }
                )
            }
        },
        containerColor = BackgroundLight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            //   HERO
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MainBlue,
                                    Color(0xFF64B5F6),
                                    Color.White
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .offset(x = (-80).dp, y = (-80).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = 60.dp, y = (-40).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomStart)
                            .offset(x = 40.dp, y = 20.dp)
                            .clip(CircleShape)
                            .background(MainBlue.copy(alpha = 0.05f))
                    )

                    // Konten Header
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(110.dp),
                            shadowElevation = 8.dp,
                            border = BorderStroke(3.dp, Color.White)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.silpafix),
                                contentDescription = "Logo SILPA",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            "Selamat Datang di SILPA",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SurfaceWhite,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Sistem Informasi  Layanan Perizinan Akademik Terpadu\nPoliteknik Statistika STIS",
                            fontSize = 14.sp,
                            color = Color(0xFF072A5E),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)) {
                    Text(
                        "Informasi Perizinan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextBlack
                    )
                    Text(
                        "Pilih jenis izin untuk melihat syarat & detail.",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }

            items(jenisIzinList) { izin ->
                InfoIzinCard(izin) {
                    onIzinClicked(izin)
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (showBottomSheet && selectedJenisIzin != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = SurfaceWhite
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
                    Text(
                        text = selectedJenisIzin?.namaTampilan ?: "Detail Izin",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MainBlue
                    )
                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = BorderGray)

                    val details = selectedJenisIzin?.daftarDetail ?: emptyList()

                    if (details.isEmpty()) {
                        Text("Tidak ada informasi detail untuk izin ini.", color = TextGray)
                    } else {
                        Text("Kategori & Syarat Berkas:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
                        Spacer(modifier = Modifier.height(8.dp))

                        details.forEach { detail ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(1.dp, BorderBlue)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(detail.namaTampilan, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextBlack)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(detail.deskripsi, fontSize = 12.sp, color = TextGray)

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Syarat Dokumen:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MainBlue)
                                    Text(detail.syarat, fontSize = 12.sp, fontStyle = Italic, color = TextBlack)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoIzinCard(izin: InfoJenisIzinDto, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderBlue),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MainBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MainBlue)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(izin.namaTampilan, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextBlack)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextGray)
        }
    }
}