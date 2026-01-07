package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
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
import com.example.silpa.model.UpdateStatusDto
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.example.silpa.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminValidasiScreen(navController: NavController, perizinanId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var detailIzin by remember { mutableStateOf<PerizinanDto?>(null) }
    var catatan by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(perizinanId) {
        try {
            val allList = RetrofitInstance.getApi(context).getSemuaPerizinan()
            detailIzin = allList.find { it.id == perizinanId }
            if (detailIzin != null && !detailIzin!!.catatanAdmin.isNullOrEmpty()) {
                catatan = detailIzin!!.catatanAdmin!!
            }
        } catch (e: Exception) {
            errorMessage = "Gagal memuat data: ${e.localizedMessage}"
            showErrorDialog = true
        }
    }

    fun submitValidasi(status: String) {
        if ((status == "DITOLAK" || status == "PERLU_REVISI") && catatan.isBlank()) {
            errorMessage = "Wajib isi catatan untuk keputusan $status!"
            showErrorDialog = true
            return
        }

        scope.launch {
            isProcessing = true
            try {
                val api = RetrofitInstance.getApi(context)
                val request = UpdateStatusDto(status, catatan)

                val updatedResult = api.validasiIzin(perizinanId, request)
                detailIzin = updatedResult // Update UI langsung

                Toast.makeText(context, "Status berhasil disimpan!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is HttpException -> when (e.code()) {
                        400 -> "Data tidak valid. Cek input Anda."
                        403 -> "Akses ditolak."
                        else -> "Gagal memproses (Error ${e.code()})"
                    }
                    is IOException -> "Masalah koneksi internet."
                    else -> "Error: ${e.localizedMessage}"
                }
                showErrorDialog = true
            } finally {
                isProcessing = false
            }
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Row { Icon(Icons.Default.Error, null, tint = AlertRed); Spacer(Modifier.width(8.dp)); Text("Perhatian", color = AlertRed) } },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = { showErrorDialog = false }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Validasi Izin", color = Color.White) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MainBlue)
            )
        }
    ) { padding ->
        if (detailIzin == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MainBlue) }
        } else {
            // LOGIKA UI UTAMA: Jika status bukan DIAJUKAN, berarti sudah final
            val isFinal = detailIzin!!.status != "DIAJUKAN"
            val currentStatus = detailIzin!!.status

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(BackgroundLight) // Background screen abu-abu muda
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Jarak antar elemen utama
            ) {
                // --- 1. CARD UTAMA: Berisi semua informasi ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderBlue), // Border tipis
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Header: Pemohon & Status
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Pemohon", fontSize = 12.sp, color = TextGray)
                                Text(
                                    text = detailIzin!!.mahasiswaNama ?: detailIzin!!.mahasiswaNama ?: "-",
                                    fontWeight = FontWeight.Bold,
                                    color = TextBlack,
                                    fontSize = 18.sp
                                )
                            }
                            BadgeStatus(detailIzin!!.status)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderBlue)

                        // Body: Detail Izin
                        Text("Detail Pengajuan", fontWeight = FontWeight.SemiBold, color = TextBlack, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("Jenis Izin", detailIzin!!.jenisIzin)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Detail", detailIzin!!.detailIzin)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("Tanggal Mulai", detailIzin!!.tanggalMulai)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Alasan:", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = detailIzin!!.deskripsi,
                            fontSize = 14.sp,
                            color = TextBlack,
                            lineHeight = 20.sp
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = BorderBlue)

                        // Footer: Input Catatan
                        Text("Catatan Admin", fontWeight = FontWeight.SemiBold, color = TextBlack, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = catatan,
                            onValueChange = { if (!isFinal) catatan = it },
                            placeholder = { Text("Tambahkan catatan jika perlu revisi/tolak...", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            enabled = !isFinal, // Read-only jika sudah final
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MainBlue,
                                unfocusedBorderColor = MainBlue,
                                disabledBorderColor = BorderGray.copy(alpha = 0.5f),
                                disabledTextColor = TextBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // --- 2. TOMBOL AKSI (Di luar Card) ---
                if (isFinal) {
                    // Tampilan Status Final
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = TextGray)
                            Spacer(Modifier.width(8.dp))
                            Text("Keputusan: $currentStatus", color = TextGray, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Tombol Aksi Aktif
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Tombol Tolak
                        Button(
                            onClick = { submitValidasi("DITOLAK") },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text("Tolak", fontWeight = FontWeight.Bold)
                        }

                        // Tombol Revisi
                        Button(
                            onClick = { submitValidasi("PERLU_REVISI") },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Revisi", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Tombol Setuju
                        Button(
                            onClick = { submitValidasi("DISETUJUI") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Setuju", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
