package com.example.silpa.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.DetailSesiIzinDto
import com.example.silpa.model.PerizinanDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.components.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException

// --- Helper Data Class untuk UI State ---
data class SessionInputState(
    val id: Int, // 1, 2, atau 3
    var label: String,
    var isSelected: Boolean = false,
    var matkul: String = "",
    var dosen: String = ""
)

data class DayInputState(
    val date: LocalDate,
    val sessions: List<SessionInputState>
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitIzinScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State Data User ---
    var mahasiswaId by remember { mutableStateOf<Long?>(null) }
    var mahasiswaNama by remember { mutableStateOf<String?>(null) }

    // Ambil profil user saat layar dibuka
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val api = RetrofitInstance.getApi(context)
                val response = api.getProfil()
                if (response.berhasil && response.data != null) {
                    mahasiswaId = response.data.id
                    mahasiswaNama = response.data.namaLengkap
                }
            } catch (e: Exception) {
                // Handle silent error
            }
        }
    }

    // Form State Utama
    var jenisIzin by remember { mutableStateOf("SAKIT") }
    var detailIzin by remember { mutableStateOf("RAWAT_JALAN") }
    var tanggalMulai by remember { mutableStateOf(LocalDate.now().toString()) }
    var durasiHari by remember { mutableStateOf("1") }
    var bobotKehadiran by remember { mutableStateOf("0") }
    var deskripsi by remember { mutableStateOf("") }

    // State untuk Jadwal Dinamis
    // List ini akan menampung data input per hari dan per sesi
    var scheduleList = remember { mutableStateListOf<DayInputState>() }

    // Error State
    var tanggalError by remember { mutableStateOf(false) }
    var durasiError by remember { mutableStateOf(false) }
    var bobotError by remember { mutableStateOf(false) }
    var deskripsiError by remember { mutableStateOf(false) }
    var scheduleError by remember { mutableStateOf(false) }

    // File State
    val selectedUris = remember { mutableStateListOf<Uri>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Launcher Multi-File
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris.addAll(uris)
    }

    // --- Logic: Generate Jadwal saat Tanggal/Durasi berubah ---
    LaunchedEffect(tanggalMulai, durasiHari) {
        try {
            val start = LocalDate.parse(tanggalMulai)
            val days = durasiHari.toIntOrNull() ?: 1

            // Simpan data lama jika ada, untuk menghindari reset inputan user saat nambah hari
            val oldList = scheduleList.toList()
            scheduleList.clear()

            for (i in 0 until days) {
                val currentDate = start.plusDays(i.toLong())

                // Cek apakah tanggal ini sudah ada inputannya sebelumnya (preserve input)
                val existingDay = oldList.find { it.date == currentDate }

                if (existingDay != null) {
                    scheduleList.add(existingDay)
                } else {
                    // Buat baru default
                    scheduleList.add(
                        DayInputState(
                            date = currentDate,
                            sessions = listOf(
                                SessionInputState(1, "Sesi 1 (07.30)"),
                                SessionInputState(2, "Sesi 2 (09.20/10.20)"),
                                SessionInputState(3, "Sesi 3 (13.30)")
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore parse error while typing
        }
    }

    // Hitung otomatis bobot kehadiran berdasarkan jumlah sesi yang dicentang
    LaunchedEffect(scheduleList.size, scheduleList.sumOf { it.sessions.count { s -> s.isSelected } }) {
        val totalSesi = scheduleList.sumOf { it.sessions.count { s -> s.isSelected } }
        if (bobotKehadiran == "0" || bobotKehadiran.isEmpty()) {
            bobotKehadiran = (totalSesi * 2).toString()
        }
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title ="Ajukan Izin",
                canNavigateBack = true,
                navigateUp = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(SurfaceWhite)
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Jenis & Tanggal
                Text("Informasi Dasar", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)

                DropdownInput(
                    value = jenisIzin,
                    options = listOf("SAKIT", "DISPENSASI_INSTITUSI", "IZIN_ALASAN_PENTING"),
                    onSelectionChanged = { jenisIzin = it }
                )

                val detailOptions = when(jenisIzin) {
                    "SAKIT" -> listOf("RAWAT_JALAN", "RAWAT_INAP")
                    "DISPENSASI_INSTITUSI" -> listOf("DISPENSASI")
                    else -> listOf("KELUARGA_INTI_MENINGGAL", "BENCANA", "PASANGAN_MELAHIRKAN")
                }
                LaunchedEffect(jenisIzin) {
                    if(detailIzin !in detailOptions) detailIzin = detailOptions.firstOrNull() ?: ""
                }
                DropdownInput(
                    value = detailIzin,
                    options = detailOptions,
                    onSelectionChanged = { detailIzin = it }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Tanggal Mulai
                    OutlinedTextField(
                        value = tanggalMulai,
                        onValueChange = {},
                        label = { Text("Mulai", fontSize = 12.sp, fontFamily = poppinsFont) },
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                        enabled = false, // Biar klik lewat trailing icon atau area
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = BorderGray,
                            disabledLabelColor = Color.Gray
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, null, tint = MainBlue)
                            }
                        }
                    )

                    // Durasi
                    OutlinedTextField(
                        value = durasiHari,
                        onValueChange = {
                            if(it.all { char -> char.isDigit() }) durasiHari = it
                            durasiError = false
                        },
                        label = { Text("Durasi (Hari)", fontSize = 12.sp, fontFamily = poppinsFont) },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = durasiError
                    )
                }

                Divider(color = BorderGray.copy(alpha = 0.5f))

                // Detail Sesi (Dynamic List)
                Text("Detail Perkuliahan", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)
                Text(
                    "Centang sesi yang ingin diizinkan, lalu isi Mata Kuliah & Dosen.",
                    fontSize = 12.sp, color = Color.Gray, lineHeight = 14.sp
                )

                if (scheduleError) {
                    Text("Mohon lengkapi Matkul & Dosen pada sesi yang dipilih!", color = AlertRed, fontSize = 12.sp)
                }

                // Loop per Hari
                scheduleList.forEachIndexed { dayIndex, dayData ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header Tanggal
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, null, tint = MainBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dayData.date.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = MainBlue,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Pilihan Sesi (Checkbox)
                            Text("Pilih Sesi:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                dayData.sessions.forEach { session ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = session.isSelected,
                                            onCheckedChange = { isChecked ->
                                                // Update state khusus item ini
                                                scheduleList[dayIndex] = dayData.copy(
                                                    sessions = dayData.sessions.map {
                                                        if (it.id == session.id) it.copy(isSelected = isChecked) else it
                                                    }
                                                )
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = MainBlue)
                                        )
                                        Text("Sesi ${session.id}", fontSize = 12.sp)
                                    }
                                }
                            }

                            // Form Input (Muncul jika dicentang)
                            dayData.sessions.forEach { session ->
                                AnimatedVisibility(
                                    visible = session.isSelected,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, BorderGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            session.label,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MainBlue
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Matkul Input
                                        OutlinedTextField(
                                            value = session.matkul,
                                            onValueChange = { newVal ->
                                                scheduleList[dayIndex] = dayData.copy(
                                                    sessions = dayData.sessions.map {
                                                        if (it.id == session.id) it.copy(matkul = newVal) else it
                                                    }
                                                )
                                            },
                                            label = { Text("Mata Kuliah", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Dosen Input
                                        OutlinedTextField(
                                            value = session.dosen,
                                            onValueChange = { newVal ->
                                                scheduleList[dayIndex] = dayData.copy(
                                                    sessions = dayData.sessions.map {
                                                        if (it.id == session.id) it.copy(dosen = newVal) else it
                                                    }
                                                )
                                            },
                                            label = { Text("Nama Dosen", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                            singleLine = true,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Deskripsi & File
                OutlinedTextField(
                    value = bobotKehadiran,
                    onValueChange = { if(it.all { c -> c.isDigit() }) bobotKehadiran = it },
                    label = { Text("Total Bobot (Jam/SKS)", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = bobotError
                )

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it; deskripsiError = false },
                    label = { Text("Alasan Lengkap", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    isError = deskripsiError
                )

                Button(
                    onClick = { fileLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Lampiran", fontWeight = FontWeight.Bold)
                }

                if (selectedUris.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(selectedUris) { uri ->
                            InputChip(
                                selected = true,
                                onClick = { selectedUris.remove(uri) },
                                label = { Text(getFileName(context, uri).take(15), fontSize = 11.sp) },
                                trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                //  SUBMIT
                Button(
                    onClick = {
                        // 1. Validasi Dasar
                        var isValid = true
                        if (durasiHari.toIntOrNull() == null || durasiHari.toInt() <= 0) { durasiError = true; isValid = false }
                        if (deskripsi.isBlank()) { deskripsiError = true; isValid = false }
                        if (bobotKehadiran.isBlank()) { bobotError = true; isValid = false }
                        if (selectedUris.isEmpty()) {
                            Toast.makeText(context, "Upload minimal 1 dokumen", Toast.LENGTH_SHORT).show(); return@Button
                        }

                        //Validasi Jadwal
                        val anyEmptyField = scheduleList.any { day ->
                            day.sessions.any { it.isSelected && (it.matkul.isBlank() || it.dosen.isBlank()) }
                        }
                        val anySessionSelected = scheduleList.any { day -> day.sessions.any { it.isSelected } }

                        if (anyEmptyField) {
                            scheduleError = true
                            Toast.makeText(context, "Lengkapi Nama Matkul & Dosen pada sesi yang dicentang!", Toast.LENGTH_LONG).show()
                            isValid = false
                        } else if (!anySessionSelected) {
                            Toast.makeText(context, "Pilih minimal satu sesi!", Toast.LENGTH_SHORT).show()
                            isValid = false
                        }

                        if (!isValid) return@Button

                        scope.launch {
                            isSubmitting = true
                            try {
                                //  CONVERT UI STATE TO DTO
                                val finalStart = LocalDate.parse(tanggalMulai)
                                val finalEnd = finalStart.plusDays((durasiHari.toInt() - 1).toLong()).toString()

                                val listSesiDto = mutableListOf<DetailSesiIzinDto>()

                                scheduleList.forEach { day ->
                                    day.sessions.forEach { session ->
                                        if (session.isSelected) {
                                            listSesiDto.add(
                                                DetailSesiIzinDto(
                                                    tanggal = day.date.toString(),
                                                    namaMataKuliah = session.matkul,
                                                    namaDosen = session.dosen,
                                                    sesi1 = (session.id == 1),
                                                    sesi2 = (session.id == 2),
                                                    sesi3 = (session.id == 3)
                                                )
                                            )
                                        }
                                    }
                                }

                                val izinData = PerizinanDto(
                                    id = 0,
                                    mahasiswaId = mahasiswaId,
                                    mahasiswaNama = mahasiswaNama,
                                    jenisIzin = jenisIzin,
                                    detailIzin = detailIzin,
                                    tanggalMulai = tanggalMulai,
                                    tanggalSelesai = finalEnd,
                                    deskripsi = deskripsi,
                                    bobotKehadiran = bobotKehadiran.toIntOrNull() ?: 0,
                                    status = "PENDING",
                                    catatanAdmin = null,
                                    daftarBerkas = emptyList(),
                                    daftarSesi = listSesiDto
                                )

                                val gson = Gson()
                                val jsonString = gson.toJson(izinData)
                                val izinPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                                val fileParts = prepareFileParts(context, selectedUris)

                                val api = RetrofitInstance.getApi(context)
                                api.ajukanIzin(izinPart, fileParts)

                                Toast.makeText(context, "Berhasil diajukan!", Toast.LENGTH_LONG).show()
                                navController.popBackStack()

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White) else Text("AJUKAN IZIN", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        tanggalMulai = selectedDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK", fontFamily = poppinsFont) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel", fontFamily = poppinsFont) } }
        ) { DatePicker(state = datePickerState) }
    }
}

fun prepareFileParts(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
    return uris.mapNotNull { uri ->
        try {
            val contentResolver = context.contentResolver
            val tempFile = File.createTempFile("upload", ".tmp", context.cacheDir)
            tempFile.deleteOnExit()

            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

            MultipartBody.Part.createFormData("berkas", getFileName(context, uri), requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(index >= 0) result = cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown"
}