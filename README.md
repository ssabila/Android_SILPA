# SILPA - Sistem Informasi Layanan Perizinan Akademik

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)

Aplikasi mobile Android untuk manajemen perizinan akademik mahasiswa yang terintegrasi dengan backend Spring Boot.

## 📱 Tentang Aplikasi

SILPA adalah aplikasi manajemen perizinan akademik yang memudahkan mahasiswa untuk mengajukan berbagai jenis izin (sakit, acara keluarga, dll) dan memungkinkan admin/dosen untuk memvalidasi pengajuan tersebut secara digital. Aplikasi ini dibangun dengan teknologi modern menggunakan Jetpack Compose untuk UI yang responsif dan intuitif.

## ✨ Fitur Utama

### 👨‍🎓 Fitur Mahasiswa
- **Autentikasi**
  - Login dengan email dan password
  - Registrasi akun baru
  - Logout
  
- **Dashboard**
  - Ringkasan izin (disetujui, ditolak, pending)
  - Statistik kehadiran
  - Notifikasi status izin

- **Manajemen Perizinan**
  - Ajukan izin baru (Sakit, Acara Keluarga, Keperluan Pribadi, dll)
  - Pilih jenis detail izin (Rawat Jalan, Rawat Inap, dll)
  - Upload lampiran dokumen pendukung (foto, PDF)
  - Lihat riwayat izin lengkap
  - Revisi izin yang perlu diperbaiki
  - Detail izin dengan status real-time

- **Profil**
  - Lihat dan edit profil
  - Ganti kata sandi (dengan konfirmasi)
  - Informasi akun

### 👨‍💼 Fitur Admin/Dosen
- **Dashboard Admin**
  - Statistik perizinan keseluruhan
  - Izin pending yang perlu divalidasi
  - Grafik dan analitik

- **Validasi Perizinan**
  - Lihat detail pengajuan izin
  - Setujui atau tolak izin
  - Minta revisi dengan catatan
  - Tambahkan catatan admin

- **Manajemen Mahasiswa**
  - Lihat daftar seluruh mahasiswa
  - Detail profil mahasiswa
  - Riwayat izin per mahasiswa

- **Statistik & Laporan**
  - Statistik per jenis izin
  - Statistik per bulan
  - Trend perizinan
  - Filter berdasarkan status, jenis, nama mahasiswa, bulan, dan tahun

## 🏗️ Struktur Proyek

```
SILPA/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/silpa/
│   │   │   │   ├── data/              # Layer Data & Network
│   │   │   │   │   ├── RetrofitInstance.kt    # Konfigurasi Retrofit
│   │   │   │   │   ├── SessionManager.kt      # Manajemen token & session
│   │   │   │   │   └── SilpaApiService.kt     # API endpoints
│   │   │   │   │
│   │   │   │   ├── model/             # Data Models
│   │   │   │   │   └── SilpaModels.kt         # DTOs dan models
│   │   │   │   │
│   │   │   │   ├── ui/                # UI Layer
│   │   │   │   │   ├── components/    # Komponen reusable
│   │   │   │   │   │   └── components.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screens/       # Layar aplikasi
│   │   │   │   │   │   ├── AdminDashboardScreen.kt
│   │   │   │   │   │   ├── AdminMahasiswaScreen.kt
│   │   │   │   │   │   ├── AdminStatistikScreen.kt
│   │   │   │   │   │   ├── AdminValidasiScreen.kt
│   │   │   │   │   │   ├── DetailIzinScreen.kt
│   │   │   │   │   │   ├── LandingScreen.kt
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── MahasiswaDashboardScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   ├── RegisterScreen.kt
│   │   │   │   │   │   ├── RevisiIzinScreen.kt
│   │   │   │   │   │   ├── RiwayatIzinScreen.kt
│   │   │   │   │   │   └── SubmitIzinScreen.kt
│   │   │   │   │   │
│   │   │   │   │   └── theme/         # Design System
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   │
│   │   │   │   ├── utils/             # Utilities
│   │   │   │   │   └── DateUtils.kt
│   │   │   │   │
│   │   │   │   └── MainActivity.kt    # Entry point
│   │   │   │
│   │   │   ├── res/                   # Resources
│   │   │   │   ├── drawable/          # Icons & images
│   │   │   │   ├── values/            # Strings, colors, themes
│   │   │   │   └── ...
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── androidTest/               # UI Tests
│   │
│   └── build.gradle.kts               # Dependencies
│
├── gradle/                            # Gradle configuration
├── build.gradle.kts                   # Project build config
├── settings.gradle.kts                # Project settings
└── README.md                          # Dokumentasi ini
```

## 🛠️ Tech Stack

### Frontend (Android)
- **Kotlin** - Bahasa pemrograman utama
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - Design system
- **Retrofit** - HTTP client untuk API calls
- **OkHttp** - Interceptor untuk logging dan authentication
- **Gson** - JSON serialization/deserialization
- **Coil** - Image loading library
- **Coroutines** - Asynchronous programming
- **ViewModel & LiveData** - Architecture components
- **Navigation Compose** - Navigation framework

### Backend (Spring Boot)
- **Spring Boot 3.x** - Backend framework
- **Spring Security** - Authentication & authorization
- **JWT** - Token-based authentication
- **Spring Data JPA** - Database access
- **PostgreSQL/MySQL** - Database
- **Hibernate** - ORM

## 📋 Prerequisites

Sebelum instalasi, pastikan Anda memiliki:

- **Android Studio** Hedgehog (2023.1.1) atau lebih baru
- **JDK 17** atau lebih tinggi
- **Android SDK** API Level 24+ (Android 7.0+)
- **Gradle** 8.0+
- **Device/Emulator** dengan Android 7.0 (API 24) atau lebih tinggi

## 🚀 Cara Instalasi

### 1. Clone Repository
```bash
git clone https://git.stis.ac.id/222313363/silpa_ui.git
cd silpa_ui
```

### 2. Konfigurasi Backend URL

Edit file `RetrofitInstance.kt` untuk mengatur URL backend:

```kotlin
// Path: app/src/main/java/com/example/silpa/data/RetrofitInstance.kt

private const val BASE_URL = "http://192.168.0.24:8080/api/"
```

**Catatan penting:**
- Untuk **Emulator Android**: Gunakan `http://10.0.2.2:8080/api/`
- Untuk **Device Fisik**: Gunakan IP laptop Anda (contoh: `http://192.168.x.x:8080/api/`)
- Untuk **Production**: Gunakan URL domain server (contoh: `https://api.silpa.com/api/`)

### 3. Sinkronisasi Dependencies

Buka proyek di Android Studio, kemudian:
1. Klik **File** → **Sync Project with Gradle Files**
2. Tunggu hingga proses sinkronisasi selesai
3. Pastikan tidak ada error di Gradle

### 4. Setup Backend Server

Pastikan backend Spring Boot sudah running dengan endpoint berikut tersedia:
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/pengguna/saya`
- `PUT /api/pengguna/saya/kata-sandi`
- Dan endpoint lainnya sesuai dokumentasi backend

### 5. Build & Run

#### Menggunakan Android Studio:
1. Pilih device/emulator dari dropdown
2. Klik tombol **Run** (▶️) atau tekan `Shift + F10`

#### Menggunakan Command Line:
```bash
# Debug build
./gradlew assembleDebug

# Install ke device/emulator
./gradlew installDebug

# Build APK release
./gradlew assembleRelease
```

APK yang dihasilkan akan berada di:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 🔐 Akun Default

### Admin
- Email: `admin@silpa.com`
- Password: `admin123`

### Mahasiswa (untuk testing)
- Email: `mahasiswa@silpa.com`
- Password: `mahasiswa123`

*Sesuaikan dengan akun yang ada di backend Anda*

## 🎨 Design System

### Color Palette
- **Main Blue**: `#1E88E5` - Warna primer aplikasi
- **Surface White**: `#FFFFFF` - Background card
- **Background Light**: `#F5F7FA` - Background layar
- **Text Black**: `#1A1A1A` - Text utama
- **Text Gray**: `#666666` - Text sekunder
- **Success Green**: `#4CAF50` - Status berhasil
- **Alert Red**: `#F44336` - Status error/ditolak
- **Warning Yellow**: `#FFC107` - Status pending/revisi

### Typography
- **Display**: 28sp, Bold
- **Headline**: 24sp, Bold
- **Title**: 20sp, SemiBold
- **Body**: 16sp, Regular
- **Caption**: 12sp, Regular

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### UI Tests
```bash
./gradlew connectedAndroidTest
```

## 📦 Dependencies Utama

```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")
implementation("androidx.navigation:navigation-compose:2.7.5")

// Networking
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 🐛 Troubleshooting

### Error: "Unable to connect to backend"
- Pastikan backend server sudah running
- Periksa URL di `RetrofitInstance.kt`
- Untuk emulator, gunakan IP `10.0.2.2`
- Untuk device fisik, pastikan laptop dan HP di network yang sama

### Error: "401 Unauthorized"
- Token mungkin sudah expired
- Coba logout dan login kembali
- Periksa implementasi JWT di backend

### Error: "No Internet Connection"
- Periksa koneksi internet device/emulator
- Tambahkan permission di `AndroidManifest.xml`:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
  ```

### Build Error
```bash
# Clean dan rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

## 🤝 Kontribusi

Kontribusi sangat diterima! Silakan ikuti langkah berikut:

1. Fork repository ini
2. Buat branch fitur (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## 📄 Lisensi

Proyek ini dilisensikan di bawah MIT License - lihat file [LICENSE](LICENSE) untuk detail.

## 👥 Tim Pengembang

- **Frontend Developer** - Android (Kotlin, Jetpack Compose)
- **Backend Developer** - Spring Boot (Java)
- **UI/UX Designer** - Design System

## 📞 Kontak

- Repository: https://git.stis.ac.id/222313363/silpa_ui
- Email: support@silpa.com

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Retrofit](https://square.github.io/retrofit/)
- [Spring Boot](https://spring.io/projects/spring-boot)

---

**Dibuat dengan ❤️ untuk kemudahan manajemen perizinan akademik**

## Suggestions for a good README

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.

## Name
Choose a self-explaining name for your project.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.

## Contributing
State if you are open to contributions and what your requirements are for accepting them.

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.

## License
For open source projects, say how it is licensed.

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
