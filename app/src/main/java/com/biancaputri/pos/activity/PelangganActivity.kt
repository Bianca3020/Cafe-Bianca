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
import com.biancaputri.pos.adapter.PelangganAdapter
import com.biancaputri.pos.model.ModelPelanggan
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.biancaputri.pos.mod.ModPelangganActivity

class PelangganActivity : AppCompatActivity() {

    private lateinit var recyclerViewPelanggan: RecyclerView
    private lateinit var edtSearchPelanggan: EditText
    private lateinit var fabAddPelanggan: FloatingActionButton
    private lateinit var adapter: PelangganAdapter

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference
    private val pelangganListFull = mutableListOf<ModelPelanggan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pelanggan)

        setupEdgeToEdge()

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, getString(R.string.sesi_invalid), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        myRef = database.getReference("pelanggan").child(idAkun)

        recyclerViewPelanggan = findViewById(R.id.recyclerViewPelanggan)
        edtSearchPelanggan = findViewById(R.id.edtSearchPelanggan)
        fabAddPelanggan = findViewById(R.id.fabAddPelanggan)

        adapter = PelangganAdapter(mutableListOf())
        recyclerViewPelanggan.layoutManager = LinearLayoutManager(this)
        recyclerViewPelanggan.adapter = adapter

        adapter.setOnClickListener(object : PelangganAdapter.OnClickListener {
            override fun onItemClick(pelanggan: ModelPelanggan) {
                val intent = Intent(this@PelangganActivity, ModPelangganActivity::class.java)
                intent.putExtra("EXTRA_PELANGGAN", pelanggan)
                startActivity(intent)
            }
        })

        edtSearchPelanggan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPelanggan(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fabAddPelanggan.setOnClickListener {
            startActivity(Intent(this, ModPelangganActivity::class.java))
        }

        loadPelanggan()
    }

    private fun filterPelanggan(query: String) {
        val filtered = if (query.trim().isEmpty()) {
            pelangganListFull
        } else {
            pelangganListFull.filter {
                it.namaPelanggan?.contains(query, ignoreCase = true) == true ||
                        it.nomorTelp?.contains(query) == true
            }
        }
        adapter.updateData(filtered)
    }

    private fun loadPelanggan() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pelangganListFull.clear()
                for (data in snapshot.children) {
                    val pelanggan = data.getValue(ModelPelanggan::class.java)
                    if (pelanggan != null && !pelanggan.idPelanggan.isNullOrEmpty() && !pelanggan.namaPelanggan.isNullOrEmpty()) {
                        pelangganListFull.add(pelanggan)
                    }
                }
                val currentQuery = edtSearchPelanggan.text.toString()
                filterPelanggan(currentQuery)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PelangganActivity,
                    getString(R.string.gagal_muat_data_detail, error.message),
                    Toast.LENGTH_SHORT).show()
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