package com.pcs.lean_limpieza.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.tools.Prefs
import com.pcs.lean_limpieza.tools.Utils
import java.lang.Integer.parseInt

class SettingsFragment : Fragment(){

    private lateinit var prefs : Prefs

    private lateinit var spinner: Spinner
    private lateinit var editId: EditText
    private lateinit var editUrl: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        val view: View =inflater.inflate(R.layout.fragment_settings,container,false)

        prefs = Prefs(this.context!!)

        editId = view.findViewById(R.id.app_id)
        editId.setText(prefs.idApp.toString())

        val data = resources.getStringArray(R.array.centers)
        val adapter = ArrayAdapter(context!!, R.layout.spinner_item_selected, data)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner = view.findViewById(R.id.spinner_center)
        spinner.adapter = adapter

        val url = prefs.settingsUrl
        editUrl = view.findViewById(R.id.edit_url)
        editUrl.setText(url)

        val button: MaterialButton = view.findViewById(R.id.btn_settings)
        button.setOnClickListener {
            save()
        }

        return view
    }

    private fun isValid() :Boolean {
        if( editId.text.isEmpty()){
            Utils.alert(context!!, "Debes indicar un ID")
            return false
        }
        if( editUrl.text.isEmpty()){
            Utils.alert(context!!, "Debes indicar una URL")
            return false
        }
        return true
    }

    private fun save(){
        if(isValid()){
            val id = editId.text.toString()
            prefs.idApp = parseInt(id)

            val position: Int = spinner.selectedItemPosition
            prefs.settingsCenter = position

            val url = editUrl.text.toString()
            prefs.settingsUrl = url
            Utils.alert(context!!, "Guardado correctamente")
        }
    }

}