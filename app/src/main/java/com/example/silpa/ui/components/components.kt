package com.example.silpa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silpa.model.PerizinanDto
import com.example.silpa.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilpaTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp // Ukuran font responsif yang wajar
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MainBlue, // Konsisten warna biru
            titleContentColor = SurfaceWhite,
            navigationIconContentColor = SurfaceWhite,
            actionIconContentColor = SurfaceWhite
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
        },
        actions = actions
    )
}

@Composable
fun BadgeStatusLarge(status: String) {
    val (textColor, bgColor) = when (status) {
        "DISETUJUI" -> SuccessGreen to SuccessGreen.copy(alpha = 0.1f)
        "DITOLAK" -> AlertRed to AlertRed.copy(alpha = 0.1f)
        "PERLU_REVISI" -> WarningYellow to WarningYellow.copy(alpha = 0.1f)
        else -> AccentPurple to AccentPurple.copy(alpha = 0.1f)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun StatSimpleBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MainBlue)
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}

@Composable
fun StatBarRow(label: String, value: Int, maxVal: Int) {
    Column {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(value.toString(), fontWeight = FontWeight.Bold, color = MainBlue)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val percentage = if(maxVal > 0) (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) else 0f

        Box(modifier = Modifier.fillMaxWidth().height(12.dp).background(BorderGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))) {
            Box(modifier = Modifier.fillMaxWidth(percentage).fillMaxHeight().background(MainBlue, RoundedCornerShape(6.dp)))
        }
    }
}
@Composable
fun DropdownInput(value: String, options: List<String>, onSelectionChanged: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChanged(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AdminMenuCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, MainBlue),
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MainBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MainBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextBlack, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 12.sp, color = TextGray)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextBlack)
        Divider(color = BorderGray.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun InfoIzinCard(izin: com.example.silpa.model.InfoJenisIzinDto, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MainBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MainBlue, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(izin.namaTampilan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextBlack)
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = BorderGray, modifier = Modifier.size(24.dp))
        }
    }
}


@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = TextGray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextBlack)
    }
}

@Composable
fun BadgeStatus(status: String) {
    val (color, bgColor) = when(status) {
        "DISETUJUI" -> SuccessGreen to SuccessGreen.copy(alpha = 0.1f)
        "DITOLAK" -> AlertRed to AlertRed.copy(alpha = 0.1f)
        "PERLU_REVISI" -> WarningYellow to WarningYellow.copy(alpha = 0.1f)
        else -> MainBlue to MainBlue.copy(alpha = 0.1f)
    }

    Surface(color = bgColor, shape = RoundedCornerShape(50)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal=10.dp, vertical=4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatItemDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MainBlue)
        Text(label, fontSize = 12.sp, color = TextGray)
    }
}


@Composable
fun StatCard(title: String, value: String, bgColor: Color, accentColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = title, fontSize = 11.sp, color = TextGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun MenuCard(title: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Sedikit elevasi agar timbul
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon dengan background lingkaran halus
            Surface(
                shape = CircleShape,
                color = MainBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MainBlue, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextBlack)
        }
    }
}

@Composable
fun HistoryItem(izin: PerizinanDto, onClick: () -> Unit) {
    // Warna berdasarkan STATUS - semua dipanggil dari Color.kt
    val (cardBgColor, accentColor, statusText) = when (izin.status.uppercase()) {
        "DISETUJUI", "APPROVED" -> Triple(
            SuccessGreenTint,
            SuccessGreen,
            "Disetujui"
        )
        "DITOLAK", "REJECTED" -> Triple(
            AlertRedTint,
            AlertRed,
            "Ditolak"
        )
        "REVISI", "REVISION", "PERLU_REVISI" -> Triple(
            WarningYellowTint,
            WarningYellow,
            "Perlu Revisi"
        )
        "MENUNGGU", "PENDING" -> Triple(
            WarningYellowTint,
            WarningYellow,
            "Menunggu"
        )
        else -> Triple(
            BackgroundLight,
            TextGray,
            izin.status
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            accentColor.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row {


            Column(modifier = Modifier.padding(16.dp)) {
                // Header: Nama & Status Badge
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = izin.mahasiswaNama ?: "Mahasiswa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextBlack
                    )
                    BadgeStatus(izin.status)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tanggal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(accentColor.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Mulai: ${izin.tanggalMulai}",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                // Catatan Admin
                if (!izin.catatanAdmin.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SurfaceWhite.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            accentColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Catatan: " + izin.catatanAdmin,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = TextBlack,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}