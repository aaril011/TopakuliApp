package com.project.topakuli.data.remote.model
import com.google.gson.annotations.SerializedName

data class TogaResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("nama_tanaman")
    val namaTanaman: String,

    @SerializedName("nama_latin")
    val namaLatin: String,

    @SerializedName("nama_daerah")
    val namaDaerah: String,

    @SerializedName("confidence")
    val confidence: Double
)
