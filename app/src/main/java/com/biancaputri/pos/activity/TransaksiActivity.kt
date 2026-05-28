package com.biancaputri.pos.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.KategoriFilterAdapter
import com.biancaputri.pos.adapter.TransaksiProdukAdapter
import com.biancaputri.pos.model.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TransaksiActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var edtSearchTransaksi: EditText
    private lateinit var recyclerViewTransaksiProduk: RecyclerView
    private lateinit var rvKategoriFilter: RecyclerView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnPilihPelanggan: Button
    private lateinit var btnPilihKasir: Button
    private lateinit var btnPilihCabang: Button

    private lateinit var adapter: TransaksiProdukAdapter
    private lateinit var kategoriAdapter: KategoriFilterAdapter

    private val database = FirebaseDatabase.getInstance("https://cafebianca-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private lateinit var produkRef: DatabaseReference
    private lateinit var transaksiRef: DatabaseReference
    private lateinit var pelangganRef: DatabaseReference
    private lateinit var pegawaiRef: DatabaseReference
    private lateinit var cabangRef: DatabaseReference
    private lateinit var kategoriRef: DatabaseReference
    private lateinit var idAkun: String

    private val pelangganNamaList = mutableListOf<String>()
    private val kasirNamaList = mutableListOf<String>()
    private val cabangNamaList = mutableListOf<String>()
    private val mapCabang = mutableMapOf<String, String>()
    private val kategoriList = mutableListOf<ModelKategori>()
    private val masterProdukList = mutableListOf<ModelProduk>()

    private var selectedPelanggan = ""
    private var selectedKasir = ""
    private var selectedCabangName = ""
    private var selectedCabangId = "all"
    private var selectedKategoriId = "all"
    private var selectedKategoriName = ""

    private val currentCart = mutableMapOf<String, ModelTransaksiItem>()
    private var totalHargaCart = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        idAkun = prefs.getString("idAkun", "") ?: ""
        if (idAkun.isEmpty()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        produkRef = database.getReference("produk").child(idAkun)
        transaksiRef = database.getReference("transaksi").child(idAkun)
        pelangganRef = database.getReference("pelanggan").child(idAkun)
        pegawaiRef = database.getReference("pegawai").child(idAkun)
        cabangRef = database.getReference("cabang").child(idAkun)
        kategoriRef = database.getReference("kategori").child(idAkun)

        selectedPelanggan = getString(R.string.tanpa_pelanggan)
        selectedKasir = getString(R.string.admin_default)
        selectedCabangName = getString(R.string.semua_cabang)
        selectedKategoriName = getString(R.string.semua_kategori)
        selectedCabangId = "all"
        selectedKategoriId = "all"

        setupEdgeToEdge()
        initView()
        setupRecyclerView()
        setupSearch()

        loadPelanggan()
        loadKasir()
        loadCabang()
        loadKategori()
        loadProduk()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        edtSearchTransaksi = findViewById(R.id.edtSearchProduk)
        recyclerViewTransaksiProduk = findViewById(R.id.recyclerViewTransaksiProduk)
        rvKategoriFilter = findViewById(R.id.rvKategoriFilter)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        btnCheckout = findViewById(R.id.btnCheckout)
        btnPilihPelanggan = findViewById(R.id.btnPilihPelanggan)
        btnPilihKasir = findViewById(R.id.btnPilihKasir)
        btnPilihCabang = findViewById(R.id.btnPilihCabang)

        btnPilihCabang.text = selectedCabangName
        btnPilihPelanggan.setOnClickListener { showPelangganDialog() }
        btnPilihKasir.setOnClickListener { showKasirDialog() }
        btnPilihCabang.setOnClickListener { showCabangDialog() }
        btnBack.setOnClickListener { finish() }

        btnCheckout.setOnClickListener {
            if (currentCart.isEmpty()) {
                Toast.makeText(this, getString(R.string.keranjang_kosong), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showCheckoutDialog()
        }
    }

    private fun applyFilters() {
        val query = edtSearchTransaksi.text.toString().trim().lowercase()
        val filteredList = masterProdukList.filter { produk ->
            val isAktif = produk.statusProduk != getString(R.string.status_non_aktif)
            val matchCabang = if (selectedCabangId == "all") true else produk.idCabang == selectedCabangId || produk.idCabang == selectedCabangName
            val matchKategori = if (selectedKategoriId == "all") true else produk.idKategori == selectedKategoriId || produk.idKategori == selectedKategoriName
            val matchSearch = query.isEmpty() || produk.namaProduk?.lowercase()?.contains(query) == true
            isAktif && matchCabang && matchKategori && matchSearch
        }
        adapter.updateData(filteredList)
    }

    private fun setupRecyclerView() {
        adapter = TransaksiProdukAdapter(mutableListOf())
        recyclerViewTransaksiProduk.layoutManager = LinearLayoutManager(this)
        recyclerViewTransaksiProduk.adapter = adapter
        adapter.setOnCartChangeListener(object : TransaksiProdukAdapter.OnCartChangeListener {
            override fun onQuantityChanged(produk: ModelProduk, quantity: Int) {
                val id = produk.idProduk ?: ""
                if (quantity <= 0) currentCart.remove(id)
                else currentCart[id] = ModelTransaksiItem(id, produk.namaProduk, produk.hargaProduk ?: 0, quantity)
                updateCartSummary()
            }
        })

        kategoriAdapter = KategoriFilterAdapter(kategoriList, object : KategoriFilterAdapter.OnKategoriClickListener {
            override fun onKategoriClick(kategori: ModelKategori) {
                selectedKategoriId = kategori.idKategori ?: "all"
                selectedKategoriName = kategori.namaKategori ?: ""
                applyFilters()
            }
        })
        rvKategoriFilter.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvKategoriFilter.adapter = kategoriAdapter
    }

    private fun updateCartSummary() {
        var totalQty = 0
        totalHargaCart = 0
        for (item in currentCart.values) {
            totalQty += item.quantity ?: 0
            totalHargaCart += (item.hargaProduk ?: 0) * (item.quantity ?: 0)
        }
        tvTotalItems.text = getString(R.string.item_dipilih, totalQty)
        tvTotalPrice.text = getString(R.string.format_rupiah, String.format("%,d", totalHargaCart).replace(',', '.'))
    }

    private fun showCheckoutDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null)
        bottomSheetDialog.setContentView(dialogView)

        val tvTotalDialog = dialogView.findViewById<TextView>(R.id.tvTotalDialog)
        val etBayar = dialogView.findViewById<EditText>(R.id.etBayar)
        val tvKembalian = dialogView.findViewById<TextView>(R.id.tvKembalian)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpanTransaksi)
        val btnTunai = dialogView.findViewById<Button>(R.id.btnTunai)
        val btnNonTunai = dialogView.findViewById<Button>(R.id.btnNonTunai)

        val layoutTunai = dialogView.findViewById<LinearLayout>(R.id.layoutTunai)
        val layoutNonTunai = dialogView.findViewById<LinearLayout>(R.id.layoutNonTunai)
        val toggleGroupNonTunai = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupNonTunai)

        tvTotalDialog.text = getString(R.string.total_tagihan, getString(R.string.format_rupiah, String.format("%,d", totalHargaCart).replace(',', '.')))

        var metode = getString(R.string.tunai)
        var subMetode = ""

        fun updateMetodeUI() {
            if (metode == getString(R.string.tunai)) {
                btnTunai.backgroundTintList = ColorStateList.valueOf(getColor(R.color.button))
                btnTunai.setTextColor(getColor(android.R.color.white))
                btnNonTunai.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_variant))
                btnNonTunai.setTextColor(getColor(R.color.text_primary))
                layoutTunai.visibility = View.VISIBLE
                layoutNonTunai.visibility = View.GONE
                etBayar.isEnabled = true
                etBayar.setText("")
                subMetode = ""
                toggleGroupNonTunai.clearChecked()
            } else {
                btnNonTunai.backgroundTintList = ColorStateList.valueOf(getColor(R.color.button))
                btnNonTunai.setTextColor(getColor(android.R.color.white))
                btnTunai.backgroundTintList = ColorStateList.valueOf(getColor(R.color.surface_variant))
                btnTunai.setTextColor(getColor(R.color.text_primary))
                layoutTunai.visibility = View.GONE
                layoutNonTunai.visibility = View.VISIBLE
                etBayar.setText(totalHargaCart.toString())
                etBayar.isEnabled = false
            }
        }

        btnTunai.setOnClickListener { metode = getString(R.string.tunai); updateMetodeUI() }
        btnNonTunai.setOnClickListener { metode = getString(R.string.non_tunai); updateMetodeUI() }

        toggleGroupNonTunai.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                subMetode = when (checkedId) {
                    R.id.btnQRIS -> getString(R.string.qris)
                    R.id.btnTransfer -> getString(R.string.transfer)
                    R.id.btnKartu -> getString(R.string.kartu)
                    else -> ""
                }
            }
        }

        etBayar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val bayar = s.toString().toIntOrNull() ?: 0
                val kembalian = bayar - totalHargaCart

                val formatKembalian = String.format("%,d", if (kembalian >= 0) kembalian else -kembalian).replace(',', '.')
                val nominalFormat = getString(R.string.format_rupiah, formatKembalian)

                if (kembalian >= 0) {
                    tvKembalian.text = getString(R.string.kembalian, nominalFormat)
                    tvKembalian.setTextColor(getColor(R.color.text_primary))
                } else {
                    tvKembalian.text = getString(R.string.uang_kurang, nominalFormat)
                    tvKembalian.setTextColor(getColor(R.color.button_hapus))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })

        btnSimpan.setOnClickListener {
            if (metode == getString(R.string.non_tunai) && subMetode.isEmpty()) {
                Toast.makeText(this, getString(R.string.pilih_metode_dulu), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bayarNominal = etBayar.text.toString().toIntOrNull() ?: 0
            if (metode == getString(R.string.tunai) && bayarNominal < totalHargaCart) {
                Toast.makeText(this, getString(R.string.bayar_kurang), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalMetode = if (metode == getString(R.string.tunai)) metode else "$metode ($subMetode)"
            saveTransaction(finalMetode, if (metode == getString(R.string.tunai)) bayarNominal else totalHargaCart, bottomSheetDialog)
        }

        updateMetodeUI()
        bottomSheetDialog.show()
    }

    private fun saveTransaction(metode: String, bayar: Int, dialog: BottomSheetDialog) {
        val transId = transaksiRef.push().key ?: return
        
        val transData = ModelTransaksi(
            transId,
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            totalHargaCart,
            bayar,
            bayar - totalHargaCart,
            selectedKasir,
            selectedPelanggan,
            selectedCabangName,
            metode,
            currentCart.values.toList()
        )

        transaksiRef.child(transId).setValue(transData).addOnSuccessListener {
            updateStocks()
            dialog.dismiss()
            currentCart.clear()
            adapter.clearCart()
            updateCartSummary()

            val intent = Intent(this, NotaActivity::class.java)
            intent.putExtra("EXTRA_TRANSAKSI", transData)
            startActivity(intent)
        }.addOnFailureListener {
            Toast.makeText(this, getString(R.string.gagal_simpan_transaksi) + ": ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateStocks() {
        for (item in currentCart.values) {
            val prodId = item.idProduk ?: continue
            produkRef.child(prodId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val p = snapshot.getValue(ModelProduk::class.java)
                    if (p != null && p.tanpaBatas != "ya") {
                        val newStock = (p.stokProduk ?: 0) - (item.quantity ?: 0)
                        produkRef.child(prodId).child("stokProduk").setValue(if (newStock < 0) 0 else newStock)
                    }
                }
                override fun onCancelled(p0: DatabaseError) {}
            })
        }
    }

    private fun loadCabang() { cabangRef.addValueEventListener(object : ValueEventListener { override fun onDataChange(s: DataSnapshot) { cabangNamaList.clear(); mapCabang.clear(); cabangNamaList.add(getString(R.string.semua_cabang)); for (d in s.children) { val cb = d.getValue(ModelCabang::class.java); if (cb != null) { val id = d.key ?: ""; val nama = cb.namaCabang ?: ""; cabangNamaList.add(nama); mapCabang[nama] = id } } }; override fun onCancelled(e: DatabaseError) {} }) }
    private fun loadKategori() { kategoriRef.addValueEventListener(object : ValueEventListener { override fun onDataChange(s: DataSnapshot) { kategoriList.clear(); kategoriList.add(ModelKategori("all", getString(R.string.semua_kategori))); for (d in s.children) { val kat = d.getValue(ModelKategori::class.java); if (kat != null) kategoriList.add(kat.copy(idKategori = d.key)) }; kategoriAdapter.updateList(kategoriList) }; override fun onCancelled(e: DatabaseError) {} }) }
    private fun loadProduk() { produkRef.addValueEventListener(object : ValueEventListener { override fun onDataChange(s: DataSnapshot) { masterProdukList.clear(); for (d in s.children) { val p = d.getValue(ModelProduk::class.java); if (p != null) masterProdukList.add(p.copy(idProduk = d.key)) }; applyFilters() }; override fun onCancelled(e: DatabaseError) {} }) }
    private fun loadPelanggan() { pelangganRef.addValueEventListener(object : ValueEventListener { override fun onDataChange(s: DataSnapshot) { pelangganNamaList.clear(); for (d in s.children) { val pel = d.getValue(ModelPelanggan::class.java); if (pel != null) pel.namaPelanggan?.let { pelangganNamaList.add(it) } } }; override fun onCancelled(e: DatabaseError) {} }) }
    private fun loadKasir() { pegawaiRef.addValueEventListener(object : ValueEventListener { override fun onDataChange(s: DataSnapshot) { kasirNamaList.clear(); for (d in s.children) { val peg = d.getValue(ModelPegawai::class.java); if (peg != null && peg.statusPegawai == getString(R.string.status_aktif_val)) { if (peg.namaCabang == selectedCabangName || peg.namaCabang == "Semua Cabang") peg.namaPegawai?.let { kasirNamaList.add(it) } } } }; override fun onCancelled(e: DatabaseError) {} }) }

    private fun showCabangDialog() { 
        showCustomSelectionDialog(getString(R.string.pilih_cabang), cabangNamaList) { n, _ -> 
            selectedCabangName = n
            selectedCabangId = if (n == getString(R.string.semua_cabang)) "all" else mapCabang[n] ?: ""
            btnPilihCabang.text = selectedCabangName
            applyFilters()
            loadKasir() 
        } 
    }
    
    private fun showPelangganDialog() { 
        val opt = mutableListOf("+ " + getString(R.string.tambah_pelanggan))
        opt.addAll(pelangganNamaList)
        showCustomSelectionDialog(getString(R.string.pilih_pelanggan), opt) { n, w -> 
            if (w == 0) showInputPelangganManual() 
            else { 
                selectedPelanggan = n
                btnPilihPelanggan.text = selectedPelanggan 
            } 
        } 
    }
    
    private fun showKasirDialog() { 
        showCustomSelectionDialog(getString(R.string.pilih_kasir), kasirNamaList) { n, _ ->
            selectedKasir = n
            btnPilihKasir.text = selectedKasir 
        } 
    }
    
    private fun showSearch() { edtSearchTransaksi.addTextChangedListener(object : TextWatcher { override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) { applyFilters() }; override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}; override fun afterTextChanged(p0: Editable?) {} }) }

    private fun showCustomSelectionDialog(title: String, items: List<String>, onSelected: (String, Int) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pilih_data, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val rvItems = dialogView.findViewById<RecyclerView>(R.id.rvDialogItems)
        val btnTutup = dialogView.findViewById<Button>(R.id.btnTutupDialog)
        tvTitle.text = title
        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class ItemHolder(v: View) : RecyclerView.ViewHolder(v) { val text: TextView = v.findViewById(R.id.tvItemName) }
            override fun onCreateViewHolder(p: ViewGroup, t: Int) = ItemHolder(LayoutInflater.from(p.context).inflate(R.layout.item_dialog_pilih, p, false))
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
                val holder = h as ItemHolder; val itemText = items[pos]; holder.text.text = itemText
                if (itemText.startsWith("+")) { holder.text.setTextColor(getColor(R.color.button)); holder.text.setTypeface(null, Typeface.BOLD) }
                holder.itemView.setOnClickListener { onSelected(itemText, pos); alertDialog.dismiss() }
            }
            override fun getItemCount() = items.size
        }
        btnTutup.setOnClickListener { alertDialog.dismiss() }; alertDialog.show()
    }

    private fun showInputPelangganManual() {
        val input = EditText(this)
        input.hint = getString(R.string.hint_nama_pelanggan_form)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(64, 24, 64, 0)
        input.layoutParams = lp
        input.setPadding(48, 32, 48, 32)
        input.setBackgroundResource(R.drawable.bg_input)
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.addView(input)
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.tambah_pelanggan))
            .setView(container)
            .setPositiveButton(getString(R.string.simpan)) { _, _ ->
                val n = input.text.toString().trim()
                if (n.isNotEmpty()) {
                    val id = pelangganRef.push().key ?: ""
                    pelangganRef.child(id).setValue(ModelPelanggan(idPelanggan = id, namaPelanggan = n))
                    selectedPelanggan = n
                    btnPilihPelanggan.text = n
                }
            }
            .setNegativeButton(getString(R.string.batal), null)
            .show()
    }

    private fun setupEdgeToEdge() { ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets -> val s = insets.getInsets(WindowInsetsCompat.Type.systemBars()); v.setPadding(s.left, s.top, s.right, s.bottom); insets } }
    private fun setupSearch() { edtSearchTransaksi.addTextChangedListener(object : TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}; override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { applyFilters() }; override fun afterTextChanged(s: Editable?) {} }) }
}
