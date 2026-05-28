package com.biancaputri.pos.mod

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biancaputri.pos.R
import com.google.firebase.database.FirebaseDatabase

class ModAkunActivity : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etNomor: EditText
    private lateinit var etGithub: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnBack: ImageView

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_akun)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNama = findViewById(R.id.etNama)
        etNomor = findViewById(R.id.etNomor)
        etGithub = findViewById(R.id.etGithub)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""

        etNama.setText(prefs.getString("nama", ""))
        etNomor.setText(prefs.getString("nomor", ""))
        etGithub.setText(prefs.getString("github", ""))

        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val nomor = etNomor.text.toString().trim()
            val github = etGithub.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = getString(R.string.nama_kosong)
                return@setOnClickListener
            }

            if (idAkun.isNotEmpty()) {
                updateData(idAkun, nama, nomor, github)
            }
        }
    }

    private fun updateData(idAkun: String, nama: String, nomor: String, github: String) {
        btnSimpan.isEnabled = false
        btnSimpan.text = getString(R.string.simpan) + "..."

        val updates = mapOf(
            "nama" to nama,
            "nomor" to nomor,
            "github" to github
        )

        database.getReference("akun").child(idAkun).updateChildren(updates)
            .addOnSuccessListener {
                val prefs = getSharedPreferences("session", MODE_PRIVATE)
                prefs.edit().apply {
                    putString("nama", nama)
                    putString("nomor", nomor)
                    putString("github", github)
                    apply()
                }

                Toast.makeText(this, getString(R.string.berhasil_simpan), Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan), Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
                btnSimpan.text = getString(R.string.simpan)
            }
    }
}