package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.GantiKataSandiDto
import com.example.silpa.model.PerbaruiProfilDto
import com.example.silpa.ui.components.SilpaTopAppBar
import com.example.silpa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    sessionManager: SessionManager,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    var isEditingProfile by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val res = RetrofitInstance.getApi(context).getProfil()
            if (res.berhasil && res.data != null) {
                nama = res.data.namaLengkap
                email = res.data.email
                role = res.data.peran
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal memuat profil", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (showPasswordDialog) {
        GantiPasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSubmit = { lama, baru ->
                scope.launch {
                    try {
                        val api = RetrofitInstance.getApi(context)
                        val res = api.gantiKataSandi(GantiKataSandiDto(lama, baru))
                        Toast.makeText(context, res.pesan, Toast.LENGTH_SHORT).show()
                        if (res.berhasil) showPasswordDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Gagal ganti password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            SilpaTopAppBar(
                title = "Profil Saya",
                canNavigateBack = false
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //  KARTU UTAMA PROFIL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BorderBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header: Avatar (
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = BackgroundLight,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    tint = AccentBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Role Badge
                        Surface(
                            color = MainBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = role,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = MainBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Form / Info Section
                        if (isEditingProfile) {
                            OutlinedTextField(
                                value = nama,
                                onValueChange = { nama = it },
                                label = { Text("Nama Lengkap") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MainBlue,
                                    unfocusedBorderColor = BorderGray
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MainBlue,
                                    unfocusedBorderColor = BorderGray
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            // Tombol Aksi Edit
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { isEditingProfile = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight, contentColor = TextGray),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) { Text("Batal") }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val res = RetrofitInstance.getApi(context).updateProfil(PerbaruiProfilDto(nama, email))
                                                Toast.makeText(context, res.pesan, Toast.LENGTH_SHORT).show()
                                                isEditingProfile = false
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) { Text("Simpan") }
                            }
                        } else {
                            // Tampilan Info (Read-Only)
                            InfoRowMinimal("Nama Lengkap", nama)
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGray.copy(alpha = 0.5f))
                            InfoRowMinimal("Email", email)

                            Spacer(modifier = Modifier.height(32.dp))

                            // Tombol-tombol Aksi
                            Button(
                                onClick = { isEditingProfile = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit Profil", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { showPasswordDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextBlack)
                            ) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ganti Kata Sandi")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Tombol Keluar (Merah tapi soft)
                            TextButton(
                                onClick = {
                                    sessionManager.clearSession()
                                    onLogout()
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.textButtonColors(contentColor = AlertRed)
                            ) {
                                Text("Keluar Akun", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Versi Aplikasi 1.0.0",
                    fontSize = 12.sp,
                    color = TextGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun InfoRowMinimal(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = TextBlack,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun GantiPasswordDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var lama by remember { mutableStateOf("") }
    var baru by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = { Text("Ganti Kata Sandi", color = TextBlack, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = lama,
                    onValueChange = { lama = it },
                    label = { Text("Sandi Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = baru,
                    onValueChange = { baru = it },
                    label = { Text("Sandi Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(lama, baru) },
                colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
            ) { Text("Batal") }
        }
    )
}