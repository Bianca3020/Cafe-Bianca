package com.biancaputri.pos.mod

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biancaputri.pos.activity.LoginActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelPelanggan
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ModPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var idAkun: String

    private lateinit var btnBack: ImageView
    private lateinit var etNama: EditText
    private lateinit var etTelp: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var tvTitleHeader: TextView
    private var isEditMode = false
    private var existingPelanggan: ModelPelanggan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pelanggan)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        myRef = database.getReference("pelanggan").child(idAkun)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()

        @Suppress("DEPRECATION")
        existingPelanggan = intent.getParcelableExtra("EXTRA_PELANGGAN")
        if (existingPelanggan != null) {
            isEditMode = true
            tvTitleHeader.text = getString(R.string.edit_pelanggan)
            etNama.setText(existingPelanggan?.namaPelanggan)
            etTelp.setText(existingPelanggan?.nomorTelp)
            etEmail.setText(existingPelanggan?.email)
            btnHapus.visibility = View.VISIBLE
        } else {
            isEditMode = false
            tvTitleHeader.text = getString(R.string.tambah_pelanggan)
            btnHapus.visibility = View.GONE
        }

        setupActions()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNama = findViewById(R.id.etNama)
        etTelp = findViewById(R.id.etTelp)
        etEmail = findViewById(R.id.etEmail)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
    }

    private fun setupActions() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val telp = etTelp.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = getString(R.string.error_nama_pelanggan)
                etNama.requestFocus()
                return@setOnClickListener
            }

            if (telp.isEmpty()) {
                etTelp.error = getString(R.string.error_telp_pelanggan)
                etTelp.requestFocus()
                return@setOnClickListener
            }

            val id = if (isEditMode) {
                existingPelanggan?.idPelanggan ?: return@setOnClickListener
            } else {
                myRef.push().key ?: return@setOnClickListener
            }

            val data = ModelPelanggan(
                idPelanggan = id,
                namaPelanggan = nama,
                nomorTelp = telp,
                email = email,
            )

            myRef.child(id).setValue(data).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.berhasil_simpan_pelanggan), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_pelanggan), Toast.LENGTH_SHORT).show()
            }
        }

        btnHapus.setOnClickListener {
            val id = existingPelanggan?.idPelanggan ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hapus_pelanggan))
                .setMessage(getString(R.string.konfirmasi_hapus_pelanggan))
                .setPositiveButton(getString(R.string.ya)) { _, _ ->
                    myRef.child(id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.berhasil_hapus_pelanggan), Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.gagal_hapus_pelanggan), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(getString(R.string.tidak), null)
                .show()
        }
    }
}
