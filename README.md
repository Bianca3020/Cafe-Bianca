# 🛒 Bianca POS (Point of Sale) Enterprise

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84.svg?style=for-the-badge&logo=android-studio&logoColor=white)
![Material Design 3](https://img.shields.io/badge/Material--Design--3-746AAE?style=for-the-badge&logo=material-design&logoColor=white)

**Bianca POS** adalah solusi manajemen *Point of Sale* modern yang dirancang khusus untuk efisiensi bisnis UMKM hingga skala Enterprise. Aplikasi ini mengombinasikan kekuatan **Firebase Realtime Database** untuk sinkronisasi data instan dengan antarmuka **Material 3** yang intuitif.

---

## 📸 Tampilan Aplikasi (Screenshots)

### 🔐 Autentikasi & Dashboard Utama
|                     Login                     |                     Register                     |                     Dashboard                     |
|:---------------------------------------------:|:------------------------------------------------:|:-------------------------------------------------:|
| <img src="screenshots/login.jpg" width="220"> | <img src="screenshots/register.jpg" width="220"> | <img src="screenshots/dashboard.jpg" width="220"> |
|              Masuk ke Dashboard               |                   Daftar Akun                    |             Monitoring Ringkasan Data             |

### 📦 Manajemen Produk & Stok
|                 Daftar Produk                  |                 Detail/Edit Produk                 |                Tambah Produk Baru                 |
|:----------------------------------------------:|:--------------------------------------------------:|:-------------------------------------------------:|
| <img src="Screenshots/produk.jpg" width="220"> | <img src="Screenshots/editproduk.jpg" width="220"> | <img src="screenshots/modproduk.jpg" width="220"> |
|              Lihat Katalog Produk              |            **Bisa Edit & Update** Data             |           **Bisa Tambah** Produk & Stok           |

### 🏷️ Manajemen Kategori
|                 Daftar Kategori                  |                 Detail/Edit Kategori                 |                   Tambah Kategori                   |
|:------------------------------------------------:|:----------------------------------------------------:|:---------------------------------------------------:|
| <img src="screenshots/kategori.jpg" width="220"> | <img src="screenshots/EDITkategori.jpg" width="220"> | <img src="screenshots/modkategori.jpg" width="220"> |
|            Lihat Klasifikasi Kategori            |                **Bisa Edit** Kategori                |            **Bisa Tambah** Kategori Baru            |

### 🛒 Transaksi & Nota Digital
|                  Alur Transaksi                   |                                             Keranjang Belanja                                              |   |              Cetak Nota Digital              |
|:-------------------------------------------------:|:----------------------------------------------------------------------------------------------------------:|---|:--------------------------------------------:|
| <img src="screenshots/transaksi.jpg" width="220"> | <img src="screenshots/paymentcash.jpg" width="220"> <img src="screenshots/paymentnoncash.jpg" width="220"> |   | <img src="screenshots/nota.jpg" width="220"> |
|              Pilih Kasir & Pelanggan              |                                           Update Qty & Checkout                                            |   |        Simpan & **Cetak Ke Printer**         |

### 👥 Pegawai & Pelanggan
|                                                                      Data Pegawai                                                                      |                                                                      Data Pelanggan                                                                      |
|:------------------------------------------------------------------------------------------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------:|
| <img src="screenshots/pegawai.jpg" width="220"> <img src="screenshots/modpegawai.jpg" width="220"> <img src="screenshots/editpegawai.jpg" width="220"> | <img src="screenshots/pelanggan.jpg" width="220"> <img src="screenshots/modpegawai.jpg" width="220"> <img src="screenshots/editpegawai.jpg" width="220"> |
|                                                              **Tambah & Edit** Staf Kasir                                                              |                                                               **Tambah & Edit** Pelanggan                                                                |

### 🏢 Cabang & Laporan
|                                                                  Manajemen Cabang                                                                   |                Laporan Penjualan                |
|:---------------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------:|
| <img src="screenshots/cabang.jpg" width="220"> <img src="screenshots/modcabang.jpg" width="220"> <img src="screenshots/editcabang.jpg" width="220"> | <img src="screenshots/laporan.jpg" width="220"> |
|                                                           **Tambah & Edit** Lokasi Outlet                                                           |              Rekap Omset & Histori              |

### ⚙️ Pengaturan & Hardware
|               Konfigurasi Printer               |                   Ganti Bahasa                   |                    Dark Mode                     |
|:-----------------------------------------------:|:------------------------------------------------:|:------------------------------------------------:|
| <img src="screenshots/printer.jpg" width="220"> | <img src="screenshots/language.jpg" width="220"> | <img src="screenshots/darkmode.jpg" width="220"> |
|            Scan & Connect Bluetooth             |                Mendukung ID / EN                 |           Nyaman di Mata (Dark Theme)            |

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