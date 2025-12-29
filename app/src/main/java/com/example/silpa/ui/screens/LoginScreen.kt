package com.example.silpa.ui.screens

import android.widget.Toast
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
import com.example.silpa.data.SessionManager
import com.example.silpa.model.LoginDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun LoginScreen(
    navController: NavController,
    sessionManager: SessionManager,
    onLoginSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // State Error Feedback
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Dialog Error
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = AlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login Gagal", color = AlertRed, fontFamily = poppinsFont)
                }
            },
            text = { Text(errorMessage, fontFamily = poppinsFont) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("Coba Lagi") }
            }
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
                        "Selamat Datang",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppinsFont
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Login untuk melanjutkan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        fontFamily = poppinsFont
                    )
                }
            }

            // Form Container
            Column(modifier = Modifier.fillMaxWidth()) {
                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextGray, fontFamily = poppinsFont) },
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
                    label = { Text("Kata Sandi", color = TextGray, fontFamily = poppinsFont) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                null,
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
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            val api = RetrofitInstance.getApi(context)
                            val response = api.login(LoginDto(email, password))

                            if (response.berhasil && response.data != null) {
                                sessionManager.saveToken(response.data.accessToken)
                                val profil = api.getProfil()
                                if (profil.berhasil) {
                                    val role = profil.data?.peran ?: "MAHASISWA"
                                    onLoginSuccess(role)
                                } else {
                                    errorMessage = "Gagal mengambil profil user."
                                    showErrorDialog = true
                                }
                            } else {
                                errorMessage = response.pesan
                                showErrorDialog = true
                            }
                        } catch (e: Exception) {
                            errorMessage = when(e) {
                                is HttpException -> {
                                    when(e.code()) {
                                        401 -> "Email atau kata sandi salah. Silakan periksa kembali."
                                        403 -> "Akun dinonaktifkan atau akses ditolak."
                                        404 -> "Akun tidak ditemukan."
                                        500 -> "Terjadi kesalahan pada server. Coba lagi nanti."
                                        else -> "Gagal login (Kode: ${e.code()})."
                                    }
                                }
                                is IOException -> "Tidak ada koneksi internet. Periksa jaringan Anda."
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
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
            ) {
                if(isLoading) {
                    CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(20.dp))
                } else {
                    Text("MASUK", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = poppinsFont)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Belum punya akun? ", color = TextGray, fontSize = 14.sp, fontFamily = poppinsFont)
                TextButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Daftar", color = MainBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = poppinsFont)
                }
            }
        }
    }
}