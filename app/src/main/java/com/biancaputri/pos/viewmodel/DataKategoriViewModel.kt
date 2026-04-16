package com.biancaputri.pos.kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    // Views
    private lateinit var tvJudul: TextView
    private lateinit var etNamaKategori: TextInputEditText
    private lateinit var spStatusKategori: Spinner
    private lateinit var btnSimpan: MaterialButton
    private lateinit var btnBack: ImageView

    // Firebase - Make sure your google-services.json is in the app folder!
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    // Data variables for Edit Mode
    private var kategoriId: String? = null
    private var kategoriNama: String? = null
    private var kategoriStatus: String? = null

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAMA = "extra_nama"
        const val EXTRA_STATUS = "extra_status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        getIntentData()
        setupSpinner()
        setupClickListeners()
        updateTitle()

    }

    private fun init() {
        tvJudul = findViewById(R.id.tvJudul)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spStatusKategori = findViewById(R.id.spinnerStatusKategori)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun getIntentData() {
        kategoriId = intent.getStringExtra(EXTRA_ID)
        kategoriNama = intent.getStringExtra(EXTRA_NAMA)
        kategoriStatus = intent.getStringExtra(EXTRA_STATUS)

        if (kategoriId != null) {
            etNamaKategori.setText(kategoriNama)
        }
    }

    private fun updateTitle() {
        // Chi's Note: Fixed the title logic for you! ✨
        tvJudul.text = if (kategoriId == null) "Tambah Kategori" else "Edit Kategori"
    }

    private fun setupSpinner() {
        val listStatus = arrayOf("Aktif", "Tidak Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listStatus)
        spStatusKategori.adapter = adapter

        // If editing, set the spinner to the saved status
        if (kategoriStatus != null) {
            val selection = if (kategoriStatus == "1") 0 else 1
            spStatusKategori.setSelection(selection)
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }
        btnSimpan.setOnClickListener { simpanData() }
    }

    private fun simpanData() {
        val nama = etNamaKategori.text.toString().trim()
        val status = if (spStatusKategori.selectedItem.toString() == "Aktif") "1" else "0"

        if (nama.isEmpty()) {
            etNamaKategori.error = "Nama kategori wajib diisi!"
            return
        }

        if (kategoriId == null) {
            // MODE: TAMBAH (NEW DATA)
            val newKey = myRef.push().key ?: return
            val data = ModelKategori(newKey, nama, status)

            myRef.child(newKey).setValue(data).addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil ditambah!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Waduh, gagal simpan!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // MODE: EDIT (UPDATE DATA)
            val data = ModelKategori(kategoriId!!, nama, status)
            myRef.child(kategoriId!!).setValue(data).addOnSuccessListener {
                Toast.makeText(this, "Update berhasil!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
