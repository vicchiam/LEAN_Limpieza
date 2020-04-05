package com.pcs.lean_limpieza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.fragment.CleanFragment
import com.pcs.lean_limpieza.models.Clean
import com.pcs.lean_limpieza.tools.Utils

class CleanAdapter: RecyclerView.Adapter<CleanAdapter.ViewHolder>(), Filterable {

    private lateinit var cleanFragment: CleanFragment

    private lateinit var originalList: List<Clean>
    private lateinit var showList: MutableList<Clean>

    private var lastSearch: String? = ""

    fun cleanAdapter(downloadFragment: CleanFragment, list: MutableList<Clean>){
        this.cleanFragment = downloadFragment
        this.originalList = list
        this.showList = ArrayList(list)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Clean = this.showList[position]
        holder.bind(item, cleanFragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_clean, parent, false))
    }

    override fun getItemCount(): Int {
        return showList.size
    }

    private var onNothingFound: (() -> Unit)? = null

    fun search(s: String?, onNothingFound: (() -> Unit)?) {
        lastSearch = s
        this.onNothingFound = onNothingFound
        filter.filter(s)
    }

    fun update(){
        search(lastSearch){}
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            private val filterResults = FilterResults()

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                showList.clear()
                if (constraint.isNullOrBlank()) {
                    showList.addAll(originalList)
                } else {
                    val searchResults = originalList.filter {
                        it.getSearchCriteria().contains(constraint)
                    }
                    showList.addAll(searchResults)
                }
                return filterResults.also {
                    it.values = showList
                }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (showList.isNullOrEmpty())
                    onNothingFound?.invoke()
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val relativeLayout: RelativeLayout = view.findViewById(R.id.item_download)

        private val textConf: TextView =  view.findViewById(R.id.item_clean_conf)
        private val textStart: TextView =  view.findViewById(R.id.item_clean_start)
        private val textEnd: TextView =  view.findViewById(R.id.item_clean_end)
        private val textOperators: TextView =  view.findViewById(R.id.item_clean_operators)

        fun bind(clean: Clean, cleanFragment: CleanFragment){
            val confText = "${clean.conf} - ${clean.confName}"
            textConf.text = confText
            textStart.text = Utils.dateToString(clean.start!!,"dd/MM/yyyy HH:mm")
            if(clean.end!=null)
                textEnd.text = Utils.dateToString(clean.end!!, "dd/MM/yyyy HH:mm")
            else
                textEnd.text = "No Finalizada"
            if(clean.pending)
                relativeLayout.background =
                    cleanFragment.resources.getDrawable(
                        android.R.color.holo_green_light,
                        cleanFragment.context!!.theme
                    )
            else
                relativeLayout.background =
                    cleanFragment.resources.getDrawable(
                        android.R.color.white,
                        cleanFragment.context!!.theme
                    )
            val operatorText = "Operarios: ${clean.operators}"
            textOperators.text = operatorText

            itemView.setOnClickListener {
                cleanFragment.editClean(clean)
            }
        }

    }
}