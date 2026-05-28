package com.biancaputri.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelProduk(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var hargaProduk: Int? = null,
    var idKategori: String? = null,
    var idCabang: String? = null,
    var fotoProduk: String? = null,
    var stokProduk: Int? = null,
    var tanpaBatas: String? = null,
    var statusProduk: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Parcelable