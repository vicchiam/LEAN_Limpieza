package com.pcs.lean_limpieza.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonSyntaxException
import com.pcs.lean_limpieza.MainActivity
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.adapter.SelectConfAdapter
import com.pcs.lean_limpieza.models.Conf
import com.pcs.lean_limpieza.tools.Prefs
import com.pcs.lean_limpieza.tools.Router
import com.pcs.lean_limpieza.tools.Utils
import java.util.*

class SelectConfFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    private val adapter: SelectConfAdapter = SelectConfAdapter()

    private lateinit var searchView: SearchView
    private lateinit var recycler: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_conf, container, false)

        val imageButton: ImageButton = view.findViewById(R.id.btn_back_select_provider)
        imageButton.setOnClickListener {
            mainActivity.navigateToNewClean()
        }

        searchView = view.findViewById(R.id.search_provider)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.search(query){}
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.search(newText){}
                return true
            }
        })

        recycler = view.findViewById(R.id.recycler_conf)
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(context)

        if(mainActivity.cache.get("confs")==null) {
            getConfs()
        }
        else {
            adapter.selectConfAdapter(this, (mainActivity.cache.get("confs") as MutableList<Conf>))
            recycler.adapter = adapter
        }

        return view
    }

    private fun getConfs(){
        val prefs = Prefs(mainActivity)
        val url: String = prefs.settingsUrl
        val center: Int = prefs.settingsCenter

        val today = Utils.dateToString(Date(),"yyyy-MM-dd")

        if(url.isNotEmpty()){
            val dialog: AlertDialog = Utils.modalAlert(mainActivity)
            dialog.show()
            Router.get(
                context = context!!,
                url = url,
                params = "action=get-confs",
                responseListener = { response ->
                    if(context!=null) {
                        try{
                            val list: List<Conf> = Utils.fromJson(response)
                            val mutableList = list.toMutableList()
                            mainActivity.cache.set("confs", mutableList)
                            adapter.selectConfAdapter(this, mutableList)
                            recycler.adapter = adapter
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

    fun selectItem(conf: Conf){
        Utils.closeKeyboard(context!!, mainActivity)
        mainActivity.clean.conf = conf.conf
        mainActivity.clean.confName = conf.confName
        mainActivity.navigateToNewClean()
    }

}