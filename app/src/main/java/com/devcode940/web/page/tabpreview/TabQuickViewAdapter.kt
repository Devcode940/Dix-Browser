package com.devcode940.web.page.tabpreview

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.ITabQuickView
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.utils.StringUtils

class TabQuickViewAdapter(private var context: Context?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), ITabQuickView.Observer {

    private var tabLruCache: ITabQuickView.Subject? = null
    var listener: OnTabClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_ADD) {
            val itemView = LayoutInflater.from(context).inflate(R.layout.layout_add_tab_item, parent, false)
            TabAddViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(context).inflate(R.layout.layout_tab_item, parent, false)
            TabQuickViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (tabLruCache == null || tabLruCache?.provideInfoList() == null) {
            return
        }

        if (holder is TabAddViewHolder) {
            bindAddView(holder)
            return
        }

        if (holder is TabQuickViewHolder) {
            bindQuickView(holder, position)
        }
    }

    private fun bindAddView(holder: TabAddViewHolder) {
        holder.addTabButton.setOnClickListener {
            listener?.onAddTab()
        }
    }

    private fun bindQuickView(holder: TabQuickViewHolder, position: Int) {
        val cache = tabLruCache ?: return
        val info = cache.provideInfoList()[position]

        val ctx = context
        if (ctx is IBrowser) {
            val tabController =
                ctx.provideBrowserComponent(BrowserConst.TAB_COMPONENT) as IBrowser.ITabController
            val currentTab = tabController.getCurrentTab()
            if (info == currentTab) {
                holder.indicator.visibility = View.VISIBLE
            } else {
                holder.indicator.visibility = View.INVISIBLE
            }
            val bitmap = tabController.getPreviewForTab(info)
            holder.preview.setImageBitmap(bitmap)
        }

        holder.siteTitle.text = info.title

        holder.closeButton.setOnClickListener {
            if (tabLruCache?.provideInfoList() == null) {
                return@setOnClickListener
            }
            if (!StringUtils.isEmpty(info.tag)) {
                listener?.onTabClose(info)
            }
        }

        holder.itemView.setOnClickListener {
            listener?.onTabClick(info)
        }
    }

    fun attachToSubject(target: ITabQuickView.Subject) {
        tabLruCache = target
        tabLruCache?.attach(this)
    }

    fun detachSubject() {
        tabLruCache?.detach()
        tabLruCache = null
        context = null
    }

    override fun updateQuickView() {
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        val cache = tabLruCache ?: return 1
        return cache.provideInfoList().size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (itemCount == 1) {
            return VIEW_ADD
        }
        return if (position < itemCount - 1) {
            VIEW_TAB
        } else {
            VIEW_ADD
        }
    }

    class TabQuickViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val siteTitle: TextView = itemView.findViewById(R.id.item_title)
        val closeButton: ImageView = itemView.findViewById(R.id.item_close_button)
        val indicator: View = itemView.findViewById(R.id.tab_indicator)
        val preview: ImageView = itemView.findViewById(R.id.tab_preview)
    }

    class TabAddViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addTabButton: ImageView = itemView.findViewById(R.id.add_tab_button)
    }

    interface OnTabClickListener {
        fun onTabClick(tag: TabInfo)

        fun onTabClose(tag: TabInfo)

        fun onAddTab()
    }

    companion object {
        private const val VIEW_ADD = 100
        private const val VIEW_TAB = 101
    }
}
