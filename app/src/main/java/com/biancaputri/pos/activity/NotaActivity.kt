package com.biancaputri.pos.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelTransaksi
import java.io.File
import java.io.FileOutputStream

class NotaActivity : AppCompatActivity() {

    private lateinit var btnBack: AppCompatImageView
    private lateinit var cardNota: CardView
    private lateinit var tvNotaId: TextView
    private lateinit var tvNotaTanggal: TextView
    private lateinit var tvNotaKasir: TextView
    private lateinit var tvNotaPelanggan: TextView
    private lateinit var tvNotaMetode: TextView
    private lateinit var layoutItems: LinearLayout
    private lateinit var tvNotaTotal: TextView
    private lateinit var layoutBayarKembalian: LinearLayout
    private lateinit var tvNotaBayar: TextView
    private lateinit var tvNotaKembalian: TextView
    private lateinit var btnShareNota: Button
    private lateinit var btnPrintNota: Button
    private lateinit var btnSelesai: Button

    private var transaksi: ModelTransaksi? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nota)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        transaksi = intent.getParcelableExtra("EXTRA_TRANSAKSI")

        initView()
        populateNota()
        setupActions()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btnBack)
        cardNota = findViewById(R.id.cardNota)
        tvNotaId = findViewById(R.id.tvNotaId)
        tvNotaTanggal = findViewById(R.id.tvNotaTanggal)
        tvNotaKasir = findViewById(R.id.tvNotaKasir)
        tvNotaPelanggan = findViewById(R.id.tvNotaPelanggan)
        tvNotaMetode = findViewById(R.id.tvNotaMetode)
        layoutItems = findViewById(R.id.layoutItems)
        tvNotaTotal = findViewById(R.id.tvNotaTotal)
        layoutBayarKembalian = findViewById(R.id.layoutBayarKembalian)
        tvNotaBayar = findViewById(R.id.tvNotaBayar)
        tvNotaKembalian = findViewById(R.id.tvNotaKembalian)
        btnShareNota = findViewById(R.id.btnShareNota)
        btnPrintNota = findViewById(R.id.btnPrintNota)
        btnSelesai = findViewById(R.id.btnSelesai)
    }

    private fun populateNota() {
        val t = transaksi ?: return

        tvNotaId.text = t.idTransaksi?.takeLast(8)?.uppercase() ?: "-"
        tvNotaTanggal.text = t.tanggalTransaksi ?: "-"
        tvNotaKasir.text = t.kasirName ?: "-"
        tvNotaPelanggan.text = t.pelangganName ?: "-"
        tvNotaMetode.text = t.metodePembayaran ?: "-"

        layoutItems.removeAllViews()
        t.items?.forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 6
                layoutParams = params
            }

            val tvNama = TextView(this).apply {
                text = item.namaProduk ?: "-"
                textSize = 12f
                setTextColor(ViewCompat.MEASURED_STATE_MASK)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f)
            }

            val tvQty = TextView(this).apply {
                text = getString(R.string.format_qty, item.quantity ?: 0)
                textSize = 12f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val subtotal = (item.hargaProduk ?: 0) * (item.quantity ?: 0)
            val subtotalFormatted = String.format("%,d", subtotal).replace(',', '.')
            val tvSubtotal = TextView(this).apply {
                text = getString(R.string.format_rupiah, subtotalFormatted)
                textSize = 12f
                setTextColor(ViewCompat.MEASURED_STATE_MASK)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            }

            row.addView(tvNama)
            row.addView(tvQty)
            row.addView(tvSubtotal)
            layoutItems.addView(row)
        }

        val totalFormatted = String.format("%,d", t.totalHarga ?: 0).replace(',', '.')
        tvNotaTotal.text = getString(R.string.format_rupiah, totalFormatted)

        if (t.metodePembayaran?.contains(getString(R.string.tunai), ignoreCase = true) == true) {
            layoutBayarKembalian.visibility = View.VISIBLE
            val bayarFormatted = String.format("%,d", t.jumlahBayar ?: 0).replace(',', '.')
            val kembaliFormatted = String.format("%,d", t.kembalian ?: 0).replace(',', '.')
            tvNotaBayar.text = getString(R.string.format_rupiah, bayarFormatted)
            tvNotaKembalian.text = getString(R.string.format_rupiah, kembaliFormatted)
        } else {
            layoutBayarKembalian.visibility = View.GONE
        }
    }

    private fun setupActions() {
        btnBack.setOnClickListener { finish() }
        btnSelesai.setOnClickListener { finish() }

        btnShareNota.setOnClickListener {
            shareNotaAsImage()
        }

        btnPrintNota.setOnClickListener {
            printNota()
        }
    }

    private fun shareNotaAsImage() {
        try {
            val bitmap = captureCard(cardNota)
            val file = File(cacheDir, "nota_${transaksi?.idTransaksi?.takeLast(8)}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, getString(R.string.pesan_share_nota))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.pilih_share)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.gagal_share, e.message ?: ""), Toast.LENGTH_SHORT).show()
        }
    }

    private fun centerText(text: String): String {
        val lineWidth = 32
        val padding = (lineWidth - text.length) / 2
        return if (padding > 0) " ".repeat(padding) + text else text
    }
    private fun printNota() {
        val t = transaksi ?: return

        val sb = StringBuilder()
        sb.append("${centerText(getString(R.string.nama_toko))}\n")
        sb.append("${centerText("POS System")}\n")
        sb.append("================================\n")
        sb.append("${getString(R.string.label_id_titik, t.idTransaksi?.takeLast(8)?.uppercase() ?: "")}\n")
        sb.append("${getString(R.string.label_tgl_titik, t.tanggalTransaksi ?: "")}\n")
        sb.append("${getString(R.string.label_kasir_titik, t.kasirName ?: "")}\n")
        sb.append("${getString(R.string.label_cabang)} : ${t.cabangName ?: "-"}\n")
        sb.append("${getString(R.string.label_metode_bayar)}: ${t.metodePembayaran}\n")
        sb.append("--------------------------------\n")

        t.items?.forEach { item ->
            val sub = (item.hargaProduk ?: 0) * (item.quantity ?: 0)
            val subFormatted = String.format("%,d", sub).replace(',', '.')
            sb.append("${item.namaProduk}\n")
            sb.append(" ${item.quantity} x ${item.hargaProduk} = $subFormatted\n")
        }

        sb.append("--------------------------------\n")
        val totalFormatted = String.format("%,d", t.totalHarga ?: 0).replace(',', '.')
        sb.append("${getString(R.string.label_total_titik, totalFormatted)}\n")
        
        if (t.metodePembayaran?.contains(getString(R.string.tunai), ignoreCase = true) == true) {
            val bayarFormatted = String.format("%,d", t.jumlahBayar ?: 0).replace(',', '.')
            val kembaliFormatted = String.format("%,d", t.kembalian ?: 0).replace(',', '.')
            sb.append("${getString(R.string.label_bayar_titik, bayarFormatted)}\n")
            sb.append("${getString(R.string.label_kembali_titik, kembaliFormatted)}\n")
        }
        sb.append("================================\n")
        sb.append("${centerText(getString(R.string.terima_kasih))}\n")
        sb.append("\n\n\n\n")

        PrinterActivity.sendPrint(this, sb.toString())
    }

    private fun captureCard(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }
}
