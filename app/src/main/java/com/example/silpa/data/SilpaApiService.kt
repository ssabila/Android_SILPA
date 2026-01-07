package com.example.silpa.data

import com.example.silpa.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface SilpaApiService {

    // Auth & User
    @POST("auth/login")
    suspend fun login(@Body request: LoginDto): ApiResponse<JwtAuthResponseDto>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterDto): ApiResponse<String>

    @GET("pengguna/saya")
    suspend fun getProfil(): ApiResponse<ProfilPenggunaDto>

    @PUT("pengguna/saya")
    suspend fun updateProfil(@Body request: PerbaruiProfilDto): ApiResponse<ProfilPenggunaDto>

    @POST("pengguna/ganti-sandi")
    suspend fun gantiKataSandi(@Body request: GantiKataSandiDto): ApiResponse<String>

    // Dashboard
    @GET("mahasiswa/dashboard")
    suspend fun getDashboard(): MahasiswaDashboardDto

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): AdminDashboardDto

    // ADMIN MAHASISWA
    @GET("admin/mahasiswa")
    suspend fun getAllMahasiswa(): List<ProfilPenggunaDto>

    @GET("admin/mahasiswa/{id}")
    suspend fun getMahasiswaDetail(@Path("id") mahasiswaId: Long): MahasiswaDetailAdminDto

    @GET("admin/mahasiswa/{id}/perizinan")
    suspend fun getPerizinanMahasiswaById(@Path("id") mahasiswaId: Long): List<PerizinanDto>


    // Perizinan
    @GET("perizinan/saya")
    suspend fun getRiwayatIzin(): List<PerizinanDto> // Backend return List langsung

    @GET("perizinan")
    suspend fun getSemuaPerizinan(): List<PerizinanDto> // Backend return List langsung

    @Multipart
    @POST("perizinan")
    suspend fun ajukanIzin(
        @Part("izin") izin: RequestBody,
        @Part berkas: List<MultipartBody.Part>
    ): PerizinanDto

    // Get Detail by ID
    @GET("perizinan/{id}")
    suspend fun getPerizinanById(@Path("id") id: Long): PerizinanDto

    // Revisi Izin
    @Multipart
    @PUT("perizinan/{id}/revisi")
    suspend fun revisiIzin(
        @Path("id") id: Long,
        @Part("izin") izin: RequestBody,
        @Part berkas: List<MultipartBody.Part>
    ): PerizinanDto

    // VALIDASI ADMIN
    @PUT("perizinan/{id}/status")
    suspend fun validasiIzin(
        @Path("id") id: Long,
        @Body request: UpdateStatusDto
    ): PerizinanDto

    @GET("perizinan/filter")
    suspend fun filterPerizinan(
        @Query("status") status: String? = null,
        @Query("jenisIzin") jenisIzin: String? = null,
        @Query("namaMahasiswa") namaMahasiswa: String? = null,
        @Query("bulan") bulan: Int? = null,
        @Query("tahun") tahun: Int? = null
    ): List<PerizinanDto>

    // Info
    @GET("info-perizinan")
    suspend fun getInfoJenisIzin(): ApiResponse<List<InfoJenisIzinDto>>

    @GET("notifikasi/saya")
    suspend fun getNotifikasi(): ApiResponse<List<NotifikasiDto>>

    // STATISTIK
    @GET("statistik/per-jenis-izin")
    suspend fun getStatistikPerJenis(): ApiResponse<List<StatistikPerJenisDto>>

    @GET("statistik/per-bulan")
    suspend fun getStatistikBulanan(): ApiResponse<List<StatistikPerBulanDto>>

    @GET("statistik/trend")
    suspend fun getStatistikTrend(): ApiResponse<StatistikTrendDto>

}