package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.model.ModelPegawai
import java.util.Locale

class PegawaiAdapter(
    private val pegawaiList: MutableList<ModelPegawai>
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    private var pegawaiListFull: List<ModelPegawai> = ArrayList(pegawaiList)

    interface OnClickListener {
        fun onItemClick(pegawai: ModelPegawai)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelPegawai>) {
        pegawaiList.clear()
        pegawaiList.addAll(newList)
        pegawaiListFull = ArrayList(newList)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val search = query.lowercase(Locale.getDefault()).trim()
        pegawaiList.clear()
        if (search.isEmpty()) {
            pegawaiList.addAll(pegawaiListFull)
        } else {
            for (item in pegawaiListFull) {
                if (item.namaPegawai?.lowercase(Locale.getDefault())?.contains(search)== true) {
                    pegawaiList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_pegawai, parent, false)
        return PegawaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        holder.bind(pegawaiList[position])
    }

    override fun getItemCount(): Int = pegawaiList.size

    inner class PegawaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNamaPegawai: TextView = itemView.findViewById(R.id.tvNamaPegawai)
        private val tvRolePegawai: TextView = itemView.findViewById(R.id.tvRolePegawai)
        private val tvEmailPegawai: TextView = itemView.findViewById(R.id.tvEmailPegawai)
        private val tvNoHpPegawai: TextView = itemView.findViewById(R.id.tvNoHpPegawai)
        private val btnAtur: Button = itemView.findViewById(R.id.btnAtur)

        fun bind(pegawai: ModelPegawai) {
            tvNamaPegawai.text = pegawai.namaPegawai?.ifEmpty { "-" } ?: "-"
            tvRolePegawai.text = pegawai.rolePegawai?.ifEmpty { "-" } ?: "-"
            tvEmailPegawai.text = pegawai.emailPegawai?.ifEmpty { "-" } ?: "-"
            tvNoHpPegawai.text = pegawai.nomorTelp?.ifEmpty { "-" } ?: "-"
            btnAtur.setOnClickListener { listener?.onItemClick(pegawai) }
        }
    }
}