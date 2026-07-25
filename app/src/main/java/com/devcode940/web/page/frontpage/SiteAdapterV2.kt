package com.devcode940.web.page.frontpage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.EasyApplication
import com.devcode940.web.R
import com.devcode940.web.entity.dao.WebSite
import com.devcode940.web.utils.StringUtils
import com.devcode940.web.widget.WebSiteLogo

class SiteAdapterV2(private val context: Context) :
    RecyclerView.Adapter<SiteAdapterV2.SiteViewHolder>() {

    private val dataList: MutableList<WebSite> = ArrayList()

    private var listener: OnSiteItemClickListener? = null

    fun getDataList(): List<WebSite> = dataList

    fun clearDataList() {
        dataList.clear()
    }

    fun appendDataList(list: List<WebSite>?) {
        dataList.addAll(list ?: emptyList())
    }

    fun getListener(): OnSiteItemClickListener? = listener

    fun setListener(listener: OnSiteItemClickListener?) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_grid_item, parent, false)
        return SiteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        if (itemCount <= position) {
            return
        }

        val entity = dataList[position]
        holder.title.text = entity.siteName
        val name = entity.siteName
        if (!name.isNullOrEmpty()) {
            holder.icon.setName(name.substring(0, 1))
        } else {
            holder.icon.setName("E")
        }

        holder.itemView.setOnClickListener {
            listener?.onSiteItemClick(entity)
        }
    }

    override fun getItemCount(): Int = dataList.size

    class SiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: WebSiteLogo = itemView.findViewById(R.id.item_image)
        val title: TextView = itemView.findViewById(R.id.item_title)
    }

    interface OnSiteItemClickListener {
        fun onSiteItemClick(webSite: WebSite)
    }

    companion object {
        @JvmStatic
        fun getTestDataList(context: Context?): List<WebSite> {
            if (context == null || context.applicationContext == null) {
                return ArrayList()
            }
            val application = context.applicationContext as EasyApplication
            val db = application.getAppDatabase()
            val dbList = db.webSiteDao().getAll()
            return if (dbList.isEmpty()) ArrayList() else ArrayList(dbList)
        }
    }
}
