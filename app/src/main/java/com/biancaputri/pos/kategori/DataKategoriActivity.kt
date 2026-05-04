package com.biancaputri.pos.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori

class DetailKategoriAdapter(
    private var kategoriList: MutableList<ModelKategori>
) : RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>() {

    private lateinit var appContext: Context

    interface OnClickListener {
        fun onItemClick(kategori: ModelKategori)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelKategori>) {
        kategoriList.clear()
        kategoriList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun getItemCount(): Int = kategoriList.size

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.bind(kategori)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(kategori)
        }
    }

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaKategori)

        fun bind(kategori: ModelKategori) {
            tvNama.text = kategori.namaKategori ?: "-"
        }
    }
}