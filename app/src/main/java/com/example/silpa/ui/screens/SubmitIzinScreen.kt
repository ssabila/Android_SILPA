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
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeParseException
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitIzinScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form State
    var jenisIzin by remember { mutableStateOf("SAKIT") }
    var detailIzin by remember { mutableStateOf("RAWAT_JALAN") }

    // Input Data Sesi
    var tanggalMulai by remember { mutableStateOf("2025-01-01") }
    var durasiHari by remember { mutableStateOf("1") }
    var matkul by remember { mutableStateOf("-") }
    var dosen by remember { mutableStateOf("-") }
    var deskripsi by remember { mutableStateOf("") }

    // Error State
    var tanggalError by remember { mutableStateOf(false) }
    var durasiError by remember { mutableStateOf(false) }
    var deskripsiError by remember { mutableStateOf(false) }
    var matkulError by remember { mutableStateOf(false) }
    var dosenError by remember { mutableStateOf(false) }

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
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(MainBlue, MainBlue.copy(alpha = 0.8f))
                        )
                    )
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                Column {
                    Text(
                        "Ajukan Izin",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Isi form dengan data yang lengkap dan akurat",
                        fontSize = 13.sp,
                        color = SurfaceWhite.copy(alpha = 0.85f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- FORM INPUT ---
                Text("Jenis Perizinan", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)

                // Dropdown Jenis Izin
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

                // --- INPUT SESI ---
                Spacer(modifier = Modifier.height(8.dp))
                Text("Jadwal Perkuliahan", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = tanggalMulai,
                            onValueChange = {
                                tanggalMulai = it
                                tanggalError = false
                            },
                            label = { Text("Tanggal Mulai (YYYY-MM-DD)", fontSize = 14.sp, fontFamily = poppinsFont) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(24.dp)) },
                            isError = tanggalError,
                            supportingText = { if (tanggalError) Text("Format tanggal salah!", color = AlertRed, fontSize = 11.sp, fontFamily = poppinsFont) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MainBlue,
                                unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                                cursorColor = MainBlue
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontFamily = poppinsFont),
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MainBlue)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = durasiHari,
                            onValueChange = {
                                if(it.all { char -> char.isDigit() }) durasiHari = it
                                durasiError = false
                            },
                            label = { Text("Lama Izin (Hari)", fontSize = 14.sp, fontFamily = poppinsFont) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = durasiError,
                            supportingText = { if (durasiError) Text("Wajib diisi (>0)", color = AlertRed, fontSize = 11.sp, fontFamily = poppinsFont) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MainBlue,
                                unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                                cursorColor = MainBlue
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontFamily = poppinsFont),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = matkul,
                            onValueChange = { matkul = it; matkulError = false },
                            label = { Text("Mata Kuliah", fontSize = 14.sp, fontFamily = poppinsFont) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            isError = matkulError,
                            supportingText = { if (matkulError) Text("Wajib diisi", color = AlertRed, fontSize = 11.sp, fontFamily = poppinsFont) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MainBlue,
                                unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                                cursorColor = MainBlue
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontFamily = poppinsFont),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dosen,
                            onValueChange = { dosen = it; dosenError = false },
                            label = { Text("Dosen Pengampu", fontSize = 14.sp, fontFamily = poppinsFont) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            shape = RoundedCornerShape(12.dp),
                            isError = dosenError,
                            supportingText = { if (dosenError) Text("Wajib diisi", color = AlertRed, fontSize = 11.sp, fontFamily = poppinsFont) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MainBlue,
                                unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                                cursorColor = MainBlue
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontFamily = poppinsFont),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Alasan Pengajuan", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it; deskripsiError = false },
                    label = { Text("Deskripsikan alasan lengkap Anda", fontSize = 14.sp, fontFamily = poppinsFont) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    isError = deskripsiError,
                    supportingText = { if (deskripsiError) Text("Wajib diisi", color = AlertRed, fontSize = 11.sp, fontFamily = poppinsFont) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                        cursorColor = MainBlue
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontFamily = poppinsFont)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Dokumen Pendukung", fontWeight = FontWeight.Bold, color = TextBlack, fontSize = 16.sp)

                Button(
                    onClick = { fileLauncher.launch("*/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih File", fontWeight = FontWeight.Bold)
                }

                if (selectedUris.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(selectedUris) { uri ->
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedUris.remove(uri) },
                                        label = { Text(getFileName(context, uri).take(15), fontSize = 11.sp) },
                                        trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) },
                                        colors = InputChipDefaults.inputChipColors(
                                            containerColor = MainBlue.copy(alpha = 0.1f),
                                            labelColor = MainBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = AlertRed.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Minimal 1 dokumen harus diunggah", fontSize = 12.sp, color = AlertRed, fontWeight = FontWeight.Medium, modifier = Modifier.padding(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- SUBMIT BUTTON ---
                Button(
                    onClick = {
                        // --- VALIDASI INPUT ---
                        var isValid = true

                        // Cek Tanggal
                        try {
                            LocalDate.parse(tanggalMulai)
                        } catch (e: DateTimeParseException) {
                            tanggalError = true
                            isValid = false
                        }

                        if (durasiHari.toIntOrNull() == null || durasiHari.toInt() <= 0) {
                            durasiError = true
                            isValid = false
                        }
                        if (matkul.isBlank()) { matkulError = true; isValid = false }
                        if (dosen.isBlank()) { dosenError = true; isValid = false }
                        if (deskripsi.isBlank()) { deskripsiError = true; isValid = false }

                        if (selectedUris.isEmpty()) {
                            Toast.makeText(context, "Upload minimal 1 dokumen", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (!isValid) {
                            Toast.makeText(context, "Mohon lengkapi data yang salah!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        scope.launch {
                            isSubmitting = true
                            try {
                                // --- 1. GENERATE LIST SESI ---
                                val durasi = durasiHari.toInt()
                                val start = LocalDate.parse(tanggalMulai)

                                val listSesi = mutableListOf<DetailSesiIzinDto>()
                                for(i in 0 until durasi) {
                                    val tgl = start.plusDays(i.toLong()).toString()
                                    listSesi.add(DetailSesiIzinDto(
                                        tanggal = tgl,
                                        namaMataKuliah = matkul,
                                        namaDosen = dosen,
                                        sesi1 = true, sesi2 = true, sesi3 = true
                                    ))
                                }

                                // --- 2. Buat DTO ---
                                val izinData = AjukanIzinDto(
                                    jenisIzin = jenisIzin,
                                    detailIzin = detailIzin,
                                    deskripsi = deskripsi,
                                    daftarSesi = listSesi // List Sesi Lengkap
                                )

                                // --- 3. Convert ke Multipart ---
                                val gson = Gson()
                                val jsonString = gson.toJson(izinData)
                                val izinPart = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                                val fileParts = prepareFileParts(context, selectedUris)

                                // --- 4. Kirim ke API ---
                                val api = RetrofitInstance.getApi(context)
                                api.ajukanIzin(izinPart, fileParts)

                                Toast.makeText(context, "Berhasil diajukan!", Toast.LENGTH_LONG).show()
                                navController.popBackStack()

                        } catch (e: Exception) {
                            e.printStackTrace()
                            val msg = when(e) {
                                is HttpException -> {
                                    when(e.code()) {
                                        400 -> "Data tidak lengkap atau format salah."
                                        413 -> "Ukuran file terlalu besar."
                                        500 -> "Server bermasalah."
                                        else -> "Gagal mengirim (Error ${e.code()})"
                                    }
                                }
                                is IOException -> "Gagal terhubung. Periksa internet Anda."
                                else -> "Error: ${e.localizedMessage}"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                enabled = !isSubmitting
            ) {
                if(isSubmitting) {
                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                } else {
                    Text("AJUKAN IZIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = poppinsFont)
                }
            }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                LocalDate.parse(tanggalMulai).toEpochDay() * 86400 * 1000
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            tanggalMulai = selectedDate.toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", fontFamily = poppinsFont)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel", fontFamily = poppinsFont)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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

// Helper function to handle date picker dialog display in the composable
// This will be called from the SubmitIzinScreen composition

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