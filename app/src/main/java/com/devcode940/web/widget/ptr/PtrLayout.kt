package com.devcode940.web.widget.ptr

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class PtrLayout : SwipeRefreshLayout {

    @JvmField
    var pageSize: Int = 20

    @JvmField
    var hasMore: Boolean = false

    var loading: Boolean = false
        private set

    private var mRecyclerView: RecyclerView? = null

    private var loadMoreView: View? = null

    private var mOnLoadListener: OnLoadListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        findRecyclerView()
        super.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            if (!loading && mOnLoadListener != null) {
                mOnLoadListener?.onRefresh()
            }
        })
    }

    override fun setOnRefreshListener(listener: SwipeRefreshLayout.OnRefreshListener?) {
        throw RuntimeException("USE PtrLayout::setOnLoadListener INSTEAD!!!")
    }

    private fun findRecyclerView() {
        if (childCount > 0) {
            val target = getChildAt(0)
            if (target is RecyclerView) {
                mRecyclerView = target
                mRecyclerView?.clearOnScrollListeners()
                mRecyclerView?.addOnScrollListener(PtrOnScrollListener())
            }
        }
    }

    fun getOnLoadListener(): OnLoadListener? = mOnLoadListener

    /**
     * 设置加载监听器
     */
    fun setOnLoadListener(ltn: OnLoadListener?) {
        this.mOnLoadListener = ltn
    }

    fun getLoadMoreView(): View? = loadMoreView

    /**
     * 设置加载更多对应自定义View
     */
    fun setLoadMoreView(loadMoreView: View?) {
        this.loadMoreView = loadMoreView
    }

    fun getPageSize(): Int = pageSize

    fun setPageSize(size: Int) {
        this.pageSize = size
    }

    fun isHasMore(): Boolean = hasMore

    /**
     * 设置是否还有更多数据，仅当有更多数据时，加载回调函数才会生效
     */
    fun setHasMore(more: Boolean) {
        this.hasMore = more
    }

    /**
     * 加载完毕后，设置数据状态
     */
    fun loadFinish(emptyResult: Boolean, more: Boolean) {
        loading = false
        hasMore = more
        loadMoreView?.visibility = View.GONE
    }

    inner class PtrOnScrollListener : RecyclerView.OnScrollListener() {

        private var layoutManager: RecyclerView.LayoutManager? = null
        private var lastVisibleItem = 0

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (recyclerView.adapter == null) {
                lastVisibleItem = -1
                return
            }
            if (!loading && lastVisibleItem + 1 == recyclerView.adapter!!.itemCount) {
                if (loading || !hasMore) {
                    return
                }
                if (mOnLoadListener != null) {
                    mOnLoadListener?.onLoadMore()
                    loadMoreView?.visibility = View.VISIBLE
                    loading = true
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (recyclerView.adapter == null) {
                lastVisibleItem = -1
                return
            }
            if (layoutManager == null) {
                layoutManager = recyclerView.layoutManager
            }

            if (layoutManager is LinearLayoutManager) {
                lastVisibleItem = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            } else if (layoutManager is StaggeredGridLayoutManager) {
                val spanItem = (layoutManager as StaggeredGridLayoutManager)
                    .findLastCompletelyVisibleItemPositions(null)
                lastVisibleItem = spanItem[0]
            }
        }
    }

    /**
     * 列表的加载监听器
     */
    interface OnLoadListener {
        /**
         * 加载更多回调函数
         */
        fun onLoadMore()

        /**
         * 下拉刷新回调函数
         */
        fun onRefresh()
    }
}
