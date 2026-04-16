package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView // 1. FIX: Added this import!
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori

class DetailKategoriAdapter(private val kategoriList: List<ModelKategori>) :
    RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>() {

    interface OnItemClick {
        fun onItemClick(kategori: ModelKategori)
    }

    private var listener: OnItemClick? = null

    fun setOnClickListener(listener: OnItemClick) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int = kategoriList.size

    inner class KategoriViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        // 2. FIX: Moved the findViewById here to make the code faster (Standard ViewHolder pattern!)
        private val tvNama = itemView.findViewById<TextView>(R.id.tvNamaKategori)

        fun bind(kategori: ModelKategori) {
            // 3. FIX: Make sure 'nama' matches the variable in your ModelKategori class!
            tvNama.text = kategori.namaKategori

            itemView.setOnClickListener {
                listener?.onItemClick(kategori)
            }
        }
    }
}
