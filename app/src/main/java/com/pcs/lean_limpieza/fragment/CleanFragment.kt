package com.pcs.lean_limpieza.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.JsonSyntaxException
import com.pcs.lean_limpieza.MainActivity
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.adapter.CleanAdapter
import com.pcs.lean_limpieza.models.Clean
import com.pcs.lean_limpieza.tools.Prefs
import com.pcs.lean_limpieza.tools.Router
import com.pcs.lean_limpieza.tools.Utils
import java.util.*

class CleanFragment: Fragment() {

    private lateinit var mainActivity: MainActivity

    private val adapter = CleanAdapter()

    private lateinit var recycler: RecyclerView

    private var currentDate: Date = Date()

    private lateinit var dialog: Dialog //Es el fragmento de entrada y puede dar error al abrir en portrait y canviar a landscape

    override fun onDestroy() {
        super.onDestroy()
        if(dialog.isShowing)
            dialog.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
        dialog = Utils.modalAlert(mainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clean, container, false)

        makeFloatingActionButton(view)
        makeSearchDate(view)

        recycler = view.findViewById(R.id.recycler_download)
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(context)

        adapter.cleanAdapter(this, mainActivity.listClean)
        recycler.adapter = adapter

        if(mainActivity.listClean.isEmpty()){
            getCleans()
        }
        else{
            adapter.cleanAdapter(this, mainActivity.listClean)
            recycler.adapter = adapter
        }

        adapter.search(Utils.dateToString(currentDate)){}

        mainActivity.currentAdapter = adapter

        return view
    }

    private fun makeFloatingActionButton(view: View){
        val actionButton: FloatingActionButton = view.findViewById(R.id.fab_download)
        actionButton.setOnClickListener {
            //mainActivity.download = Download()
            mainActivity.navigateToNewClean()
        }
    }

    private fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
        this.setOnTouchListener { v, event ->
            Utils.closeKeyboard(context, mainActivity)

            var hasConsumed = false
            if (v is EditText) {
                if (event.x >= v.width - v.totalPaddingRight) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        onClicked(this)
                    }
                    hasConsumed = true
                }
            }
            hasConsumed
        }
    }

    private fun makeSearchDate(view: View, date: Date = Date()){
        val editText: EditText = view.findViewById(R.id.search_date)
        editText.setText(Utils.dateToString(date))
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val aux: String=dayOfMonth.toString().padStart(2,'0')+"/"+(monthOfYear+1).toString().padStart(2,'0')+"/"+year.toString()
                editText.setText(aux)
                currentDate = Utils.stringToDate(aux)
                //adapter.search(aux){}
            }
        editText.onRightDrawableClicked {
            val cal = Calendar.getInstance()
            cal.time = Utils.stringToDate(editText.text.toString())
            DatePickerDialog(context!!,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun getCleans(){
        val prefs = Prefs(mainActivity)
        val url: String = prefs.settingsUrl

        if(url.isNotEmpty()){
            dialog.show()
            Router.get(
                context = context!!,
                url = url,
                params = "action=get-cleans&id_device=${mainActivity.idApp}",
                responseListener = { response ->
                    if(context!=null){
                        try {
                            val list: List<Clean> = Utils.fromJson(response)
                            mainActivity.listClean = list.toMutableList()
                            adapter.cleanAdapter(this, mainActivity.listClean)
                            recycler.adapter = adapter
                            adapter.search(Utils.dateToString(currentDate)) {}
                        }
                        catch (ex: JsonSyntaxException){
                            Utils.alert(context!!,"El formato de la respuesta no es correcto: $response")
                        }
                        catch (ex: Exception){
                            Utils.alert(context!!,ex.toString())
                        }
                        finally {
                            dialog.dismiss()
                        }
                    }
                    else{
                        Log.e("No Context","AAAAAAAAAAAA")
                    }
                },
                errorListener = { err ->
                    if(context!=null){
                        Utils.alert(context!!, err)
                        dialog.dismiss()
                    }
                }
            )
        }
    }

    fun editClean(clean: Clean){
        mainActivity.clean = clean
        mainActivity.navigateToNewClean()
    }

}