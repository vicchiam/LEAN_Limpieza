package com.pcs.lean_limpieza.models

import com.google.gson.annotations.SerializedName

data class Inc(
    @SerializedName("code") var code: String = "",
    @SerializedName("name") var name: String = ""
){}