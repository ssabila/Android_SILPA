package com.example.silpa.utils

/**
 * Mengkonversi string dengan underscore menjadi format yang lebih readable
 * Contoh: "RAWAT_JALAN" -> "Rawat Jalan"
 *         "PERLU_REVISI" -> "Perlu Revisi"
 */
fun String.toReadableFormat(): String {
    return this.split("_")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

/**
 * Map untuk konversi jenis izin dari database ke format UI
 */
val JENIS_IZIN_MAP = mapOf(
    "SAKIT" to "Sakit",
    "DISPENSASI_INSTITUSI" to "Dispensasi Institusi",
    "IZIN_ALASAN_PENTING" to "Izin Alasan Penting"
)

/**
 * Map untuk konversi detail izin dari database ke format UI
 */
val DETAIL_IZIN_MAP = mapOf(
    "RAWAT_JALAN" to "Rawat Jalan",
    "RAWAT_INAP" to "Rawat Inap",
    "DISPENSASI" to "Dispensasi",
    "KELUARGA_INTI_MENINGGAL" to "Keluarga Inti Meninggal",
    "BENCANA" to "Bencana",
    "PASANGAN_MELAHIRKAN" to "Pasangan Melahirkan"
)

/**
 * Map untuk konversi status dari database ke format UI
 */
val STATUS_MAP = mapOf(
    "DISETUJUI" to "Disetujui",
    "DITOLAK" to "Ditolak",
    "PERLU_REVISI" to "Perlu Revisi",
    "PENDING" to "Menunggu"
)

/**
 * Get readable display text untuk jenis izin
 */
fun String.toReadableJenisIzin(): String {
    return JENIS_IZIN_MAP[this] ?: this.toReadableFormat()
}

/**
 * Get readable display text untuk detail izin
 */
fun String.toReadableDetailIzin(): String {
    return DETAIL_IZIN_MAP[this] ?: this.toReadableFormat()
}

/**
 * Get readable display text untuk status
 */
fun String.toReadableStatus(): String {
    return STATUS_MAP[this.uppercase()] ?: this.toReadableFormat()
}
