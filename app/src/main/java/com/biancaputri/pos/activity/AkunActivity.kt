package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AlertDialog
import com.biancaputri.pos.R
import com.google.firebase.database.FirebaseDatabase
import com.biancaputri.pos.mod.ModAkunActivity

class AkunActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_akun)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadData()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnEditProfil).setOnClickListener {
            startActivity(Intent(this, ModAkunActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutDialog()
        }

        findViewById<Button>(R.id.btnHapusAkun).setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val dash = getString(R.string.dash)
        val nama = prefs.getString("nama", dash) ?: dash
        val email = prefs.getString("email", dash) ?: dash
        val nomor = prefs.getString("nomor", dash) ?: dash
        val github = prefs.getString("github", dash) ?: dash

        findViewById<TextView>(R.id.tvNama).text = nama
        findViewById<TextView>(R.id.tvEmail).text = email
        findViewById<TextView>(R.id.tvNomor).text = nomor
        findViewById<TextView>(R.id.tvGithub).text = github
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.pesan_logout))
            .setPositiveButton(getString(R.string.ya)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(this, getString(R.string.berhasil_logout), Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_hapus_akun))
            .setMessage(getString(R.string.konfirmasi_hapus_akun))
            .setPositiveButton(getString(R.string.ya)) { _, _ ->
                deleteAccountData()
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }

    private fun deleteAccountData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""

        if (idAkun.isNotEmpty()) {
            val nodes = listOf("akun", "produk", "transaksi", "pelanggan", "kategori", "cabang", "pegawai")
            var completed = 0
            
            for (node in nodes) {
                database.getReference(node).child(idAkun).removeValue()
                    .addOnCompleteListener {
                        completed++
                        if (completed == nodes.size) {
                            Toast.makeText(this, getString(R.string.berhasil_hapus_akun), Toast.LENGTH_SHORT).show()
                            performLogout()
                        }
                    }
            }
        } else {
            Toast.makeText(this, getString(R.string.gagal_hapus_akun), Toast.LENGTH_SHORT).show()
        }
    }
}
