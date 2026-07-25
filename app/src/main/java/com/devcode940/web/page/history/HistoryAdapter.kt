package com.devcode940.web.page.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.R
import com.devcode940.web.entity.dao.History

class HistoryAdapter(private val mContext: Context) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val dataList: MutableList<History> = ArrayList()

    private var itemClickListener: OnHistoryItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(mContext).inflate(R.layout.layout_history_item, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        if (dataList.size <= position) {
            return
        }
        val entity = dataList[position] ?: return
        holder.title.text = "${entity.id}. ${entity.title}"
        holder.url.text = entity.url

        holder.itemView.setOnClickListener {
            itemClickListener?.onHistoryItemClick(entity)
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun clearDataList() {
        dataList.clear()
    }

    fun appendDataList(list: List<History>?) {
        dataList.addAll(list ?: emptyList())
    }

    fun setItemClickListener(itemClickListener: OnHistoryItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.history_title)
        val url: TextView = itemView.findViewById(R.id.history_url)
    }

    interface OnHistoryItemClickListener {
        fun onHistoryItemClick(entity: History)
    }
}
