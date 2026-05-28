package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelCabang
import java.util.Locale

class CabangAdapter(
    private val cabangList: MutableList<ModelCabang>
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    private var cabangListFull: List<ModelCabang> = ArrayList(cabangList)

    interface OnClickListener {
        fun onItemClick(cabang: ModelCabang)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelCabang>) {
        cabangList.clear()
        cabangList.addAll(newList)
        cabangListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase(Locale.getDefault()).trim()
        cabangList.clear()
        if (search.isEmpty()) {
            cabangList.addAll(cabangListFull)
        } else {
            for (item in cabangListFull) {
                if (item.namaCabang?.lowercase(Locale.getDefault())?.contains(search)== true) {
                    cabangList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        holder.bind(cabangList[position])
    }

    override fun getItemCount(): Int = cabangList.size

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaCabang: TextView = itemView.findViewById(R.id.tvNamaCabang)
        private val tvAlamatCabang: TextView = itemView.findViewById(R.id.tvAlamatCabang)
        private val btnAtur: Button = itemView.findViewById(R.id.btnAtur)

        fun bind(cabang: ModelCabang) {
            tvNamaCabang.text = cabang.namaCabang
            tvAlamatCabang.text = cabang.alamatCabang
            btnAtur.setOnClickListener { listener?.onItemClick(cabang) }
        }
    }
}