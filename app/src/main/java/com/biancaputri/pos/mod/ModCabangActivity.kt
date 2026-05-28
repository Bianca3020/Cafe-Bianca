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
import com.biancaputri.pos.model.ModelCabang
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ModCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var idAkun: String

    private lateinit var btnBack: ImageView
    private lateinit var etNamaCabang: EditText
    private lateinit var etAlamatCabang: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var tvTitleHeader: TextView

    private var isEditMode = false
    private var existingCabang: ModelCabang? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }
        myRef = database.getReference("cabang").child(idAkun)

        initView()
        setupEdgeToEdge()
        setupSpinner()

        @Suppress("DEPRECATION")
        existingCabang = intent.getParcelableExtra("EXTRA_CABANG")
        if (existingCabang != null) {
            isEditMode = true
            tvTitleHeader.text = getString(R.string.edit_cabang)
            etNamaCabang.setText(existingCabang?.namaCabang)
            etAlamatCabang.setText(existingCabang?.alamatCabang)
            btnHapus.visibility = View.VISIBLE

            val status = existingCabang?.statusCabang
            if (status == "Non-Aktif") {
                spinnerStatus.setSelection(1)
            } else {
                spinnerStatus.setSelection(0)
            }
        } else {
            isEditMode = false
            tvTitleHeader.text = getString(R.string.tambah_cabang)
            btnHapus.visibility = View.GONE
        }

        setupButtonActions()

    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNamaCabang = findViewById(R.id.etNamaCabang)
        etAlamatCabang = findViewById(R.id.etAlamatCabang)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
    }

    private fun setupEdgeToEdge() {
        val mainLayout = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.spinnerStatus,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
    }

    private fun setupButtonActions() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNamaCabang.text.toString().trim()
            val alamat = etAlamatCabang.text.toString().trim()
            val status = spinnerStatus.selectedItem.toString()

            if (nama.isEmpty()) {
                etNamaCabang.error = getString(R.string.error_nama_cabang)
                etNamaCabang.requestFocus()
                return@setOnClickListener
            }

            if (alamat.isEmpty()) {
                etAlamatCabang.error = getString(R.string.error_alamat_cabang)
                etAlamatCabang.requestFocus()
                return@setOnClickListener
            }

            val id = if (isEditMode) {
                existingCabang?.idCabang ?: return@setOnClickListener
            } else {
                myRef.push().key ?: return@setOnClickListener
            }

            val data = ModelCabang(
                idCabang = id,
                namaCabang = nama,
                alamatCabang = alamat,
                statusCabang = status
            )

            myRef.child(id).setValue(data).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.berhasil_simpan_cabang), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_cabang), Toast.LENGTH_SHORT).show()
            }
        }

        btnHapus.setOnClickListener {
            val id = existingCabang?.idCabang ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hapus_cabang))
                .setMessage(getString(R.string.konfirmasi_hapus_cabang))
                .setPositiveButton(getString(R.string.ya)) { _, _ ->
                    myRef.child(id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.berhasil_hapus_cabang), Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.gagal_hapus_cabang), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(getString(R.string.tidak), null)
                .show()
        }
    }
}