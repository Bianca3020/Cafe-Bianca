package com.biancaputri.pos.kategori

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biancaputri.pos.R
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ModKategoriActivity : AppCompatActivity() {

    // Firebase with your specific URL
    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val myRef = database.getReference("kategori")

    private lateinit var btnBack: ImageView
    private lateinit var etNamaKategori: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var tvSapa: TextView // Added this for your greeting!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)

        initView()
        setupEdgeToEdge()
        setupSpinner()
        setupButtonActions()

        // 1. Call the dynamic greeting (Make sure tvSapa is in your XML!)
        updateGreeting("Bianca")
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNamaKategori = findViewById(R.id.etNamaKategori)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSimpan = findViewById(R.id.btnSimpan)
        // tvSapa = findViewById(R.id.tvSapa) // Uncomment this if you added it to XML
    }

    // 2. Moved inside the class so it can access 'getString'
    private fun updateGreeting(userName: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greetingRes = when (hour) {
            in 0..10 -> R.string.sapa_pagi
            in 11..14 -> R.string.sapa_siang
            in 15..18 -> R.string.sapa_sore
            else -> R.string.sapa_malam
        }

        // Checking if tvSapa is initialized to avoid crashes
        if (::tvSapa.isInitialized) {
            tvSapa.text = getString(greetingRes, userName)
        }
    }

    private fun setupEdgeToEdge() {
        // Chi's Note: Make sure R.id.main is a ScrollView or change this to match your XML!
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
            val namaKategori = etNamaKategori.text.toString().trim()
            val statusKategori = spinnerStatus.selectedItem.toString()

            if (namaKategori.isEmpty()) {
                etNamaKategori.error = "Nama kategori tidak boleh kosong"
                etNamaKategori.requestFocus()
                return@setOnClickListener
            }

            // 3. Actually save to Firebase! 🚀
            val id = myRef.push().key ?: return@setOnClickListener
            val data = mapOf(
                "id" to id,
                "nama" to namaKategori,
                "status" to statusKategori
            )

            myRef.child(id).setValue(data).addOnSuccessListener {
                Toast.makeText(this, "Berhasil simpan ke Firebase!", Toast.LENGTH_SHORT).show()
                finish() // Go back after saving
            }.addOnFailureListener {
                Toast.makeText(this, "Gagal simpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
