package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelKategori
import com.google.android.material.card.MaterialCardView

class KategoriFilterAdapter(
    private var list: List<ModelKategori>,
    private val listener: OnKategoriClickListener
) : RecyclerView.Adapter<KategoriFilterAdapter.ViewHolder>() {

    private var selectedPosition = 0

    interface OnKategoriClickListener {
        fun onKategoriClick(kategori: ModelKategori)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNama: TextView = itemView.findViewById(R.id.txtNamaKategori)
        val card: MaterialCardView = itemView.findViewById(R.id.cardKategori)

        fun bind(kategori: ModelKategori, position: Int) {
            txtNama.text = kategori.namaKategori
            
            if (selectedPosition == position) {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.button))
                txtNama.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                card.strokeWidth = 0
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
                txtNama.setTextColor(ContextCompat.getColor(itemView.context, R.color.button))
                card.strokeWidth = 2
            }

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                listener.onKategoriClick(kategori)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kategori_filter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<ModelKategori>) {
        list = newList
        notifyDataSetChanged()
    }

    fun resetSelection() {
        val oldPos = selectedPosition
        selectedPosition = 0
        notifyItemChanged(oldPos)
        notifyItemChanged(0)
    }
}
