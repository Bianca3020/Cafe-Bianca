package com.biancaputri.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPelanggan(
    var idPelanggan: String? = null,
    var namaPelanggan: String? = null,
    var nomorTelp: String? = null,
    var email: String? = null,
    var poin: Int? = 0
) : Parcelable