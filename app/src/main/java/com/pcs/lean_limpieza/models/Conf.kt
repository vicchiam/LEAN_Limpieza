package com.pcs.lean_limpieza.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Conf(
    @SerializedName("conf") val conf: String,
    @SerializedName("name") val confName: String
){

    fun getSearchCriteria(): String{
        return "$conf $confName".toLowerCase(Locale.getDefault())
    }

}