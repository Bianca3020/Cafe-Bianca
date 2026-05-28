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
import com.biancaputri.pos.adapter.CabangAdapter
import com.biancaputri.pos.model.ModelCabang
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.biancaputri.pos.mod.ModCabangActivity

class CabangActivity : AppCompatActivity() {

    private lateinit var recyclerViewCabang: RecyclerView
    private lateinit var edtSearchCabang: EditText
    private lateinit var fabAddCabang: FloatingActionButton
    private lateinit var adapter: CabangAdapter

    private val database = FirebaseDatabase.getInstance(
        "https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private lateinit var myRef: DatabaseReference

    private val cabangListFull = mutableListOf<ModelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cabang)

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
        myRef = database.getReference("cabang").child(idAkun)

        recyclerViewCabang = findViewById(R.id.recyclerViewCabang)
        edtSearchCabang = findViewById(R.id.edtSearchCabang)
        fabAddCabang = findViewById(R.id.fabAddCabang)

        adapter = CabangAdapter(mutableListOf())
        recyclerViewCabang.layoutManager = LinearLayoutManager(this)
        recyclerViewCabang.adapter = adapter

        edtSearchCabang.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCabang(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadCabang()

        fabAddCabang.setOnClickListener {
            val intent = Intent(this, ModCabangActivity::class.java)
            startActivity(intent)
        }

        adapter.setOnClickListener(object : CabangAdapter.OnClickListener {
            override fun onItemClick(cabang: ModelCabang) {
                val intent = Intent(this@CabangActivity, ModCabangActivity::class.java)
                intent.putExtra("EXTRA_CABANG", cabang)
                startActivity(intent)
            }
        })
    }

    private fun filterCabang(query: String) {
        val filtered = if (query.trim().isEmpty()) {
            cabangListFull
        } else {
            cabangListFull.filter {
                it.namaCabang?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.updateData(filtered.toMutableList())
    }

    private fun loadCabang() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cabangListFull.clear()
                for (data in snapshot.children) {
                    val cabang = data.getValue(ModelCabang::class.java)
                    if (cabang != null && !cabang.idCabang.isNullOrEmpty() && data.key == cabang.idCabang) {
                        cabangListFull.add(cabang)
                    }
                }

                val currentQuery = if (::edtSearchCabang.isInitialized) edtSearchCabang.text.toString() else ""
                filterCabang(currentQuery)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CabangActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
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