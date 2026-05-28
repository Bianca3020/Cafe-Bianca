package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori
import java.util.Locale

class KategoriAdapter(
    private var listKategori: ArrayList<ModelKategori>
) : RecyclerView.Adapter<KategoriAdapter.ViewHolder>() {

    private var listKategoriFull: List<ModelKategori> = ArrayList(listKategori)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKategori: TextView = itemView.findViewById(R.id.tvNamaKategori)
        val btnAtur: Button = itemView.findViewById(R.id.btnAtur)
    }

    interface OnClickListener {
        fun onItemClick(kategori: ModelKategori)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kategori = listKategori[position]
        holder.tvNamaKategori.text = kategori.namaKategori
        holder.btnAtur.setOnClickListener { listener?.onItemClick(kategori) }
        holder.itemView.setOnClickListener { listener?.onItemClick(kategori) }
    }

    override fun getItemCount(): Int = listKategori.size

    fun updateData(newList: ArrayList<ModelKategori>) {
        listKategori.clear()
        listKategori.addAll(newList)
        listKategoriFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase(Locale.getDefault()).trim()
        listKategori.clear()
        if (search.isEmpty()) {
            listKategori.addAll(listKategoriFull)
        } else {
            listKategoriFull.filter {
                it.namaKategori?.lowercase(Locale.getDefault())?.contains(search) == true
            }.let { listKategori.addAll(it) }
        }
        notifyDataSetChanged()
    }
}
