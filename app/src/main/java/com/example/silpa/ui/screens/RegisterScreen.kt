package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.RegisterDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun RegisterScreen(navController: NavController) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // State untuk toggle visibility password
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Menghitung kekuatan password dan warnanya
    val (passwordStrength, strengthColor) = calculatePasswordStrength(password)
    // Animasi halus untuk progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = passwordStrength,
        animationSpec = tween(durationMillis = 500), label = "progressAnim"
    )

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Row { Icon(Icons.Default.Error, null, tint = AlertRed); Spacer(Modifier.width(8.dp)); Text("Gagal Daftar", color = AlertRed, fontFamily = poppinsFont) } },
            text = { Text(errorMessage, fontFamily = poppinsFont) },
            confirmButton = { TextButton(onClick = { showErrorDialog = false }) { Text("OK", fontFamily = poppinsFont) } }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(SurfaceWhite)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header dengan gradient background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = MainBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, "Logo", tint = MainBlue, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Buat Akun Baru",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextBlack,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Daftar untuk memulai",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            }

            // Form Container
            Column(modifier = Modifier.fillMaxWidth()) {
                // Nama Field
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap", color = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = MainBlue
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = MainBlue
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi", color = TextGray) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Sembunyikan Sandi" else "Tampilkan Sandi",
                                tint = if (passwordVisible) MainBlue else TextGray
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = MainBlue
                    ),
                    singleLine = true
                )

                // Indikator Kekuatan Password
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = strengthColor,
                        trackColor = BorderGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = getStrengthLabel(passwordStrength),
                        fontSize = 12.sp,
                        color = strengthColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    if (nama.isBlank() || email.isBlank() || password.isBlank()) {
                        errorMessage = "Semua field harus diisi!"
                        showErrorDialog = true
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Kata sandi minimal 6 karakter!"
                        showErrorDialog = true
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        try {
                            val api = RetrofitInstance.getApi(context)
                            val response = api.register(RegisterDto(nama, email, password))

                            if (response.berhasil) {
                                Toast.makeText(context, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            } else {
                                errorMessage = response.pesan
                                showErrorDialog = true
                            }
                        } catch (e: Exception) {
                            errorMessage = when(e) {
                                is HttpException -> if (e.code() == 409) "Email sudah terdaftar." else "Gagal mendaftar (Error ${e.code()})"
                                is IOException -> "Masalah koneksi internet."
                                else -> "Terjadi kesalahan: ${e.localizedMessage}"
                            }
                            showErrorDialog = true
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                } else {
                    Text("DAFTAR", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sudah punya akun? ", color = TextGray, fontSize = 14.sp)
                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Login", color = MainBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}


fun calculatePasswordStrength(password: String): Pair<Float, Color> {
    if (password.isEmpty()) return 0f to BorderGray
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++ // Simbol

    return when (score) {
        0, 1 -> 0.2f to AlertRed       // Sangat Lemah
        2 -> 0.4f to WarningYellow     // Lemah
        3 -> 0.6f to Color.Yellow      // Sedang
        4 -> 0.8f to SuccessGreen.copy(alpha = 0.7f) // Kuat
        5 -> 1.0f to SuccessGreen      // Sangat Kuat
        else -> 0.2f to AlertRed
    }
}

fun getStrengthLabel(strength: Float): String {
    return when {
        strength <= 0.2f -> "Sangat Lemah"
        strength <= 0.4f -> "Lemah"
        strength <= 0.6f -> "Sedang"
        strength <= 0.8f -> "Kuat"
        else -> "Sangat Kuat"
    }
}