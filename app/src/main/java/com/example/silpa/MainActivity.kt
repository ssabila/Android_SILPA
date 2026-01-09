package com.example.silpa

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.silpa.ui.theme.AccentBlue
import com.example.silpa.ui.theme.BackgroundLight
import com.example.silpa.ui.theme.BorderBlue
import com.example.silpa.ui.theme.SurfaceWhite
import com.example.silpa.ui.theme.MainBlue
import com.example.silpa.ui.theme.BorderGray

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    // Daftar route yang memerlukan autentikasi (protected routes)
    val protectedRoutes = listOf(
        "dashboard", "history", "notifications", "profile", "submit_izin", "detail_izin",
        "admin_dashboard", "admin_history", "admin_notifications", "admin_profile",
        "admin_validasi_list", "admin_validasi", "admin_mahasiswa", "admin_mahasiswa_detail", "admin_statistik"
    )

    // Redirect ke login jika user belum login dan mencoba akses protected route
    LaunchedEffect(currentRoute, isLoggedIn.value) {
        val routeBase = currentRoute?.split("/")?.firstOrNull() ?: ""
        if (!isLoggedIn.value && routeBase in protectedRoutes) {
            navController.navigate("login") {
                popUpTo("landing") { inclusive = false }
            }
        }
    }

    // Navbar muncul di landing dan route utama lainnya
    val showBottomBar = currentRoute in listOf(
        "dashboard", "history", "landing", "notifications", "profile",
        "admin_dashboard", "admin_history", "admin_notifications", "admin_profile"
    )

    Scaffold(
        containerColor = BackgroundLight,
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = SurfaceWhite,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .padding(bottom = 0.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (isAdmin) {
                            //  MENU ADMIN
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
                            //  MENU MAHASISWA
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn.value) {
                if (userRole == "ADMIN") "admin_dashboard" else "dashboard"
            } else "landing",
            // Hanya ambil bottom padding agar kandungan tidak tertutup navbar, tapi biarkan top padding (status bar) diurus skrin masing-masing
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
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

            // Route Detail Mahasiswa Admin
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