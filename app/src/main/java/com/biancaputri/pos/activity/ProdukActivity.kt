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
import com.biancaputri.pos.adapter.ProdukAdapter
import com.biancaputri.pos.mod.ModProdukActivity
import com.biancaputri.pos.model.ModelCabang
import com.biancaputri.pos.model.ModelKategori
import com.biancaputri.pos.model.ModelProduk
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class ProdukActivity : AppCompatActivity() {

    private lateinit var recyclerViewProduk: RecyclerView
    private lateinit var rvCabangFilter: RecyclerView
    private lateinit var edtSearchProduk: EditText
    private lateinit var fabAddProduk: FloatingActionButton
    private lateinit var adapter: ProdukAdapter
    private lateinit var filterAdapter: KategoriFilterAdapter

    private val database = FirebaseDatabase.getInstance("https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var myRef: DatabaseReference
    private lateinit var cabangRef: DatabaseReference

    private val produkListFull = mutableListOf<ModelProduk>()
    private val cabangList = mutableListOf<ModelKategori>()
    private var selectedCabang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_produk)

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

        myRef = database.getReference("produk").child(idAkun)
        cabangRef = database.getReference("cabang").child(idAkun)

        initView()
        setupEdgeToEdge()
        loadCabang()
        loadProduk()
    }

    private fun initView() {
        recyclerViewProduk = findViewById(R.id.recyclerViewProduk)
        rvCabangFilter = findViewById(R.id.rvCabangFilter)
        edtSearchProduk = findViewById(R.id.edtSearchProduk)
        fabAddProduk = findViewById(R.id.fabAddProduk)

        adapter = ProdukAdapter(mutableListOf())
        recyclerViewProduk.layoutManager = LinearLayoutManager(this)
        recyclerViewProduk.adapter = adapter

        edtSearchProduk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabAddProduk.setOnClickListener {
            val intent = Intent(this, ModProdukActivity::class.java)
            startActivity(intent)
        }

        adapter.setOnClickListener(object : ProdukAdapter.OnClickListener {
            override fun onAturClick(produk: ModelProduk) {
                val intent = Intent(this@ProdukActivity, ModProdukActivity::class.java)
                intent.putExtra("EXTRA_PRODUK", produk)
                startActivity(intent)
            }
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
                applyFilters()
            }
        })
        rvCabangFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCabangFilter.adapter = filterAdapter
    }

    private fun applyFilters() {
        val query = edtSearchProduk.text.toString().trim().lowercase()
        val allText = getString(R.string.semua_cabang)
        
        val filtered = produkListFull.filter { produk ->
            val matchSearch = query.isEmpty() || produk.namaProduk?.lowercase()?.contains(query) == true

            val matchCabang = selectedCabang == allText || 
                             produk.idCabang == selectedCabang ||
                             produk.idCabang == ""
            
            matchSearch && matchCabang
        }
        adapter.updateData(filtered.toMutableList())
    }

    private fun loadProduk() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                produkListFull.clear()
                for (data in snapshot.children) {
                    val produk = data.getValue(ModelProduk::class.java)
                    if (produk != null) {
                        produk.idProduk = data.key
                        if (!produk.namaProduk.isNullOrEmpty()) {
                            produkListFull.add(produk)
                        }
                    }
                }
                applyFilters()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProdukActivity, getString(R.string.gagal_muat_data_detail, error.message), Toast.LENGTH_SHORT).show()
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
