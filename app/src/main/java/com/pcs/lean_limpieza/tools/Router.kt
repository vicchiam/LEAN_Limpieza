package com.pcs.lean_limpieza.tools

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

const val TAG = "ROUTER"

class Router{

    companion object {

        fun get(context: Context, url: String, params: String, responseListener: (String) -> Unit, errorListener: (String) -> Unit){
            val finalUrl = if(params.isEmpty()) url else "$url?$params"
            val queue = Volley.newRequestQueue(context)
            val request = StringRequest(
                Request.Method.GET,
                finalUrl,
                Response.Listener<String> { response ->
                    Log.e("RESPONSE", response)
                    responseListener(response)
                },
                Response.ErrorListener { err ->
                    errorListener(error(err))
                }
            )
            queue.add(request)

        }

        fun post(context: Context, url: String, params: HashMap<String, String>, responseListener: (String) -> Unit, errorListener: (String) -> Unit){
            val queue = Volley.newRequestQueue(context)
            val request = object: StringRequest(
                Method.POST,
                url,
                Response.Listener<String> { response ->
                    Log.e("RESPONSE", response)
                    responseListener(response)
                },
                Response.ErrorListener { err ->
                    errorListener(error(err))
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    return params
                }
            }
            queue.add(request)
        }

        private fun error(err: VolleyError): String{
            if(err is TimeoutError || err is NoConnectionError){
                return "No se ha podido conectar. Msg: $err"
            }
            else if(err is ServerError){
                return "Error en el servidor. Msg: $err"
            }
            else if(err is NetworkError){
                return "Error en la red. Msg: $err"
            }
            return "Error. mensaje: $err"

        }
    }

}