package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelPelanggan
import java.util.Locale

class PelangganAdapter(
    private val pelangganList: MutableList<ModelPelanggan>
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    private var pelangganListFull: List<ModelPelanggan> = ArrayList(pelangganList)

    interface OnClickListener {
        fun onItemClick(pelanggan: ModelPelanggan)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelPelanggan>) {
        pelangganList.clear()
        pelangganList.addAll(newList)
        pelangganListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase(Locale.getDefault()).trim()
        pelangganList.clear()
        if (search.isEmpty()) {
            pelangganList.addAll(pelangganListFull)
        } else {
            pelangganListFull.filter {
                it.namaPelanggan?.lowercase()?.contains(search) == true ||
                        it.nomorTelp?.contains(search) == true
            }.let { pelangganList.addAll(it) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pelanggan, parent, false)
        return PelangganViewHolder(view)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount(): Int = pelangganList.size

    inner class PelangganViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNama: TextView = itemView.findViewById(R.id.tvNamaPelanggan)
        private val tvTelp: TextView = itemView.findViewById(R.id.tvTelpPelanggan)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmailPelanggan)
        private val btnAtur: Button = itemView.findViewById(R.id.btnAtur)

        fun bind(pelanggan: ModelPelanggan) {
            val context = itemView.context
            tvNama.text = pelanggan.namaPelanggan ?: "-"
            tvTelp.text = pelanggan.nomorTelp ?: "-"
            tvEmail.text = pelanggan.email ?: "-"
            btnAtur.setOnClickListener { listener?.onItemClick(pelanggan) }
        }
    }
}