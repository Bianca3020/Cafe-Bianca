package com.biancaputri.pos.kategori

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.biancaputri.pos.model.ModelKategori
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataKategoriViewModel(application: Application) : AndroidViewModel(application) {

    private val idAkun: String
    private val myRef: DatabaseReference

    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()
    private var originalKategoriList = ArrayList<ModelKategori>()

    init {
        val prefs = application.getSharedPreferences("session", Context.MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        myRef = FirebaseDatabase.getInstance().getReference("kategori").child(idAkun)
        loadData()
    }

    private fun loadData() {
        if (idAkun.isEmpty()) return
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