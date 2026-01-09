package com.example.silpa.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.LoginDto
import com.example.silpa.ui.theme.*
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

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = AlertRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login Gagal", color = AlertRed)
                }
            },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("Coba Lagi") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight), // Background abu-abu muda
        contentAlignment = Alignment.Center
    ) {
        // CARD CONTAINER UTAMA
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MainBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // LOGO SILPA BESAR & ROUND
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.silpafix),
                        contentDescription = "Logo SILPA",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // Crop agar pas lingkaran
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Selamat Datang",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MainBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Silakan masuk ke akun Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }

                // FORM INPUT
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainBlue,
                            unfocusedBorderColor = BorderGray
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Kata Sandi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MainBlue,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                }

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
                                    is HttpException -> if (e.code() == 401) "Email atau kata sandi salah." else "Gagal login (Error ${e.code()})"
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
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    if(isLoading) CircularProgressIndicator(color = Color.White)
                    else Text("MASUK", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = { navController.navigate("register") }) {
                    Text("Belum punya akun? Daftar", color = MainBlue)
                }
            }
        }
    }
}