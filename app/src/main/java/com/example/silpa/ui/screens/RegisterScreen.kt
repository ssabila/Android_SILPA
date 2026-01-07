package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.model.RegisterDto
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun RegisterScreen(navController: NavController) {
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val (passwordStrength, strengthColor) = calculatePasswordStrength(password)
    val animatedProgress by animateFloatAsState(targetValue = passwordStrength, animationSpec = tween(500), label = "strength")

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Row { Icon(Icons.Default.Error, null, tint = AlertRed); Spacer(Modifier.width(8.dp)); Text("Gagal Daftar", color = AlertRed) } },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = { showErrorDialog = false }) { Text("OK") } }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp), // Padding luar agar card tidak mepet layar
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()), // Agar bisa discroll di layar kecil
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MainBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // LOGO
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.silpafix),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Buat Akun Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MainBlue,
                    fontWeight = FontWeight.Bold
                )

                // INPUT FIELDS
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = nama, onValueChange = { nama = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MainBlue, unfocusedBorderColor = BorderGray)
                    )
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MainBlue, unfocusedBorderColor = BorderGray)
                    )
                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = TextGray) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MainBlue, unfocusedBorderColor = BorderGray)
                    )

                    // Strength Meter
                    if (password.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = animatedProgress,
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = strengthColor,
                            trackColor = BorderGray
                        )
                        Text(text = getStrengthLabel(passwordStrength), fontSize = 12.sp, color = strengthColor)
                    }
                }

                Button(
                    onClick = {
                        if (nama.isBlank() || email.isBlank() || password.isBlank()) {
                            errorMessage = "Semua field harus diisi!"; showErrorDialog = true; return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "Kata sandi minimal 6 karakter!"; showErrorDialog = true; return@Button
                        }
                        scope.launch {
                            isLoading = true
                            try {
                                val api = RetrofitInstance.getApi(context)
                                val response = api.register(RegisterDto(nama, email, password))
                                if (response.berhasil) {
                                    Toast.makeText(context, "Registrasi Berhasil! Silakan Login.", Toast.LENGTH_LONG).show()
                                    navController.popBackStack()
                                } else { errorMessage = response.pesan; showErrorDialog = true }
                            } catch (e: Exception) {
                                errorMessage = when(e) {
                                    is HttpException -> if (e.code() == 409) "Email sudah terdaftar." else "Gagal mendaftar."
                                    is IOException -> "Masalah koneksi internet."
                                    else -> "Error: ${e.localizedMessage}"
                                }
                                showErrorDialog = true
                            } finally { isLoading = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White) else Text("DAFTAR", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Sudah punya akun? Kembali ke Login", color = TextGray)
                }
            }
        }
    }
}

// Helper (Sama seperti sebelumnya)
fun calculatePasswordStrength(password: String): Pair<Float, Color> {
    if (password.isEmpty()) return 0f to Color.LightGray
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return when (score) {
        0, 1 -> 0.2f to AlertRed
        2 -> 0.4f to WarningYellow
        3 -> 0.6f to Color.Yellow
        4 -> 0.8f to SuccessGreen
        5 -> 1.0f to SuccessGreen
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