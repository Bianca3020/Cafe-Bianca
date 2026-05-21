package com.biancaputri.pos.kategori

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.biancaputri.pos.adapter.DetailKategoriAdapter
import com.biancaputri.pos.databinding.ActivityDataKategoriBinding
import com.biancaputri.pos.model.ModelKategori
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataKategoriBinding
    private lateinit var adapter: DetailKategoriAdapter
    private lateinit var database: DatabaseReference
    private val kategoriList = mutableListOf<ModelKategori>()
    private val kategoriListFull = mutableListOf<ModelKategori>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataKategoriBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        loadKategori()

        binding.fabAddKategori.setOnClickListener {
            // TODO: navigasi ke tambah kategori
        }
    }

    private fun setupRecyclerView() {
        adapter = DetailKategoriAdapter(kategoriList)
        binding.recyclerViewKategori.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewKategori.adapter = adapter

        adapter.setOnClickListener(object : DetailKategoriAdapter.OnClickListener {
            override fun onItemClick(kategori: ModelKategori) {
                Toast.makeText(
                    this@DataKategoriActivity,
                    kategori.namaKategori,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupSearch() {
        binding.edtSearchKategori.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterKategori(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterKategori(query: String) {
        val filtered = if (query.isEmpty()) {
            kategoriListFull
        } else {
            kategoriListFull.filter {
                it.namaKategori?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.updateData(filtered)
    }

    private fun loadKategori() {
        database = FirebaseDatabase.getInstance().getReference("kategori")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kategoriList.clear()
                kategoriListFull.clear()
                for (data in snapshot.children) {
                    val kategori = data.getValue(ModelKategori::class.java)
                    if (kategori != null) {
                        kategoriListFull.add(kategori)
                    }
                }
                adapter.updateData(kategoriListFull)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DataKategoriActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}