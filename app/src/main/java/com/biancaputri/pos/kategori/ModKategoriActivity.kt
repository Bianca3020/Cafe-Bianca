package com.biancaputri.pos.kategori

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.biancaputri.pos.activity.LoginActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var idAkun: String

    private lateinit var btnBack: ImageView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatusKategori: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var tvTitleHeader: TextView

    private var isEditMode = false
    private var existingKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        // Ambil ID Akun untuk scoping data
        val prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        myRef = database.getReference("kategori").child(idAkun)

        initView()
        setupSpinner()

        @Suppress("DEPRECATION")
        existingKategori = intent.getParcelableExtra("EXTRA_KATEGORI")
        if (existingKategori != null) {
            isEditMode = true
            tvTitleHeader.text = getString(R.string.edit_kategori)
            etNamaKategori.setText(existingKategori?.namaKategori)
            btnHapus.visibility = View.VISIBLE

            val status = existingKategori?.statusKategori
            if (status == getString(R.string.status_non_aktif)) {
                spinnerStatusKategori.setSelection(1)
            } else {
                spinnerStatusKategori.setSelection(0)
            }
        } else {
            isEditMode = false
            tvTitleHeader.text = getString(R.string.tambah_kategori)
            btnHapus.visibility = View.GONE
        }

        setupButtonActions()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatusKategori = findViewById(R.id.spinnerStatusKategori)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinnerStatus,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatusKategori.adapter = adapter
    }

    private fun setupButtonActions() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val namaKategori = etNamaKategori.text.toString().trim()
            val statusKategori = spinnerStatusKategori.selectedItem.toString()

            if (namaKategori.isEmpty()) {
                etNamaKategori.error = getString(R.string.error_nama_kategori)
                etNamaKategori.requestFocus()
                return@setOnClickListener
            }

            val id = if (isEditMode) {
                existingKategori?.idKategori ?: return@setOnClickListener
            } else {
                myRef.push().key ?: return@setOnClickListener
            }

            val data = mapOf(
                "idKategori" to id,
                "namaKategori" to namaKategori,
                "statusKategori" to statusKategori
            )

            myRef.child(id).setValue(data).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.berhasil_simpan), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan), Toast.LENGTH_SHORT).show()
            }
        }

        btnHapus.setOnClickListener {
            val id = existingKategori?.idKategori ?: return@setOnClickListener
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.hapus_kategori))
                .setMessage(getString(R.string.konfirmasi_hapus_kategori))
                .setPositiveButton(getString(R.string.ya)) { _, _ ->
                    myRef.child(id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.berhasil_hapus), Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.gagal_hapus), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(getString(R.string.tidak), null)
                .show()
        }
    }
}