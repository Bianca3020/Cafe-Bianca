package com.biancaputri.pos.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.LocaleListCompat
import com.biancaputri.pos.kategori.DataKategoriActivity
import com.biancaputri.pos.model.ModelPelanggan
import com.biancaputri.pos.model.ModelTransaksi
import com.biancaputri.pos.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvTotalTransaksi: TextView
    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvPelanggan: TextView
    private val database = FirebaseDatabase.getInstance("https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreate(savedInstanceState: Bundle?) {
        applySettings()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val greetingText = findViewById<TextView>(R.id.greetingTextView)
        val dateText = findViewById<TextView>(R.id.dateTextView)
        tvTotalTransaksi = findViewById(R.id.transactionCountText)
        tvTotalPendapatan = findViewById(R.id.incomeText)
        tvPelanggan = findViewById(R.id.customerCountText)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        val userNameText = prefs.getString("nama", getString(R.string.pengguna_default)) ?: getString(
            R.string.pengguna_default)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        greetingText.text = when (hour) {
            in 0..10 -> getString(R.string.sapa_pagi, userNameText)
            in 11..14 -> getString(R.string.sapa_siang, userNameText)
            in 15..17 -> getString(R.string.sapa_sore, userNameText)
            else -> getString(R.string.sapa_malam, userNameText)
        }

        val currentLocale = resources.configuration.locales[0]
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", currentLocale)
        dateText.text = dateFormat.format(Date())

        loadDashboardData()

        val btnSettingAplikasi = findViewById<ImageView>(R.id.btnSettingAplikasi)
        btnSettingAplikasi.setOnClickListener {
            val popup = PopupMenu(this, btnSettingAplikasi)
            popup.menuInflater.inflate(R.menu.menu_pengaturan, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_dark_mode -> {
                        showDarkModeDialog()
                        true
                    }
                    R.id.menu_language -> {
                        showLanguageDialog()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        setupCardClickListener(R.id.transactionCard, TransaksiActivity::class.java)
        setupCardClickListener(R.id.customerCard, DataKategoriActivity::class.java)
        setupCardClickListener(R.id.reportCard, LaporanActivity::class.java)
        setupCardClickListener(R.id.accountCard, AkunActivity::class.java)
        setupCardClickListener(R.id.serviceCard, ProdukActivity::class.java)
        setupCardClickListener(R.id.extraCard, PelangganActivity::class.java)
        setupCardClickListener(R.id.staffCard, PegawaiActivity::class.java)
        setupCardClickListener(R.id.outletCard, CabangActivity::class.java)
        setupCardClickListener(R.id.printerCard, PrinterActivity::class.java)
    }

    private fun applySettings() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val darkMode = prefs.getInt("dark_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(darkMode)


        val lang = prefs.getString("language", "default") ?: "default"
        if (lang == "default") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(lang))
        }
    }

    private fun showDarkModeDialog() {
        val options = resources.getStringArray(R.array.mode_options)
        val values = intArrayOf(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_NO,
            AppCompatDelegate.MODE_NIGHT_YES
        )
        
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = prefs.getInt("dark_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val checkedItem = values.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.pilih_mode))
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                prefs.edit().putInt("dark_mode", values[which]).apply()
                AppCompatDelegate.setDefaultNightMode(values[which])
                dialog.dismiss()
            }
            .show()
    }

    private fun showLanguageDialog() {
        val options = resources.getStringArray(R.array.bahasa_options)
        val codes = arrayOf("default", "id", "en")
        
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val current = prefs.getString("language", "default")
        val checkedItem = codes.indexOf(current)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.pilih_bahasa))
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val selectedLang = codes[which]
                prefs.edit().putString("language", selectedLang).apply()
                
                if (selectedLang == "default") {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                } else {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(selectedLang))
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun setupCardClickListener(cardId: Int, activityClass: Class<*>) {
        val card = findViewById<MaterialCardView>(cardId)
        (card.getChildAt(0) as? LinearLayout ?: card).setOnClickListener {
            startActivity(Intent(this, activityClass))
        }
    }

    private fun loadDashboardData() {
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        val idAkun = prefs.getString("idAkun", "") ?: ""

        if (idAkun.isEmpty()) return

        database.getReference("transaksi").child(idAkun).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0L
                var count = 0
                for (data in snapshot.children) {
                    val tr = data.getValue(ModelTransaksi::class.java)
                    if (tr != null && !tr.idTransaksi.isNullOrEmpty() && data.key == tr.idTransaksi) {
                        total += (tr.totalHarga ?: 0).toLong()
                        count++
                    }
                }
                tvTotalTransaksi.text = count.toString()
                tvTotalPendapatan.text = getString(R.string.format_rupiah, String.format("%,d", total).replace(',', '.'))
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, getString(R.string.gagal_muat_data), Toast.LENGTH_SHORT).show()
            }
        })

        database.getReference("pelanggan").child(idAkun).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (data in snapshot.children) {
                    val pel = data.getValue(ModelPelanggan::class.java)
                    if (pel != null && !pel.idPelanggan.isNullOrEmpty() && data.key == pel.idPelanggan) {
                        count++
                    }
                }
                tvPelanggan.text = count.toString()
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, getString(R.string.gagal_load_pelanggan), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
