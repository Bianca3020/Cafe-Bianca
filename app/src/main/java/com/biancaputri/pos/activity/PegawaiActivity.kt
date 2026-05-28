package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.KategoriFilterAdapter
import com.biancaputri.pos.adapter.PegawaiAdapter
import com.biancaputri.pos.model.ModelCabang
import com.biancaputri.pos.model.ModelKategori
import com.biancaputri.pos.model.ModelPegawai
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.biancaputri.pos.mod.ModPegawaiActivity

class PegawaiActivity : AppCompatActivity() {

    private lateinit var recyclerViewPegawai: RecyclerView
    private lateinit var rvCabangFilter: RecyclerView
    private lateinit var edtSearchPegawai: EditText
    private lateinit var fabAddPegawai: FloatingActionButton
    private lateinit var adapter: PegawaiAdapter
    private lateinit var filterAdapter: KategoriFilterAdapter

    private val database = FirebaseDatabase.getInstance("https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var myRef: DatabaseReference
    private lateinit var cabangRef: DatabaseReference
    private val pegawaiListFull = mutableListOf<ModelPegawai>()
    private val cabangList = mutableListOf<ModelKategori>()
    private var selectedCabang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pegawai)

        setupEdgeToEdge()

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_gagal), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        selectedCabang = getString(R.string.semua_cabang)

        myRef = database.getReference("pegawai").child(idAkun)
        cabangRef = database.getReference("cabang").child(idAkun)

        recyclerViewPegawai = findViewById(R.id.recyclerViewPegawai)
        rvCabangFilter = findViewById(R.id.rvCabangFilter)
        edtSearchPegawai = findViewById(R.id.edtSearchPegawai)
        fabAddPegawai = findViewById(R.id.fabAddPegawai)

        adapter = PegawaiAdapter(mutableListOf())
        recyclerViewPegawai.layoutManager = LinearLayoutManager(this)
        recyclerViewPegawai.adapter = adapter

        setupSearch()
        loadCabang()
        loadPegawai()

        fabAddPegawai.setOnClickListener {
            startActivity(Intent(this, ModPegawaiActivity::class.java))
        }

        adapter.setOnClickListener(object : PegawaiAdapter.OnClickListener {
            override fun onItemClick(pegawai: ModelPegawai) {
                val intent = Intent(this@PegawaiActivity, ModPegawaiActivity::class.java)
                intent.putExtra("EXTRA_PEGAWAI", pegawai)
                startActivity(intent)
            }
        })
    }

    private fun setupSearch() {
        edtSearchPegawai.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPegawai(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCabang() {
        cabangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangList.clear()
                cabangList.add(ModelKategori(idKategori = "all", namaKategori = getString(R.string.semua_cabang)))
                for (data in snapshot.children) {
                    val cb = data.getValue(ModelCabang::class.java)
                    if (cb != null) {
                        cabangList.add(ModelKategori(idKategori = data.key, namaKategori = cb.namaCabang))
                    }
                }
                setupCabangFilter()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupCabangFilter() {
        filterAdapter = KategoriFilterAdapter(cabangList, object : KategoriFilterAdapter.OnKategoriClickListener {
            override fun onKategoriClick(kategori: ModelKategori) {
                selectedCabang = kategori.namaKategori ?: getString(R.string.semua_cabang)
                filterPegawai(edtSearchPegawai.text.toString())
            }
        })
        rvCabangFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCabangFilter.adapter = filterAdapter
    }

    private fun filterPegawai(query: String) {
        val allText = getString(R.string.semua_cabang)
        val filtered = pegawaiListFull.filter { peg ->
            val matchSearch = query.isEmpty() || 
                             peg.namaPegawai?.contains(query, true) == true || 
                             peg.rolePegawai?.contains(query, true) == true
            

            val matchCabang = selectedCabang == allText || 
                             peg.namaCabang == selectedCabang ||
                             peg.namaCabang == getString(R.string.semua_cabang)
            
            matchSearch && matchCabang
        }
        adapter.updateData(filtered)
    }

    private fun loadPegawai() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pegawaiListFull.clear()
                for (data in snapshot.children) {
                    val pegawai = data.getValue(ModelPegawai::class.java)
                    if (pegawai != null) {
                        pegawai.idPegawai = data.key
                        pegawaiListFull.add(pegawai)
                    }
                }
                filterPegawai(edtSearchPegawai.text.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PegawaiActivity, getString(R.string.gagal_muat_data_detail, error.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupEdgeToEdge() {
        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
}