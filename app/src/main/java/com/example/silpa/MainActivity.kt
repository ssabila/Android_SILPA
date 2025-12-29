package com.example.silpa

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // Import Penting
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.silpa.data.RetrofitInstance
import com.example.silpa.data.SessionManager
import com.example.silpa.ui.screens.*
import com.example.silpa.ui.theme.SilpaTheme
import com.example.silpa.ui.theme.*

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Aktifkan Edge-to-Edge agar status bar transparan & full screen
        enableEdgeToEdge()

        setContent {
            SilpaTheme {
                SilpaApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SilpaApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var userRole by remember { mutableStateOf<String?>(null) }
    val isLoggedIn = remember { mutableStateOf(sessionManager.getToken() != null) }

    // Cek Role Awal
    LaunchedEffect(isLoggedIn.value) {
        if (isLoggedIn.value) {
            try {
                val profil = RetrofitInstance.getApi(context).getProfil()
                if (profil.berhasil) {
                    userRole = profil.data?.peran ?: "MAHASISWA"
                } else {
                    sessionManager.clearSession()
                    isLoggedIn.value = false
                }
            } catch (e: Exception) {
                sessionManager.clearSession()
                isLoggedIn.value = false
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAdmin = userRole == "ADMIN"

    val showBottomBar = isLoggedIn.value && currentRoute in listOf(
        "dashboard", "history", "landing", "notifications", "profile",
        "admin_dashboard", "admin_history", "admin_notifications", "admin_profile"
    )

    Scaffold(
        // Tambahkan warna container transparan/putih agar aman
        containerColor = Color.White,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    if (isAdmin) {
                        // --- MENU ADMIN ---
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Home, "Home") },
                            label = { Text("Home") },
                            selected = currentRoute == "admin_dashboard",
                            onClick = { navController.navigate("admin_dashboard") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.History, "History") },
                            label = { Text("History") },
                            selected = currentRoute == "admin_history",
                            onClick = { navController.navigate("admin_history") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Notifications, "Notifikasi") },
                            label = { Text("Info") },
                            selected = currentRoute == "admin_notifications",
                            onClick = { navController.navigate("admin_notifications") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Person, "Profil") },
                            label = { Text("Profil") },
                            selected = currentRoute == "admin_profile",
                            onClick = { navController.navigate("admin_profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                    } else {
                        // --- MENU MAHASISWA ---
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Home, "Beranda") },
                            label = { Text("Beranda") },
                            selected = currentRoute == "dashboard",
                            onClick = { navController.navigate("dashboard") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Info, "Info Izin") },
                            label = { Text("Info") },
                            selected = currentRoute == "landing",
                            onClick = { navController.navigate("landing") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.History, "Riwayat") },
                            label = { Text("Riwayat") },
                            selected = currentRoute == "history",
                            onClick = { navController.navigate("history") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Notifications, "Pesan") },
                            label = { Text("Pesan") },
                            selected = currentRoute == "notifications",
                            onClick = { navController.navigate("notifications") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Outlined.Person, "Profil") },
                            label = { Text("Profil") },
                            selected = currentRoute == "profile",
                            onClick = { navController.navigate("profile") },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MainBlue, indicatorColor = BorderGray.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // 2. PERBAIKAN PADDING:
        // Jangan gunakan 'innerPadding' mentah-mentah karena itu berisi top padding (status bar).
        // Kita hanya butuh bottom padding (untuk navigasi bawah).
        // Padding atas akan ditangani oleh Scaffold di masing-masing screen (TopAppBar).
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn.value) {
                if (userRole == "ADMIN") "admin_dashboard" else "dashboard"
            } else "landing",
            // Hanya terapkan padding bawah, biarkan padding atas 0 agar TopBar naik ke status bar
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ... (Isi NavHost SAMA SEPERTI SEBELUMNYA, tidak ada perubahan rute) ...

            // --- PUBLIC ---
            composable("landing") { LandingScreen(navController) }
            composable("login") {
                LoginScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    onLoginSuccess = { role ->
                        userRole = role
                        isLoggedIn.value = true
                        val dest = if(role == "ADMIN") "admin_dashboard" else "dashboard"
                        navController.navigate(dest) { popUpTo("landing") { inclusive = true } }
                    }
                )
            }
            composable("register") { RegisterScreen(navController) }

            // --- MAHASISWA ROUTES ---
            composable("dashboard") { DashboardScreen(navController) }
            composable("history") { HistoryScreen(navController) }
            composable("notifications") { NotificationScreen(navController) }
            composable("submit_izin") { SubmitIzinScreen(navController) }
            composable("profile") {
                ProfileScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    onLogout = {
                        isLoggedIn.value = false
                        navController.navigate("landing") { popUpTo(0) }
                    }
                )
            }

            composable("detail_izin/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                val id = it.arguments?.getLong("id") ?: 0L
                DetailIzinScreen(navController, id)
            }
            composable("revisi_izin/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                val id = it.arguments?.getLong("id") ?: 0L
                RevisiIzinScreen(navController, id)
            }

            // --- ADMIN ROUTES ---
            composable("admin_dashboard") { AdminDashboardScreen(navController, sessionManager) }
            composable("admin_validasi_list") { AdminValidasiListScreen(navController) }
            composable(
                "admin_validasi/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                AdminValidasiScreen(navController, id)
            }
            composable("admin_history") { AdminHistoryScreen(navController) }
            composable("admin_mahasiswa") { AdminMahasiswaScreen(navController) }
            composable(
                "admin_mahasiswa_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                AdminMahasiswaDetailScreen(navController, id)
            }
            composable("admin_statistik") { AdminStatistikScreen(navController) }
            composable("admin_notifications") { NotificationScreen(navController) }
            composable("admin_profile") {
                ProfileScreen(
                    navController = navController,
                    sessionManager = sessionManager,
                    onLogout = {
                        isLoggedIn.value = false
                        navController.navigate("landing") { popUpTo(0) }
                    }
                )
            }
        }
    }
}