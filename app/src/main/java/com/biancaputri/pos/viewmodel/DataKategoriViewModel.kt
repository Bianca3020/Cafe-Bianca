package com.biancaputri.pos.kategori

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.biancaputri.pos.model.ModelKategori

class DataKategoriViewModel : ViewModel() {

    private val myRef = FirebaseDatabase.getInstance().getReference("kategori")

    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()
    private var originalKategoriList = ArrayList<ModelKategori>()

    init {
        loadData()
    }

    private fun loadData() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = ArrayList<ModelKategori>()

                for (data in snapshot.children) {
                    val kategori = data.getValue(ModelKategori::class.java)
                    if (kategori != null) {
                        items.add(kategori)
                    }
                }

                originalKategoriList = items
                kategoriList.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Optional: log error
            }
        })
    }

    fun filter(query: String) {
        val filteredList = if (query.isEmpty()) {
            originalKategoriList
        } else {
            ArrayList(
                originalKategoriList.filter {
                    it.namaKategori
                        ?.lowercase()
                        ?.contains(query.lowercase()) == true
                }
            )
        }

        kategoriList.value = filteredList
    }
}