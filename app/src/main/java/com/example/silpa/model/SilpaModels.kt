package com.example.silpa.model

//  Wrapper Response
data class ApiResponse<T>(
    val berhasil: Boolean,
    val pesan: String,
    val data: T?
)

//  Auth & User
data class LoginDto(val email: String, val kataSandi: String)
data class RegisterDto(val namaLengkap: String, val email: String, val kataSandi: String, val peran: String = "MAHASISWA")
data class JwtAuthResponseDto(val accessToken: String, val tokenType: String)
data class ProfilPenggunaDto(val id: Long, val namaLengkap: String, val email: String, val peran: String)
data class PerbaruiProfilDto(val namaLengkap: String, val email: String)
data class GantiKataSandiDto(val sandiLama: String, val sandiBaru: String)

//  Perizinan & Berkas
data class BerkasDto(
    val id: Long?,
    val namaFile: String,
    val urlAksesFile: String
)

data class DetailSesiIzinDto(
    val tanggal: String,
    val namaMataKuliah: String,
    val namaDosen: String,
    val sesi1: Boolean = true,
    val sesi2: Boolean = true,
    val sesi3: Boolean = true
)

data class PerizinanDto(
    val id: Long,
    val mahasiswaId: Long?,
    val mahasiswaNama: String?,
    val jenisIzin: String,
    val detailIzin: String,
    val tanggalMulai: String,
    val tanggalSelesai: String?,
    val deskripsi: String,
    val bobotKehadiran: Int,
    val status: String,
    val catatanAdmin: String?,
    val daftarBerkas: List<BerkasDto>?,
    val daftarSesi: List<DetailSesiIzinDto>?
)

data class AjukanIzinDto(
    val jenisIzin: String,
    val detailIzin: String,
    val deskripsi: String,
    val daftarSesi: List<DetailSesiIzinDto>
)

//  Validasi Admin
data class UpdateStatusDto(
    val status: String,
    val catatanAdmin: String
)

//  Info Publik (Landing Page)
data class InfoDetailIzinDto(
    val namaEnum: String,
    val namaTampilan: String,
    val deskripsi: String,
    val syarat: String
)

data class InfoJenisIzinDto(
    val namaEnum: String,
    val namaTampilan: String,
    val daftarDetail: List<InfoDetailIzinDto>?
)

//  Statistik
data class StatistikPerJenisDto(val jenisIzin: String, val namaJenisIzin: String?, val jumlahPengajuan: Long)
data class StatistikPerBulanDto(val tahun: Int, val bulan: Int, val namaBulanTahun: String, val jumlahPengajuan: Long)
data class StatistikTrendDto(val jumlahBulanIni: Long, val jumlahBulanLalu: Long, val persentasePerubahan: Double, val deskripsiPerubahan: String)

//  Notifikasi
data class NotifikasiDto(
    val id: Long?,
    val perizinanId: Long?,
    val pesan: String,
    val waktu: String,
    val sudahDibaca: Boolean
)

//  DASHBOARD ADMIN
data class AdminDashboardDto(
    val totalPengajuanSemuaWaktu: Long,
    val jumlahPengajuanPerStatus: Map<String, Long>?,
    val jumlahPengajuanPerJenisIzin: Map<String, Long>?,
    val pengajuanHariIni: Long,
    val pengajuanMingguIni: Long,
    val pengajuanBulanIni: Long,
    val pengajuanPerluDiproses: List<PerizinanDto>?
)

//  DATA MAHASISWA (Admin)
data class MahasiswaDetailAdminDto(
    val profil: ProfilPenggunaDto,
    val totalIzinDiajukan: Long,
    val breakdownPerStatus: Map<String, Long>?,
    val breakdownPerJenisIzin: Map<String, Long>?,
    val totalBobotTerpakai: Int,
    val daftarSemuaPerizinan: List<PerizinanDto>?
)

// DASHBOARD MAHASISWA
data class MahasiswaDashboardDto(
    val totalIzinDiajukan: Long,
    val breakdownPerStatus: Map<String, Long>?,
    val breakdownPerJenisIzin: Map<String, Long>?,
    val totalBobotTerpakai: Int,
    val izinSedangDiproses: List<PerizinanDto>?,
    val riwayat5IzinTerakhir: List<PerizinanDto>?,
    val adaPerluRevisi: Boolean
)