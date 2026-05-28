# 🛒 Bianca POS (Point of Sale) Enterprise

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material--Design--3-746AAE?style=for-the-badge&logo=material-design&logoColor=white)

**Bianca POS** adalah solusi manajemen *Point of Sale* modern yang dirancang khusus untuk efisiensi bisnis UMKM hingga skala Enterprise. Aplikasi ini mengombinasikan kekuatan **Firebase Realtime Database** untuk sinkronisasi data instan dengan antarmuka **Material 3** yang intuitif.

---

## 📸 Tampilan Aplikasi (Screenshots)

|                                        Login & Register                                        |                  Dashboard Utama                  |                Manajemen Produk                |
|:----------------------------------------------------------------------------------------------:|:-------------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/login.jpg" width="100"> <img src="screenshots/register.jpg" width="100"> | <img src="screenshots/dashboard.jpg" width="200"> | <img src="screenshots/produk.jpg" width="200"> |

|                 Transaksi Kasir                  |                 Keranjang Belanja                 |                 Nota Digital                 |
|:------------------------------------------------:|:-------------------------------------------------:|:--------------------------------------------:|
| <img src="screenshots/transaki.jpg" width="200"> | <img src="screenshots/keranjang.jpg" width="200"> | <img src="screenshots/nota.jpg" width="200"> |

|                Manajemen Pelanggan                |                Manajemen Pegawai                |                Manajemen Cabang                |
|:-------------------------------------------------:|:-----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/pelanggan.jpg" width="200"> | <img src="screenshots/pegawai.jpg" width="200"> | <img src="screenshots/cabang.jpg" width="200"> |

|              Filter Cabang Modern              |                Laporan Penjualan                |               Konfigurasi Printer               |
|:----------------------------------------------:|:-----------------------------------------------:|:-----------------------------------------------:|
| <img src="screenshots/filter.jpg" width="200"> | <img src="screenshots/laporan.jpg" width="200"> | <img src="screenshots/printer.jpg" width="200"> |

|                Pengaturan Bahasa                 |                 Dark Mode Theme                  |                Akun & Profil                 |
|:------------------------------------------------:|:------------------------------------------------:|:--------------------------------------------:|
| <img src="screenshots/language.jpg" width="200"> | <img src="screenshots/darkmode.jpg" width="200"> | <img src="screenshots/akun.jpg" width="200"> |

---

## 📂 Daftar Aktivitas & Fitur Lengkap

### 🔐 Modul Otentikasi & Akun
*   **Login & Register Activity**: Sistem pendaftaran dan masuk akun aman via Firebase Auth.
*   **Akun Activity**: Manajemen profil, **Logout**, dan akses ke pengaturan profil.
*   **Mod Akun Activity**: **Edit** informasi profil & data toko serta fitur **Hapus (Delete)** akun permanen.

### 🛒 Sistem Transaksi (POS) & Hardware
*   **Transaksi Activity**: Alur kasir lengkap: Pilih **Cabang** → Pilih **Kasir** → Pilih **Pelanggan** → **Tambah Produk** ke Keranjang → **Pembayaran**.
*   **Nota Activity**: Kalkulasi otomatis dan pembuatan **Struk Nota** digital yang siap cetak.
*   **Printer Activity**: Manajemen **List Bluetooth** untuk koneksi ke Thermal Printer.

### 📦 Manajemen Data Operasional (CRUD)
Dukungan penuh fitur **Tambah, Edit, & Hapus** untuk entitas berikut:
*   **Produk**: Manajemen inventaris, harga, dan stok.
*   **Kategori**: Pengelompokan produk dengan *Horizontal Chips*.
*   **Pelanggan**: Database klien untuk histori belanja.
*   **Pegawai**: Pengaturan staf dan hak akses per cabang.
*   **Cabang**: Manajemen multi-lokasi bisnis.

### 📊 Laporan & UI
*   **Laporan Activity**: Melihat seluruh **History Transaksi** untuk audit keuangan.
*   **Settings**: Ganti Bahasa (**ID/EN**) dan Tema (**Light/Dark**) secara adaptif.

---

## 🗄️ Struktur Database (Firebase Realtime Database)

Aplikasi menggunakan struktur JSON NoSQL yang dioptimalkan untuk kecepatan akses:

```json
{
  "akun": { "uid": { "namaToko": "", "email": "", "foto": "" } },
  "produk": { "id_produk": { "nama": "", "harga": 0, "stok": 0, "id_kategori": "", "id_cabang": "" } },
  "kategori": { "id_kategori": { "nama_kategori": "" } },
  "pelanggan": { "id_pelanggan": { "nama": "", "telepon": "", "alamat": "" } },
  "pegawai": { "id_pegawai": { "nama": "", "posisi": "", "id_cabang": "" } },
  "cabang": { "id_cabang": { "nama_cabang": "", "lokasi": "" } },
  "transaksi": { "id_transaksi": { "tanggal": "", "total": 0, "item": [], "id_pelanggan": "", "id_cabang": "" } }
}
```

---

## ✨ Fitur Unggulan

- **🚀 Real-time Cloud Sync**: Data tersinkronisasi otomatis via Firebase.
- **🏬 Multi-Branch Ecosystem**: Manajemen banyak lokasi dalam satu aplikasi.
- **🌗 Material 3 UI**: Desain modern dengan dukungan Dark Mode.
- **🌐 Localization**: Dukungan penuh Bahasa Indonesia & English.
- **🖨️ Thermal Printer**: Cetak struk instan via Bluetooth.

---

## 🚀 Instalasi

1. **Clone**: `git clone https://github.com/bianca3020/cafe-bianca.git`
2. **Config**: Tambahkan `google-services.json` ke folder `app/`.
3. **Run**: Jalankan di Android Studio (disarankan Android 10+).

---
**Developed with ❤️ by [Bianca Putri](https://github.com/bianca3020)**