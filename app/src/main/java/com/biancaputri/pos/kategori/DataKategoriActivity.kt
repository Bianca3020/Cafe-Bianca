package com.biancaputri.pos.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.DetailKategoriAdapter
import com.biancaputri.pos.model.ModelKategori
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

// --- VIEW MODEL ---
class DataKategoriViewModel : ViewModel() {
    val kategoriList = MutableLiveData<ArrayList<ModelKategori>>()

    init {
        // Temporary dummy data
        val dummyList = arrayListOf(
            ModelKategori("Kategori 1", "Deskripsi 1"),
            ModelKategori("Kategori 2", "Deskripsi 2")
        )
        kategoriList.value = dummyList
    }
}

// --- ACTIVITY ---
class DataKategoriActivity : AppCompatActivity() {

    private val viewModel: DataKategoriViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvKosong: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var searchView: SearchView
    private lateinit var fabAddKategori: FloatingActionButton
    private lateinit var adapter: DetailKategoriAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
        updateGreeting("Bianca") // Dynamic & Multi-language
        setupSearch()

        // FAB Logic to move to Add Screen
        fabAddKategori.setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java)
            startActivity(intent)
        }

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        viewModel.kategoriList.observe(this) { list ->
            adapter = DetailKategoriAdapter(list)
            recyclerView.adapter = adapter

            if (list.isEmpty()) {
                tvKosong.visibility = View.VISIBLE
            } else {
                tvKosong.visibility = View.GONE
            }
        }
    }

    private fun init() {
        recyclerView = findViewById(R.id.recyclerViewKategori)
        tvKosong = findViewById(R.id.tvKosong)
        tvGreeting = findViewById(R.id.tvGreeting)
        searchView = findViewById(R.id.edtSearchKategori)
        fabAddKategori = findViewById(R.id.fabAddKategori)

        adapter = DetailKategoriAdapter(arrayListOf())
        recyclerView.adapter = adapter
    }

    // --- MULTI-LANGUAGE GREETING LOGIC ---
    private fun updateGreeting(userName: String) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Pulls the correct string ID based on time
        val greetingRes = when (hour) {
            in 0..10 -> R.string.sapa_pagi
            in 11..14 -> R.string.sapa_siang
            in 15..18 -> R.string.sapa_sore
            else -> R.string.sapa_malam
        }

        // getString(greetingRes) automatically handles EN vs ID translations
        val greetingWord = getString(greetingRes)
        tvGreeting.text = "$greetingWord, $userName! ✨"
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter logic can be added here
                return true
            }
        })
    }
}
