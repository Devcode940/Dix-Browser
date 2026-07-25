package com.devcode940.web.widget.ptr

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class PtrAdapter<ENTITY, HOLDER : RecyclerView.ViewHolder>(
    protected val mContext: Context,
    private var dataList: MutableList<ENTITY>?,
    private val headerCount: Int,
    private val footerCount: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), View.OnClickListener {

    constructor(ctx: Context, datas: List<ENTITY>?) : this(ctx, datas?.toMutableList(), 0, 0)

    private var onListItemClickListener: OnListItemClickListener? = null

    /**
     * 生成列表项的ViewHolder
     */
    abstract fun getViewHolder(ctx: Context, parent: ViewGroup, viewType: Int): HOLDER

    abstract fun getHeaderLayoutId(): Int

    abstract fun getFooterLayoutId(): Int

    /**
     * 填充ViewHolder数据
     */
    abstract fun fillData(holder: HOLDER, datas: List<ENTITY>?, position: Int)

    open fun fillHeader(holder: RecyclerView.ViewHolder) {}

    open fun fillFooter(holder: RecyclerView.ViewHolder) {}

    open fun getHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val header = LayoutInflater.from(mContext).inflate(getHeaderLayoutId(), parent, false)
        return HeaderHolder(header)
    }

    open fun getFooterViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val footer = LayoutInflater.from(mContext).inflate(getFooterLayoutId(), parent, false)
        return FooterHolder(footer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> getHeaderViewHolder(parent)
            VIEW_TYPE_FOOTER -> getFooterViewHolder(parent)
            else -> {
                val holder = getViewHolder(mContext, parent, viewType)
                holder.itemView.setOnClickListener(this)
                holder
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val dataSize: Int = dataList?.size ?: 0

        try {
            if (position in 0 until headerCount) {  // header position
                fillHeader(holder)
            } else if (position >= headerCount && position < dataSize + headerCount) {  // content position
                val realPosition = position - headerCount
                if (dataList != null && realPosition < dataList!!.size) {
                    @Suppress("UNCHECKED_CAST")
                    fillData(holder as HOLDER, dataList, realPosition)
                    holder.itemView.tag = realPosition
                }
            } else if (position >= dataSize + headerCount && footerCount > 0) {  // footer position
                fillFooter(holder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return if (dataList != null) {
            contentSize + headerCount + footerCount
        } else {
            headerCount + footerCount
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (headerCount > 0 && position < headerCount) {
            return VIEW_TYPE_HEADER
        } else if (footerCount > 0 && position > headerCount + contentSize - 1) {
            return VIEW_TYPE_FOOTER
        }
        return VIEW_TYPE_CONTENT
    }

    override fun onClick(v: View) {
        if (onListItemClickListener != null) {
            try {
                val position = v.tag as Int
                onListItemClickListener?.onItemClick(v, position)
            } catch (e: Exception) {
            }
        }
    }

    fun getAdapterContext(): Context = mContext

    fun getDataList(): List<ENTITY>? = dataList

    fun setDataList(list: List<ENTITY>?) {
        this.dataList = list?.toMutableList()
    }

    /**
     * 在已有列表后添加数据
     */
    fun addDataList(dataListToAdd: List<ENTITY>?) {
        val list = this.dataList ?: ArrayList<ENTITY>().also { this.dataList = it }
        list.addAll(dataListToAdd ?: emptyList())
    }

    /**
     * 清空数据列表
     */
    fun clearDataList() {
        val list = this.dataList
        if (list == null) {
            this.dataList = ArrayList()
            return
        }
        list.clear()
    }

    val contentSize: Int
        get() {
            if (this.dataList == null) {
                this.dataList = ArrayList()
            }
            return dataList!!.size
        }

    fun getOnListItemClickListener(): OnListItemClickListener? = onListItemClickListener

    fun setOnListItemClickListener(onListItemClickListener: OnListItemClickListener?) {
        this.onListItemClickListener = onListItemClickListener
    }

    /**
     * 列表项点击监听器
     */
    interface OnListItemClickListener {
        fun onItemClick(itemView: View, position: Int)
    }

    class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val VIEW_TYPE_HEADER = 1000
        const val VIEW_TYPE_CONTENT = 1001
        const val VIEW_TYPE_FOOTER = 1002
    }
}
