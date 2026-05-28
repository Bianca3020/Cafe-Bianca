package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelProduk
import com.bumptech.glide.Glide

class TransaksiProdukAdapter(
    private val produkList: MutableList<ModelProduk>
) : RecyclerView.Adapter<TransaksiProdukAdapter.ViewHolder>() {

    interface OnCartChangeListener {
        fun onQuantityChanged(produk: ModelProduk, quantity: Int)
    }

    private var listener: OnCartChangeListener? = null
    private val cartMap = mutableMapOf<String, Int>()

    fun setOnCartChangeListener(listener: OnCartChangeListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelProduk>) {
        produkList.clear()
        produkList.addAll(newList)
        notifyDataSetChanged()
    }

    fun getCartMap(): Map<String, Int> = cartMap

    fun clearCart() {
        cartMap.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(produkList[position])
    }

    override fun getItemCount(): Int = produkList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgProduk: ImageView = itemView.findViewById(R.id.imgProduk)
        private val txtNama: TextView = itemView.findViewById(R.id.txtNamaProduk)
        private val txtHarga: TextView = itemView.findViewById(R.id.txtHarga)
        private val btnAtur: Button = itemView.findViewById(R.id.btnAtur)
        private val btnMinus: View = itemView.findViewById(R.id.btnMinus)
        private val btnPlus: View = itemView.findViewById(R.id.btnPlus)
        private val txtQtyItem: TextView = itemView.findViewById(R.id.txtQtyItem)
        private val txtKategori: TextView = itemView.findViewById(R.id.txtKategori)
        private val txtStok: TextView = itemView.findViewById(R.id.txtStok)
        private val txtCabang: TextView = itemView.findViewById(R.id.txtCabang)

        fun bind(produk: ModelProduk) {
            val context = itemView.context
            val id = produk.idProduk ?: ""
            val qty = cartMap[id] ?: 0

            txtNama.text = produk.namaProduk ?: "-"
            
            val hargaStr = String.format("%,d", produk.hargaProduk ?: 0).replace(',', '.')
            txtHarga.text = context.getString(R.string.format_rupiah, hargaStr)
            
            txtKategori.text = produk.idKategori ?: context.getString(R.string.umum)
            txtCabang.text = produk.idCabang ?: context.getString(R.string.utama)

            if (produk.tanpaBatas == "ya" || produk.stokProduk == null) {
                txtStok.text = context.getString(R.string.tidak_terbatas)
            } else {
                txtStok.text = context.getString(R.string.format_stok, produk.stokProduk.toString())
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

            if (qty > 0) {
                btnAtur.visibility = View.GONE
                btnMinus.visibility = View.VISIBLE
                txtQtyItem.visibility = View.VISIBLE
                btnPlus.visibility = View.VISIBLE
                txtQtyItem.text = qty.toString()
            } else {
                btnAtur.visibility = View.VISIBLE
                btnMinus.visibility = View.GONE
                txtQtyItem.visibility = View.GONE
                btnPlus.visibility = View.GONE
            }

            val addAction = View.OnClickListener {
                val currentQty = cartMap[id] ?: 0
                val isStokTerbatas = produk.tanpaBatas != "ya"
                val limitReached = isStokTerbatas && produk.stokProduk?.let { currentQty >= it } ?: false
                if (limitReached) {
                    Toast.makeText(context, context.getString(R.string.stok_kurang), Toast.LENGTH_SHORT).show()
                } else {
                    val newQty = currentQty + 1
                    cartMap[id] = newQty
                    notifyItemChanged(adapterPosition)
                    listener?.onQuantityChanged(produk, newQty)
                }
            }

            btnAtur.setOnClickListener(addAction)
            btnPlus.setOnClickListener(addAction)

            btnMinus.setOnClickListener {
                val currentQty = cartMap[id] ?: 0
                if (currentQty > 0) {
                    val newQty = currentQty - 1
                    if (newQty == 0) {
                        cartMap.remove(id)
                    } else {
                        cartMap[id] = newQty
                    }
                    notifyItemChanged(adapterPosition)
                    listener?.onQuantityChanged(produk, newQty)
                }
            }

            itemView.setOnClickListener {
                if (qty > 0) btnPlus.performClick() else btnAtur.performClick()
            }
        }
    }
}
