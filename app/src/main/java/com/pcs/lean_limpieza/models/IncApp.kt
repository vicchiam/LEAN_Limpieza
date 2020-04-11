package com.pcs.lean_limpieza.models

import com.google.gson.annotations.SerializedName

data class IncApp(
    @SerializedName("id") var id: Long = 0,
    @SerializedName("code") var code: String = "",
    @SerializedName("minutes") var minutes: Int = 0,
    @SerializedName("name") var name: String = ""
){}