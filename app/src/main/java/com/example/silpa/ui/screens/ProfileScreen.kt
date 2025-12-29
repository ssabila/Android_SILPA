package com.example.silpa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.silpa.R
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.model.GantiKataSandiDto
import com.example.silpa.model.PerbaruiProfilDto
import com.example.silpa.ui.theme.*
import com.example.silpa.ui.theme.poppinsFont
import com.example.silpa.ui.components.*
import kotlinx.coroutines.launch

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

    // Load data profil saat layar dibuka
    LaunchedEffect(Unit) {
        try {
            val res = RetrofitInstance.getApi(context).getProfil()
            if(res.berhasil && res.data != null) {
                nama = res.data.namaLengkap
                email = res.data.email
                role = res.data.peran
            }
        } catch (e: Exception) {
            // Handle error silent
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
                        if(res.berhasil) showPasswordDialog = false
                    } catch(e: Exception) {
                        Toast.makeText(context, "Gagal ganti password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // Header dengan Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MainBlue)
                .padding(vertical = 40.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(48.dp), tint = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = nama,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = role.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            if (isEditingProfile) {
                // Mode Edit
                Text("Edit Profil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextBlack, modifier = Modifier.padding(bottom = 16.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                        cursorColor = MainBlue
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MainBlue,
                        unfocusedBorderColor = BorderGray.copy(alpha = 0.3f),
                        cursorColor = MainBlue
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditingProfile = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderGray)
                    ) {
                        Text("Batal", color = TextBlack, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val res = RetrofitInstance.getApi(context).updateProfil(PerbaruiProfilDto(nama, email))
                                    Toast.makeText(context, res.pesan, Toast.LENGTH_SHORT).show()
                                    isEditingProfile = false
                                } catch (e: Exception) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MainBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Mode Tampil (View)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nama Lengkap", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(nama, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGray.copy(alpha = 0.3f))
                        Text("Email", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(email, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextBlack)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { isEditingProfile = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MainBlue)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ganti Password", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { sessionManager.clearSession(); onLogout() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AlertRed)
                ) {
                    Text("Keluar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GantiPasswordDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var lama by remember { mutableStateOf("") }
    var baru by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ganti Kata Sandi") },
        text = {
            Column {
                OutlinedTextField(
                    value = lama,
                    onValueChange = { lama = it },
                    label = { Text("Sandi Lama") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = baru,
                    onValueChange = { baru = it },
                    label = { Text("Sandi Baru") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(lama, baru) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

