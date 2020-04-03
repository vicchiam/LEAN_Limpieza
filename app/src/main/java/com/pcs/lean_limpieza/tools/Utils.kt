package com.pcs.lean_limpieza.tools

import android.app.AlertDialog
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pcs.lean_limpieza.R
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {

        fun stringToDate(text: String, delimiter: String = "/"): Date {
            val aux=text.split(delimiter)
            if(aux.size==3){
                val day= aux.get(0).toInt()
                val month = aux.get(1).toInt()
                val year = aux.get(2).toInt()
                val cal: Calendar = Calendar.getInstance()
                cal.set(year, (month-1), day)
                return cal.time
            }
            return Date()
        }

        fun dateToString(date: Date, pattern: String = "dd/MM/yyyy" ): String{
            val format = SimpleDateFormat(pattern, Locale.getDefault())
            return format.format(date)
        }

        fun closeKeyboard(context: Context, activity: AppCompatActivity){
            val imm =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }

        fun isDoubleFragment(context: Context): Boolean{
            return context.resources.getIdentifier("fragment_container2", "id", context.packageName)!=0
        }

        inline fun <reified T> fromJson(jsonString: String): T{
            val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
            return gson.fromJson<T>(jsonString, object : TypeToken<T>() {}.type)
        }

        fun alert(context: Context, message: String, title: String = "Advertencia"){
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setNeutralButton("Aceptar"){ dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }

        fun modalAlert(activity: AppCompatActivity, message: String = "Cargando..."): AlertDialog {
            val builder = AlertDialog.Builder(activity)
            val dialogView =  activity.layoutInflater.inflate(R.layout.progress_dialog, null)
            val messageTextView = dialogView.findViewById<TextView>(R.id.message)
            messageTextView.text = message
            builder.setView(dialogView)
            builder.setCancelable(false)
            val dialog = builder.create()
            return dialog
        }

    }

}