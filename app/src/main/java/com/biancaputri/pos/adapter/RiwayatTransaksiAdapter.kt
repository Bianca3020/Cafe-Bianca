package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelTransaksi

class RiwayatTransaksiAdapter(
    private val transaksiList: MutableList<ModelTransaksi>,
    private val onItemClick: (ModelTransaksi) -> Unit
) : RecyclerView.Adapter<RiwayatTransaksiAdapter.ViewHolder>() {

    fun updateData(newList: List<ModelTransaksi>) {
        transaksiList.clear()
        transaksiList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_transaksi, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = transaksiList[position]
        holder.bind(transaksi)
        holder.itemView.setOnClickListener {
            onItemClick(transaksi)
        }
    }

    override fun getItemCount(): Int = transaksiList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTanggalTransaksi: TextView = itemView.findViewById(R.id.tvTanggalTransaksi)
        private val tvTotalTransaksi: TextView = itemView.findViewById(R.id.tvTotalTransaksi)
        private val tvItemsSummary: TextView = itemView.findViewById(R.id.tvItemsSummary)
        private val tvKasirName: TextView = itemView.findViewById(R.id.tvKasirName)

        fun bind(transaksi: ModelTransaksi) {
            val context = itemView.context
            tvTanggalTransaksi.text = transaksi.tanggalTransaksi ?: "-"
            
            val totalStr = String.format("%,d", transaksi.totalHarga ?: 0).replace(',', '.')
            tvTotalTransaksi.text = context.getString(R.string.format_rupiah, totalStr)
            
            val kasir = transaksi.kasirName ?: context.getString(R.string.pengguna_default)
            tvKasirName.text = "${context.getString(R.string.label_kasir)}: $kasir"

            val summary = transaksi.items?.joinToString { item ->
                "${item.quantity}x ${item.namaProduk}"
            } ?: ""
            tvItemsSummary.text = summary
        }
    }
}