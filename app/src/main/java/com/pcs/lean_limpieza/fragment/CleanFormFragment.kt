package com.pcs.lean_limpieza.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.pcs.lean_limpieza.MainActivity
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.adapter.CleanAdapter
import com.pcs.lean_limpieza.models.Clean
import com.pcs.lean_limpieza.tools.Prefs
import com.pcs.lean_limpieza.tools.Router
import com.pcs.lean_limpieza.tools.Utils
import java.util.*

class CleanFormFragment: Fragment() {

    private lateinit var mainActivity: MainActivity

    private lateinit var buttonStart : MaterialButton
    private lateinit var buttonEnd : MaterialButton

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
        makeButtons(view, clean.start, clean.end)

        return view
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

}