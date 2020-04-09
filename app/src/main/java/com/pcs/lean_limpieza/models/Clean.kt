package com.pcs.lean_limpieza.models

import com.google.gson.annotations.SerializedName
import com.pcs.lean_limpieza.tools.Utils
import java.util.*

data class Clean(
    @SerializedName("position") var position: Int = 0,
    @SerializedName("id") var id: Long = 0,
    @SerializedName("conf") var conf: String = "",
    @SerializedName("name") var confName: String = "",
    @SerializedName("operators") var operators: Int = 1,
    @SerializedName("start") var start: Date? = null,
    @SerializedName("end") var end: Date? = null,
    @SerializedName("obs") var obs: String = ""
){
    val pending: Boolean
        get() = (start!=null && end==null)

    fun isValid(): Boolean{
        return (operators>0)
    }

    fun getSearchCriteria(): String{
        if(start==null)
            return ""
        return Utils.dateToString(start!!)
    }
}