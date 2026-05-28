package com.biancaputri.pos.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R

class PrinterAdapter(
    private val names: List<String>,
    private val addresses: List<String>,
    private val onConnect: (String) -> Unit
) : RecyclerView.Adapter<PrinterAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvPrinterName)
        val addr: TextView = v.findViewById(R.id.tvPrinterAddress)
        val btn: Button = v.findViewById(R.id.btnConnect)
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_data_printer, p, false))

    override fun onBindViewHolder(h: VH, i: Int) {
        h.name.text = names[i]
        h.addr.text = addresses[i]
        h.btn.setOnClickListener { onConnect(addresses[i]) }
    }

    override fun getItemCount() = names.size
}