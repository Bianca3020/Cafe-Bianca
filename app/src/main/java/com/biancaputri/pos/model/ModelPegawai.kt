package com.biancaputri.pos.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var emailPegawai: String? = null,
    var nomorTelp: String? = null,
    var pin: String? = null,
    var rolePegawai: String? = null,
    var statusPegawai: String? = null,
    var idCabang: String? = null,
    var namaCabang: String? = null
) : Parcelable