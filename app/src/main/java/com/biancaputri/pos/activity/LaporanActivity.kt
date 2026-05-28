package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.RiwayatTransaksiAdapter
import com.biancaputri.pos.model.ModelTransaksi
import com.google.firebase.database.*

class LaporanActivity : AppCompatActivity() {

    private lateinit var recyclerViewRiwayat: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var tvRevenue: TextView
    private lateinit var tvTransCount: TextView
    private lateinit var tvAverage: TextView

    private lateinit var adapter: RiwayatTransaksiAdapter
    private val transaksiList = mutableListOf<ModelTransaksi>()

    private val database = FirebaseDatabase.getInstance("https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var transaksiRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        transaksiRef = database.getReference("transaksi").child(idAkun)

        initView()
        setupEdgeToEdge()
        setupRecyclerView()
        loadTransaksi()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        tvRevenue = findViewById(R.id.tvRevenue)
        tvTransCount = findViewById(R.id.tvTransCount)
        tvAverage = findViewById(R.id.tvAverage)
        recyclerViewRiwayat = findViewById(R.id.recyclerViewRiwayat)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RiwayatTransaksiAdapter(transaksiList) { transaksi ->
            val intent = Intent(this, NotaActivity::class.java)
            intent.putExtra("EXTRA_TRANSAKSI", transaksi)
            startActivity(intent)
        }
        recyclerViewRiwayat.layoutManager = LinearLayoutManager(this)
        recyclerViewRiwayat.adapter = adapter
    }

    private fun loadTransaksi() {
        transaksiRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transaksiList.clear()
                var totalRevenue = 0

                for (data in snapshot.children) {
                    val transaksi = data.getValue(ModelTransaksi::class.java)
                    if (transaksi != null && transaksi.idTransaksi == data.key) {
                        transaksiList.add(transaksi)
                        totalRevenue += (transaksi.totalHarga ?: 0)
                    }
                }

                transaksiList.sortByDescending { it.tanggalTransaksi }

                val count = transaksiList.size
                val average = if (count > 0) totalRevenue / count else 0

                val revenueStr = String.format("%,d", totalRevenue).replace(',', '.')
                val averageStr = String.format("%,d", average).replace(',', '.')
                
                tvRevenue.text = getString(R.string.format_rupiah, revenueStr)
                tvTransCount.text = count.toString()
                tvAverage.text = getString(R.string.format_rupiah, averageStr)

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                val pesanError = getString(R.string.gagal_muat_laporan) + ": " + error.message
                Toast.makeText(this@LaporanActivity, pesanError, Toast.LENGTH_SHORT).show()
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
