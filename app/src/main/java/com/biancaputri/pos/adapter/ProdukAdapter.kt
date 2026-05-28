package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelProduk
import com.bumptech.glide.Glide
import java.util.Locale

class ProdukAdapter(
    private val produkList: MutableList<ModelProduk>
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    private var produkListFull: List<ModelProduk> = ArrayList(produkList)

    interface OnClickListener {
        fun onAturClick(produk: ModelProduk)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelProduk>) {
        produkList.clear()
        produkList.addAll(newList)
        produkListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase(Locale.getDefault()).trim()
        produkList.clear()
        if (search.isEmpty()) {
            produkList.addAll(produkListFull)
        } else {
            for (item in produkListFull) {
                if (item.namaProduk?.lowercase(Locale.getDefault())?.contains(search) == true) {
                    produkList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_produk, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) = holder.bind(produkList[position])

    override fun getItemCount(): Int = produkList.size

    inner class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)
        private val txtNama: TextView = itemView.findViewById(R.id.txtNamaProduk)
        private val txtHarga: TextView = itemView.findViewById(R.id.txtHarga)
        private val btnAtur: Button = itemView.findViewById(R.id.btnAtur)
        private val txtKategori: TextView = itemView.findViewById(R.id.txtKategori)
        private val txtStok: TextView = itemView.findViewById(R.id.txtStok)
        private val txtCabang: TextView = itemView.findViewById(R.id.txtCabang)

        fun bind(produk: ModelProduk) {
            val context = itemView.context
            txtNama.text = produk.namaProduk?.ifEmpty { "-" } ?: "-"
            
            val hargaStr = String.format("%,d", produk.hargaProduk ?: 0).replace(',', '.')
            txtHarga.text = context.getString(R.string.format_rupiah, hargaStr)
            
            txtKategori.text = produk.idKategori?.ifEmpty { context.getString(R.string.umum) } ?: context.getString(R.string.umum)
            txtCabang.text = produk.idCabang?.ifEmpty { context.getString(R.string.utama) } ?: context.getString(R.string.utama)

            if (produk.tanpaBatas == "ya") {
                txtStok.text = context.getString(R.string.tidak_terbatas)
            } else {
                txtStok.text = context.getString(R.string.format_stok, (produk.stokProduk ?: 0).toString())
            }

            if (!produk.fotoProduk.isNullOrEmpty()) {
                Glide.with(context)
                    .load(produk.fotoProduk)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .centerCrop()
                    .into(imgProduk)
            } else {
                imgProduk.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnAtur.setOnClickListener { listener?.onAturClick(produk) }
        }
    }
}
