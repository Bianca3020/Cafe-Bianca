package com.biancaputri.pos.kategori

import android.content.Context
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
import com.biancaputri.pos.activity.LoginActivity
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.KategoriAdapter
import com.biancaputri.pos.model.ModelKategori
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private lateinit var fabAddKategori: FloatingActionButton
    private lateinit var recyclerViewKategori: RecyclerView
    private lateinit var edtSearchKategori: EditText
    private lateinit var kategoriAdapter: KategoriAdapter
    private val listKategoriFull = ArrayList<ModelKategori>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kategori)

        val prefs = getSharedPreferences("session", Context.MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""
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
        setupRecyclerView()
        setupEdgeToEdge()
        setupSearch()

        fabAddKategori.setOnClickListener {
            startActivity(Intent(this@DataKategoriActivity, ModKategoriActivity::class.java))
        }

        getFirebaseData()
    }

    private fun initView() {
        fabAddKategori = findViewById(R.id.fabAddKategori)
        recyclerViewKategori = findViewById(R.id.recyclerViewKategori)
        edtSearchKategori = findViewById(R.id.edtSearchKategori)
    }

    private fun setupRecyclerView() {
        kategoriAdapter = KategoriAdapter(ArrayList())
        recyclerViewKategori.apply {
            layoutManager = LinearLayoutManager(this@DataKategoriActivity)
            adapter = kategoriAdapter
            setHasFixedSize(true)
        }
        kategoriAdapter.setOnClickListener(object : KategoriAdapter.OnClickListener {
            override fun onItemClick(kategori: ModelKategori) {
                val intent = Intent(this@DataKategoriActivity, ModKategoriActivity::class.java)
                intent.putExtra("EXTRA_KATEGORI", kategori)
                startActivity(intent)
            }
        })
    }

    private fun setupSearch() {
        edtSearchKategori.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterKategori(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterKategori(query: String) {
        val filtered = if (query.trim().isEmpty()) {
            listKategoriFull
        } else {
            listKategoriFull.filter {
                it.namaKategori?.contains(query, ignoreCase = true) == true
            }
        }
        kategoriAdapter.updateData(ArrayList(filtered))
    }

    private fun getFirebaseData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listKategoriFull.clear()

                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                        if (kategori != null && !kategori.idKategori.isNullOrEmpty() && !kategori.namaKategori.isNullOrEmpty()) {
                            listKategoriFull.add(kategori)
                        }
                    }
                    filterKategori(edtSearchKategori.text.toString())
                } else {
                    kategoriAdapter.updateData(ArrayList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DataKategoriActivity, getString(R.string.gagal_muat_data_detail, error.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupEdgeToEdge() {
        val mainLayout = findViewById<View>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
    }
}