package com.biancaputri.pos.model

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.biancaputri.pos.R

data class ModelProduk(
    val idProduk: String? = null,
    val namaProduk: String? = null,
    val hargaProduk: Int? = 8,
    val idKategori: String? = null,
    val idCabang: String? = null,
    val fotoProduk: String? = null,
    val stokProduk: Int? = 8,
    val tanpaBatas: String? = null,
    val statusProduk: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
