package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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

                // Panggil API
                val updatedResult = api.validasiIzin(perizinanId, request)

                // LANGSUNG UPDATE STATE LOKAL AGAR UI BERUBAH
                detailIzin = updatedResult

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
            SilpaTopAppBar(
                title = "Validasi Izin",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (detailIzin == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MainBlue) }
        } else {
            // LOGIKA UI UTAMA: Jika status bukan DIAJUKAN, berarti sudah final
            val isFinal = detailIzin!!.status != "DIAJUKAN"

            Column(
                modifier = Modifier.padding(padding).fillMaxSize().background(SurfaceWhite).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Info Mahasiswa & Detail (Sama seperti sebelumnya)
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pemohon: ${detailIzin!!.mahasiswaNama ?: detailIzin!!.mahasiswaNama ?: "-"}", fontWeight = FontWeight.Bold, color = MainBlue)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Detail Izin", fontWeight = FontWeight.Bold)
                            BadgeStatus(detailIzin!!.status)
                        }
                        Divider(Modifier.padding(vertical=8.dp))
                        Text("Jenis: ${detailIzin!!.jenisIzin}", fontSize = 14.sp)
                        Text("Mulai: ${detailIzin!!.tanggalMulai}", fontSize = 14.sp)
                        Text("Alasan: ${detailIzin!!.deskripsi}", fontSize = 14.sp, color = Color.DarkGray)
                    }
                }

                // Input Catatan (Read-only jika sudah final)
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { if (!isFinal) catatan = it },
                    label = { Text("Catatan Admin") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFinal // KUNCI INPUT JIKA SUDAH FINAL
                )

                // AREA KEPUTUSAN
                if (isFinal) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text("Keputusan telah diambil: ${detailIzin!!.status}", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { submitValidasi("DITOLAK") }, colors = ButtonDefaults.buttonColors(containerColor = AlertRed), modifier = Modifier.weight(1f), enabled = !isProcessing) { Text("Tolak") }
                        Button(onClick = { submitValidasi("PERLU_REVISI") }, colors = ButtonDefaults.buttonColors(containerColor = WarningYellow), modifier = Modifier.weight(1f), enabled = !isProcessing) { Text("Revisi") }
                        Button(onClick = { submitValidasi("DISETUJUI") }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), modifier = Modifier.weight(1f), enabled = !isProcessing) { Text("Setuju") }
                    }
                }
            }
        }
    }
}