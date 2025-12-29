package com.example.silpa.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.MahasiswaDetailAdminDto
import com.example.silpa.model.PerizinanDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.components.BadgeStatus
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMahasiswaDetailScreen(navController: NavController, mahasiswaId: Long) {
    val context = LocalContext.current
    var mahasiswa by remember { mutableStateOf<MahasiswaDetailAdminDto?>(null) }
    var perizinanList by remember { mutableStateOf<List<PerizinanDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.getApi(context)
                
                // Ambil detail mahasiswa
                mahasiswa = api.getMahasiswaDetail(mahasiswaId)
                
                // Ambil riwayat perizinan mahasiswa
                perizinanList = api.getPerizinanMahasiswaById(mahasiswaId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Detail Mahasiswa",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else if (mahasiswa != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SurfaceWhite),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Header dengan gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBlue)
                            .padding(vertical = 32.dp, horizontal = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = AccentPurple.copy(alpha = 0.2f),
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        tint = MainBlue,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                mahasiswa!!.profil.namaLengkap,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                mahasiswa!!.profil.email,
                                fontSize = 13.sp,
                                color = SurfaceWhite.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Detail Mahasiswa
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        Text(
                            "Informasi Pribadi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextBlack
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("Nama", mahasiswa!!.profil.namaLengkap)
                        DetailRow("Email", mahasiswa!!.profil.email)
                        DetailRow("Total Izin", mahasiswa!!.totalIzinDiajukan.toString())
                    }
                }

                // Divider
                item {
                    Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 24.dp))
                }

                // Riwayat Perizinan
                item {
                    Text(
                        "Riwayat Perizinan (${perizinanList.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextBlack,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }

                if (perizinanList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada riwayat perizinan", color = TextGray)
                        }
                    }
                } else {
                    items(perizinanList.sortedByDescending { it.id }) { izin ->
                        PerizinanAdminItem(izin)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontSize = 14.sp,
            color = TextBlack,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun PerizinanAdminItem(izin: PerizinanDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    izin.jenisIzin.replace("_", " "),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    izin.detailIzin.replace("_", " "),
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
            BadgeStatus(izin.status)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Mulai",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    izin.tanggalMulai,
                    fontSize = 12.sp,
                    color = TextBlack,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Alasan",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    izin.deskripsi,
                    fontSize = 12.sp,
                    color = TextBlack,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Divider(color = BorderGray, thickness = 1.dp, modifier = Modifier.padding(top = 12.dp))
    }
}
