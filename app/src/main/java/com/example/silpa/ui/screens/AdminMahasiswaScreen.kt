package com.example.silpa.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.ProfilPenggunaDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMahasiswaScreen(navController: NavController) {
    val context = LocalContext.current
    var listMhs by remember { mutableStateOf<List<ProfilPenggunaDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            listMhs = RetrofitInstance.getApi(context).getAllMahasiswa()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Daftar Mahasiswa",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp), // Padding kiri kanan saja
                verticalArrangement = Arrangement.spacedBy(12.dp), // Jarak antar item
                contentPadding = PaddingValues(vertical = 16.dp) // Padding atas bawah list
            ) {
                items(listMhs) { mhs ->
                    MahasiswaRowItem(mhs) {
                        navController.navigate("admin_mahasiswa_detail/${mhs.id}")
                    }
                }

                if (listMhs.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data mahasiswa.", color = TextGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MahasiswaRowItem(mhs: ProfilPenggunaDto, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray), // Border halus
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }, // Bisa diklik
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Avatar dengan warna aksen
            Surface(
                shape = CircleShape,
                color = AccentPurple.copy(alpha = 0.1f), // Purple muda transparan
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Text
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = mhs.namaLengkap,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextBlack,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mhs.email,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Indikator kecil (opsional)
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward, // Atau icon lain
                contentDescription = null,
                tint = BorderGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}