package com.biancaputri.pos.mod

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.biancaputri.pos.activity.LoginActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelCabang
import com.biancaputri.pos.model.ModelPegawai
import com.google.firebase.database.*

class ModPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var cabangRef: DatabaseReference
    private lateinit var idAkun: String
    private lateinit var btnBack: ImageView
    private lateinit var etNamaPegawai: EditText
    private lateinit var etEmailPegawai: EditText
    private lateinit var etNomorTelp: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerCabang: Spinner
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var tvTitleHeader: TextView
    private var isEditMode = false
    private var existingPegawai: ModelPegawai? = null
    private val listCabang = mutableListOf<ModelCabang>()
    private lateinit var cabangAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pegawai)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        myRef = database.getReference("pegawai").child(idAkun)
        cabangRef = database.getReference("cabang").child(idAkun)

        initView()
        setupSpinners()
        loadCabangData()

        @Suppress("DEPRECATION")
        existingPegawai = intent.getParcelableExtra("EXTRA_PEGAWAI")
        if (existingPegawai != null) {
            isEditMode = true
            tvTitleHeader.text = getString(R.string.edit_pegawai)
            etNamaPegawai.setText(existingPegawai?.namaPegawai)
            etEmailPegawai.setText(existingPegawai?.emailPegawai)
            etNomorTelp.setText(existingPegawai?.nomorTelp)
            btnHapus.visibility = View.VISIBLE

            val roleAdapter = spinnerRole.adapter as ArrayAdapter<String>
            val rolePos = roleAdapter.getPosition(existingPegawai?.rolePegawai)
            if (rolePos >= 0) spinnerRole.setSelection(rolePos)

            val statusAdapter = spinnerStatus.adapter as ArrayAdapter<String>
            val statusPos = statusAdapter.getPosition(existingPegawai?.statusPegawai)
            if (statusPos >= 0) spinnerStatus.setSelection(statusPos)
        } else {
            isEditMode = false
            tvTitleHeader.text = getString(R.string.tambah_pegawai)
            btnHapus.visibility = View.GONE
        }

        setupButtonActions()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        etNamaPegawai = findViewById(R.id.etNamaPegawai)
        etEmailPegawai = findViewById(R.id.etEmailPegawai)
        etNomorTelp = findViewById(R.id.etNomorTelp)
        spinnerRole = findViewById(R.id.spinnerRole)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerCabang = findViewById(R.id.spinnerCabang)
        btnSimpan = findViewById(R.id.btnSimpan)
        btnHapus = findViewById(R.id.btnHapus)
        tvTitleHeader = findViewById(R.id.tvTitleHeader)
    }

    private fun setupSpinners() {
        val roleAdapter = ArrayAdapter.createFromResource(
            this, R.array.spinnerRole, android.R.layout.simple_spinner_item
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = roleAdapter

        val statusAdapter = ArrayAdapter.createFromResource(
            this, R.array.spinnerStatus, android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter
        cabangAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf())
        cabangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCabang.adapter = cabangAdapter
    }

    private fun loadCabangData() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCabang.clear()
                val namaCabangList = mutableListOf<String>()

                namaCabangList.add(getString(R.string.semua_cabang))
                listCabang.add(ModelCabang(idCabang = "all", namaCabang = getString(R.string.semua_cabang)))

                for (data in snapshot.children) {
                    val cb = data.getValue(ModelCabang::class.java)
                    if (cb != null && cb.idCabang == data.key) {
                        listCabang.add(cb)
                        cb.namaCabang?.let { namaCabangList.add(it) }
                    }
                }

                cabangAdapter.clear()
                cabangAdapter.addAll(namaCabangList)
                cabangAdapter.notifyDataSetChanged()

                existingPegawai?.namaCabang?.let { currentNama ->
                    val pos = namaCabangList.indexOf(currentNama)
                    if (pos >= 0) spinnerCabang.setSelection(pos)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupButtonActions() {
        btnBack.setOnClickListener { finish() }

        btnSimpan.setOnClickListener {
            val nama = etNamaPegawai.text.toString().trim()
            val email = etEmailPegawai.text.toString().trim()
            val telp = etNomorTelp.text.toString().trim()
            val role = spinnerRole.selectedItem.toString()
            val status = spinnerStatus.selectedItem.toString()
            val selectedPos = spinnerCabang.selectedItemPosition
            val selectedCabangObj = listCabang[selectedPos]

            if (nama.isEmpty()) {
                etNamaPegawai.error = getString(R.string.error_nama_pegawai)
                etNamaPegawai.requestFocus()
                return@setOnClickListener
            }

            val id = if (isEditMode) {
                existingPegawai?.idPegawai ?: return@setOnClickListener
            } else {
                myRef.push().key ?: return@setOnClickListener
            }

            val data = ModelPegawai(
                idPegawai = id,
                namaPegawai = nama,
                emailPegawai = email,
                nomorTelp = telp,
                rolePegawai = role,
                statusPegawai = status,
                idCabang = selectedCabangObj.idCabang,
                namaCabang = selectedCabangObj.namaCabang
            )

            myRef.child(id).setValue(data).addOnSuccessListener {
                Toast.makeText(this, getString(R.string.berhasil_simpan_pegawai), Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, getString(R.string.gagal_simpan_pegawai), Toast.LENGTH_SHORT).show()
            }
        }

        btnHapus.setOnClickListener {
            val id = existingPegawai?.idPegawai ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.hapus_pegawai))
                .setMessage(getString(R.string.konfirmasi_hapus_pegawai))
                .setPositiveButton(getString(R.string.ya)) { _, _ ->
                    myRef.child(id).removeValue().addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.berhasil_hapus_pegawai), Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener {
                        Toast.makeText(this, getString(R.string.gagal_hapus_pegawai), Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(getString(R.string.tidak), null)
                .show()
        }
    }
}