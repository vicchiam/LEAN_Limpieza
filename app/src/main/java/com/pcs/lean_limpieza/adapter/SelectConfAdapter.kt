package com.pcs.lean_limpieza.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pcs.lean_limpieza.R
import com.pcs.lean_limpieza.fragment.SelectConfFragment
import com.pcs.lean_limpieza.models.Conf

class SelectConfAdapter : RecyclerView.Adapter<SelectConfAdapter.ViewHolder>(), Filterable {

    private lateinit var selectConfFragment: SelectConfFragment

    private lateinit var originalList: List<Conf>
    private lateinit var showList: MutableList<Conf>

    fun selectProviderAdapter(selectProviderFragment: SelectConfFragment, list: MutableList<Conf>){
        this.selectConfFragment = selectProviderFragment
        this.originalList = list
        this.showList = ArrayList(list)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: Conf = this.showList[position]
        holder.bind(item, selectConfFragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_conf_select, parent, false))
    }

    override fun getItemCount(): Int {
        return showList.size
    }

    private var onNothingFound: (() -> Unit)? = null

    fun search(s: String?, onNothingFound: (() -> Unit)?) {
        this.onNothingFound = onNothingFound
        filter.filter(s)
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
        private val textCode: TextView =  view.findViewById(R.id.item_select_conf)
        private val textName: TextView =  view.findViewById(R.id.item_select_conf_name)

        fun bind(conf: Conf, selectProviderFragment: SelectConfFragment){
            textCode.text = "${conf.conf}"
            textName.text = conf.confName

            itemView.setOnClickListener {
                selectProviderFragment.selectItem(conf)
            }
        }

    }

}