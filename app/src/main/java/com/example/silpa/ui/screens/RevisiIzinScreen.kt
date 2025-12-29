package com.example.silpa.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.AjukanIzinDto
import com.example.silpa.model.DetailSesiIzinDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisiIzinScreen(navController: NavController, perizinanId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var jenisIzin by remember { mutableStateOf("") }
    var detailIzin by remember { mutableStateOf("") }
    var tanggalMulai by remember { mutableStateOf("") }
    var durasiHari by remember { mutableStateOf("1") }
    var matkul by remember { mutableStateOf("") }
    var dosen by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    // File State
    val selectedUris = remember { mutableStateListOf<Uri>() }
    var isLoadingData by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Load Data Lama
    LaunchedEffect(perizinanId) {
        try {
            val oldData = RetrofitInstance.getApi(context).getPerizinanById(perizinanId)
            jenisIzin = oldData.jenisIzin
            detailIzin = oldData.detailIzin
            tanggalMulai = oldData.tanggalMulai
            deskripsi = oldData.deskripsi

            // Ambil data sesi pertama untuk prefill matkul/dosen/durasi
            if (!oldData.daftarSesi.isNullOrEmpty()) {
                durasiHari = oldData.daftarSesi.size.toString()
                matkul = oldData.daftarSesi[0].namaMataKuliah
                dosen = oldData.daftarSesi[0].namaDosen
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat data lama", Toast.LENGTH_SHORT).show()
        } finally {
            isLoadingData = false
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> selectedUris.addAll(uris) }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Revisi Izin",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WarningYellow)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(SurfaceWhite)
                    .padding(16.dp) // Updated padding for consistency
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing
            ) {
                Text("Perbaiki Data Pengajuan", fontWeight = FontWeight.Bold, color = MainBlue)

                // Using DropdownInput (Ensure this composable is available/imported)
                DropdownInput(jenisIzin, listOf("SAKIT", "DISPENSASI_INSTITUSI", "IZIN_ALASAN_PENTING")) { jenisIzin = it }

                val detailOptions = when (jenisIzin) {
                    "SAKIT" -> listOf("RAWAT_JALAN", "RAWAT_INAP")
                    "DISPENSASI_INSTITUSI" -> listOf("DISPENSASI")
                    else -> listOf("KELUARGA_INTI_MENINGGAL", "BENCANA", "PASANGAN_MELAHIRKAN")
                }
                DropdownInput(detailIzin, detailOptions) { detailIzin = it }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tanggalMulai,
                        onValueChange = { tanggalMulai = it },
                        label = { Text("Tanggal Mulai") },
                        modifier = Modifier.weight(2f),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                    )
                    OutlinedTextField(
                        value = durasiHari,
                        onValueChange = { if (it.all { c -> c.isDigit() }) durasiHari = it },
                        label = { Text("Hari") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = matkul,
                    onValueChange = { matkul = it },
                    label = { Text("Mata Kuliah") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosen,
                    onValueChange = { dosen = it },
                    label = { Text("Dosen") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Divider()
                Text("Dokumen Baru (Wajib Upload Ulang)", fontWeight = FontWeight.Bold, color = AlertRed)
                Text("Untuk revisi, mohon upload kembali bukti pendukung.", fontSize = 12.sp, color = Color.Gray)

                OutlinedButton(onClick = { fileLauncher.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AttachFile, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih File Baru")
                }

                if (selectedUris.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedUris) { uri ->
                            InputChip(
                                selected = true,
                                onClick = { selectedUris.remove(uri) },
                                label = { Text(getFileName(context, uri).take(15) + "...") },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) },
                                colors = InputChipDefaults.inputChipColors(containerColor = Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedUris.isEmpty()) {
                            Toast.makeText(context, "Wajib upload dokumen revisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            isSubmitting = true
                            try {
                                // Generate List Sesi Baru
                                val durasi = durasiHari.toIntOrNull() ?: 1
                                // Try parsing date, fallback to now if fails (add validation in real app)
                                val start = try { LocalDate.parse(tanggalMulai) } catch(e:Exception) { LocalDate.now() }

                                val listSesi = mutableListOf<DetailSesiIzinDto>()
                                for (i in 0 until durasi) {
                                    listSesi.add(
                                        DetailSesiIzinDto(
                                            tanggal = start.plusDays(i.toLong()).toString(),
                                            namaMataKuliah = matkul,
                                            namaDosen = dosen
                                        )
                                    )
                                }

                                val izinData = AjukanIzinDto(jenisIzin, detailIzin, deskripsi, listSesi)
                                val gson = Gson()
                                val jsonString = gson.toJson(izinData)
                                val izinPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                                val fileParts = prepareFileParts(context, selectedUris)

                                // PANGGIL ENDPOINT REVISI
                                val api = RetrofitInstance.getApi(context)
                                api.revisiIzin(perizinanId, izinPart, fileParts)

                                Toast.makeText(context, "Revisi Berhasil Dikirim!", Toast.LENGTH_LONG).show()
                                navController.popBackStack() // Kembali ke detail
                                navController.popBackStack() // Kembali ke history/dashboard

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Gagal Revisi: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White) else Text("KIRIM REVISI")
                }
            }
        }
    }
}


