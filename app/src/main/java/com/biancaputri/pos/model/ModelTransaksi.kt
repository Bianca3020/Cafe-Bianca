package com.biancaputri.pos.model

import android.os.Parcel
import android.os.Parcelable

data class ModelTransaksiItem(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var hargaProduk: Int? = 0,
    var quantity: Int? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeInt(hargaProduk ?: 0)
        parcel.writeInt(quantity ?: 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelTransaksiItem> {
        override fun createFromParcel(parcel: Parcel): ModelTransaksiItem = ModelTransaksiItem(parcel)
        override fun newArray(size: Int): Array<ModelTransaksiItem?> = arrayOfNulls(size)
    }
}

data class ModelTransaksi(
    var idTransaksi: String? = null,
    var tanggalTransaksi: String? = null,
    var totalHarga: Int? = 0,
    var jumlahBayar: Int? = 0,
    var kembalian: Int? = 0,
    var kasirName: String? = null,
    var pelangganName: String? = null,
    var cabangName: String? = null,
    var metodePembayaran: String? = null,
    var items: List<ModelTransaksiItem>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(ModelTransaksiItem.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idTransaksi)
        parcel.writeString(tanggalTransaksi)
        parcel.writeInt(totalHarga ?: 0)
        parcel.writeInt(jumlahBayar ?: 0)
        parcel.writeInt(kembalian ?: 0)
        parcel.writeString(kasirName)
        parcel.writeString(pelangganName)
        parcel.writeString(cabangName)
        parcel.writeString(metodePembayaran)
        parcel.writeTypedList(items)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelTransaksi> {
        override fun createFromParcel(parcel: Parcel): ModelTransaksi = ModelTransaksi(parcel)
        override fun newArray(size: Int): Array<ModelTransaksi?> = arrayOfNulls(size)
    }
}
