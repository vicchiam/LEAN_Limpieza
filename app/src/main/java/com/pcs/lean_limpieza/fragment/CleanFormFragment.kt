package com.pcs.lean_limpieza.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.gson.JsonSyntaxException
import com.pcs.lean_limpieza.MainActivity
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.adapter.CleanAdapter
import com.pcs.lean_limpieza.models.Clean
import com.pcs.lean_limpieza.models.Conf
import com.pcs.lean_limpieza.models.Inc
import com.pcs.lean_limpieza.models.IncApp
import com.pcs.lean_limpieza.tools.Prefs
import com.pcs.lean_limpieza.tools.Router
import com.pcs.lean_limpieza.tools.Utils
import java.util.*
import kotlin.collections.ArrayList

class CleanFormFragment: Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var listView: ListView

    private lateinit var buttonStart : MaterialButton
    private lateinit var buttonEnd : MaterialButton

    private lateinit var listIncs: List<Inc>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clean_form, container, false)

        val clean: Clean = mainActivity.clean

        val readOnly: Boolean = (clean.start!=null)

        makeTextViewConf(view, clean.conf, clean.confName, readOnly)
        makeEditTextOperator(view, clean.operators, readOnly)
        makeEditTextObs(view, clean.obs)
        makeIncs(view, clean.incApps ?: ArrayList(), readOnly)
        makeButtons(view, clean.start, clean.end)

        if(mainActivity.cache.get("incs")==null){
            getIncs()
        }
        else{
            listIncs = mainActivity.cache.get("incs") as List<Inc>
        }

        return view
    }

    private fun getIncs(){
        val prefs = Prefs(mainActivity)
        val url: String = prefs.settingsUrl

        if(url.isNotEmpty()){
            val dialog: AlertDialog = Utils.modalAlert(mainActivity)
            dialog.show()
            Router.get(
                context = context!!,
                url = url,
                params = "action=get-incs",
                responseListener = { response ->
                    if(context!=null) {
                        try{
                            val list: List<Inc> = Utils.fromJson(response)
                            mainActivity.cache.set("incs", list)
                            listIncs = list
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
                    else
                        dialog.dismiss()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun TextView.onRightDrawableClicked(onClicked: (view: TextView) -> Unit) {
        this.setOnTouchListener { v, event ->
            Utils.closeKeyboard(context, mainActivity)
            var hasConsumed = false
            if (v is TextView && event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
            hasConsumed
        }
    }

    private fun makeTextViewConf(view: View, conf: String = "", confName: String ="", readOnly: Boolean){
        val textView: TextView = view.findViewById(R.id.text_conf)

        if(conf.isEmpty())
            textView.text = ""
        else
            textView.text = "$conf - $confName"

        textView.onRightDrawableClicked {
            if(!readOnly)
                mainActivity.navigateToSelectConf()
        }

    }

    private fun makeEditTextOperator(view: View, num: Int, readOnly: Boolean){
        val editText: EditText = view.findViewById(R.id.edit_num_operators)
        editText.isEnabled=!readOnly

        editText.setText(num.toString())
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty())
                    mainActivity.clean.operators = s.toString().toInt()
            }
        })
    }

    private fun makeEditTextObs(view: View, obs: String){
        val editText: EditText = view.findViewById(R.id.edit_obs)

        editText.setText(obs)
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty())
                    mainActivity.clean.obs = s.toString()
            }
        })
    }

    private fun makeIncs(view: View, incApps: MutableList<IncApp>, buttonActive: Boolean){
        val button: ImageButton = view.findViewById(R.id.inc_add)
        button.isEnabled = buttonActive
        if(!buttonActive)
            button.setBackgroundColor(Color.LTGRAY)
        button.setOnClickListener { _ ->

            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_inc_app, null)

            val builder = AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle("Nueva Incidencia")
                .setPositiveButton("Guardar"){ dialog, _ ->
                    val spinner: Spinner = dialogView.findViewById(R.id.dialog_inc)
                    val editText: EditText = dialogView.findViewById(R.id.dialog_minutes)

                    if(!editText.text.isEmpty() && editText.text.toString().toIntOrNull()!=null ){
                        val inc: Inc = listIncs.get(spinner.selectedItemPosition)
                        val minutes: Int = editText.text.toString().toInt()
                        addIncApp(inc, minutes)
                        dialog.dismiss()
                    }

                }
                .setNegativeButton("Cancelar"){ dialog, _ ->
                    dialog.cancel()
                }

            var incs: MutableList<String> = ArrayList()
            for( inc: Inc in listIncs){
                incs.add(inc.name)
            }
            val spinner: Spinner = dialogView.findViewById(R.id.dialog_inc)
            val adapter = ArrayAdapter(context!!,android.R.layout.simple_spinner_item, incs)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.adapter = adapter
            builder.show()
        }

        listView = view.findViewById(R.id.incs)

        updateListView(incApps)

        listView.setOnItemClickListener { _, _, position, _ ->
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Advertencia")
            builder.setMessage("¿Seguro que deseas eliminar la incidencia?")
            builder.setPositiveButton("Eliminar"){ dialog, _ ->
                deleteIncApp(position)
            }
            builder.setNegativeButton("Cancelar"){ dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
    }

    private fun makeButtons(view: View, start: Date?, end: Date?){
        buttonStart = view.findViewById(R.id.btn_clean_start)
        buttonEnd = view.findViewById(R.id.btn_clean_end)

        buttonStart.isEnabled = (start==null)
        buttonEnd.isEnabled = (start!=null && end==null)


        buttonStart.setOnClickListener {
            if(!mainActivity.clean.isValid())
                Utils.alert(context!!, "Debes rellenar todos los campos")
            else
                saveStart()
        }

        buttonEnd.setOnClickListener {
            saveEnd()
        }
    }

    private fun updateListView(incApps: MutableList<IncApp>) {
        val listItems: MutableList<String> = ArrayList()
        for (incApp: IncApp in incApps) {
            val text = "${incApp.name} / Minutos:${incApp.minutes}"
            listItems.add(text)
        }

        val adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, listItems)
        listView.adapter = adapter
    }

    private fun saveStart(){
        mainActivity.clean.start = Date()

        val prefs = Prefs(mainActivity)
        val url: String = prefs.settingsUrl
        val center: Int = prefs.settingsCenter

        val params = HashMap<String,String>()
        params["action"]="add-clean"
        params["center"]=center.toString()
        params["id_device"]=mainActivity.idApp.toString()
        params["conf"]=mainActivity.clean.conf
        params["name"]=mainActivity.clean.confName
        params["operators"]=mainActivity.clean.operators.toString()
        params["obs"]=mainActivity.clean.obs
        params["start"]=Utils.dateToString(mainActivity.clean.start!!, "yyyy-MM-dd HH:mm:ss")
        if(mainActivity.clean.end!=null)
            params["end"]=Utils.dateToString(mainActivity.clean.end!!, "yyyy-MM-dd HH:mm:ss")
        else
            params["end"]=""

        if(url.isNotEmpty()){
            val dialog = Utils.modalAlert(mainActivity, "Guardando")
            dialog.show()
            Router.post(
                context = context!!,
                url = url,
                params = params,
                responseListener = { response ->
                    if (context != null){
                        if (response.toLongOrNull()!=null)
                            saveStartOk(response.toLong())
                        else
                            saveStartError(response)
                        dialog.dismiss()
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

    private fun saveStartOk(id: Long){
        buttonStart.isEnabled = false
        buttonEnd.isEnabled = true

        mainActivity.clean.id = id

        mainActivity.clean.position = mainActivity.listClean.size
        mainActivity.listClean.add(mainActivity.clean)
        (mainActivity.currentAdapter as CleanAdapter).update()
        mainActivity.cleanFragment()
    }

    private fun saveStartError(response: String){
        Utils.alert(context!!, "Error: $response")
        mainActivity.clean.start = null
    }

    private fun saveEnd(){
        mainActivity.clean.end = Date()

        val prefs = Prefs(mainActivity)
        val url: String = prefs.settingsUrl

        val params = HashMap<String,String>()
        params["action"]="end-clean"
        params["id"]=mainActivity.clean.id.toString()
        params["obs"]=mainActivity.clean.obs

        if(url.isNotEmpty()){
            val dialog = Utils.modalAlert(mainActivity, "Guardando")
            dialog.show()
            Router.post(
                context = context!!,
                url = url,
                params = params,
                responseListener = { response ->
                    if (context != null){
                        if (response=="ok")
                            saveEndOk()
                        else
                            saveEndError(response)
                        dialog.dismiss()
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

    private fun saveEndOk(){
        buttonEnd.isEnabled = false
        (mainActivity.currentAdapter as CleanAdapter).update()
        mainActivity.cleanFragment()
    }

    private fun saveEndError(response: String){
        Utils.alert(context!!, "Error: $response")
        mainActivity.listClean[mainActivity.clean.position].end = null
    }

    private fun addIncApp(inc: Inc, minutes: Int){
        val incApp = IncApp(0, inc.code, minutes, inc.name)
        mainActivity.clean.incApps!!.add(incApp)
        updateListView(mainActivity.clean.incApps!!)
    }

    private fun deleteIncApp(position: Int){
        mainActivity.clean.incApps!!.removeAt(position)
        updateListView(mainActivity.clean.incApps!!)
    }

}