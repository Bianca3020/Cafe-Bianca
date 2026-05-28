package com.biancaputri.pos.mod

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.activity.LoginActivity
import com.biancaputri.pos.model.ModelProduk
import com.google.firebase.database.*

class ModProdukActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var kategoriRef: DatabaseReference
    private lateinit var cabangRef: DatabaseReference
    private lateinit var idAkun: String

    private lateinit var btnBack: ImageView
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var spKategori: Spinner
    private lateinit var spCabang: Spinner
    private lateinit var etStok: EditText
    private lateinit var cbUnlimited: CheckBox
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var tvTitleHeader: TextView

    private var isEditMode = false
    private var produkId: String? = null
    private var existingProduk: ModelProduk? = null

    private val categories = mutableListOf<String>()
    private val branches = mutableListOf<String>()

    private lateinit var kategoriAdapter: ArrayAdapter<String>
    private lateinit var cabangAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_produk)

        // Ambil ID Akun untuk scoping data
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
        
        myRef = database.getReference("produk").child(idAkun)
        kategoriRef = database.getReference("kategori").child(idAkun)
        cabangRef = database.getReference("cabang").child(idAkun)

        initView()
        setupSpinners()
        loadSpinnerData()

        @Suppress("DEPRECATION")
        existingProduk = intent.getParcelableExtra("EXTRA_PRODUK")
        produkId = existingProduk?.idProduk

        if (existingProduk != null) {
            isEditMode = true
            tvTitleHeader.text = getString(R.string.edit_produk)
            btnHapus.visibility = View.VISIBLE
            loadProductDetails()
        } else {
            isEditMode = false
            tvTitleHeader.text = getString(R.string.tambah_produk)
            btnHapus.visibility = View.GONE
        }

        setupActions()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNama = findViewById(R.id.etNama)
        etHarga = findViewById(R.id.etHarga)
        spKategori = findViewById(R.id.spKategori)
        spCabang = findViewById(R.id.spCabang)
        etStok = findViewById(R.id.etStok)
        cbUnlimited = findViewById(R.id.cbUnlimited)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
    }

    private fun setupSpinners() {
        kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKategori.adapter = kategoriAdapter

        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, branches)
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCabang.adapter = cabangAdapter

        cbUnlimited.setOnCheckedChangeListener { _, isChecked ->
            etStok.isEnabled = !isChecked
            if (isChecked) etStok.setText("")
        }
    }

    private fun loadSpinnerData() {
        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categories.clear()
                for (data in snapshot.children) {
                    val name = data.child("namaKategori").getValue(String::class.java)
                    if (name != null) categories.add(name)
                }
                if (categories.isEmpty()) categories.add(getString(R.string.umum))
                kategoriAdapter.notifyDataSetChanged()
                existingProduk?.let { selectSpinnerItem(spKategori, it.idKategori) }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    branches.clear()
                    for (data in snapshot.children) {
                        val name = data.child("namaCabang").getValue(String::class.java)
                        if (name != null) branches.add(name)
                    }
                    if (branches.isEmpty()) branches.add(getString(R.string.utama))
                    cabangAdapter.notifyDataSetChanged()
                    existingProduk?.let { selectSpinnerItem(spCabang, it.idCabang) }
                } else {
                    database.getReference("cabang").addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(rootSnapshot: DataSnapshot) {
                            branches.clear()
                            for (data in rootSnapshot.children) {
                                val hasNama = data.child("namaCabang").exists()
                                if (hasNama) {
                                    val name = data.child("namaCabang").getValue(String::class.java)
                                    if (name != null) branches.add(name)
                                }
                            }
                            if (branches.isEmpty()) branches.add(getString(R.string.utama))
                            cabangAdapter.notifyDataSetChanged()
                            existingProduk?.let { selectSpinnerItem(spCabang, it.idCabang) }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun selectSpinnerItem(spinner: Spinner, value: String?) {
        if (value == null) return
        val adapter = spinner.adapter as? ArrayAdapter<String> ?: return
        val position = adapter.getPosition(value)
        if (position >= 0) spinner.setSelection(position)
    }

    private fun loadProductDetails() {
        val produk = existingProduk ?: return
        etNama.setText(produk.namaProduk)
        etHarga.setText((produk.hargaProduk ?: 0).toString())

        if (produk.tanpaBatas == "ya") {
            cbUnlimited.isChecked = true
            etStok.setText("")
            etStok.isEnabled = false
        } else {
            cbUnlimited.isChecked = false
            etStok.setText((produk.stokProduk ?: 0).toString())
            etStok.isEnabled = true
        }
    }

    private fun setupActions() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val hargaStr = etHarga.text.toString().trim()
            val kategori = spKategori.selectedItem?.toString() ?: getString(R.string.umum)
            val cabang = spCabang.selectedItem?.toString() ?: getString(R.string.utama)
            val unlimited = cbUnlimited.isChecked
            val stokStr = etStok.text.toString().trim()

            if (nama.isEmpty()) {
                etNama.error = getString(R.string.error_nama_produk)
                etNama.requestFocus()
                return@setOnClickListener
            }

            if (hargaStr.isEmpty()) {
                etHarga.error = getString(R.string.error_harga_produk)
                etHarga.requestFocus()
                return@setOnClickListener
            }

            val harga = hargaStr.toIntOrNull() ?: 0
            val stok = if (unlimited) 0 else (stokStr.toIntOrNull() ?: 0)
            val tanpaBatasVal = if (unlimited) "ya" else "tidak"

            val id = if (isEditMode) {
                produkId ?: return@setOnClickListener
            } else {
                myRef.push().key ?: return@setOnClickListener
            }

            val productData = ModelProduk(
                idProduk = id,
                namaProduk = nama,
                hargaProduk = harga,
                idKategori = kategori,
                idCabang = cabang,
                fotoProduk = existingProduk?.fotoProduk ?: "",
                stokProduk = stok,
                tanpaBatas = tanpaBatasVal,
                statusProduk = existingProduk?.statusProduk ?: "Aktif",
                createdAt = existingProduk?.createdAt ?: System.currentTimeMillis().toString(),
                updatedAt = System.currentTimeMillis().toString()
            )

            myRef.child(id).setValue(productData).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.berhasil_simpan_produk), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_produk), Toast.LENGTH_SHORT).show()
            }
        }

        btnHapus.setOnClickListener {
            val id = produkId ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hapus_produk))
                .setMessage(getString(R.string.konfirmasi_hapus_produk))
                .setPositiveButton(getString(R.string.ya)) { _, _ ->
                    myRef.child(id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.berhasil_hapus_produk), Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.gagal_hapus_produk), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(getString(R.string.tidak), null)
                .show()
        }
    }
}
