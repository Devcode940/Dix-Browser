package com.devcode940.web.page.history

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devcode940.web.R
import com.devcode940.web.common.Const
import com.devcode940.web.contract.IHistory
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.History
import com.devcode940.web.widget.ptr.PtrLayout

class HistoryFragment : Fragment(), IHistory.View {

    private var swipeRefreshLayout: PtrLayout? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: HistoryAdapter? = null

    private var presenter: IHistory.Presenter? = null

    private var pageNo = 1
    private var pageSize = Const.PAGE_SIZE_20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.layout_common_list, container, false)

        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout)
        // TODO 实现分页加载
        swipeRefreshLayout?.setHasMore(true)
        swipeRefreshLayout?.setOnLoadListener(object : PtrLayout.OnLoadListener {
            override fun onLoadMore() {
                pageNo++
                loadData(pageNo, pageSize)
            }

            override fun onRefresh() {
                adapter?.clearDataList()
                adapter?.notifyDataSetChanged()
                pageNo = 1
                loadData(pageNo, pageSize)
            }
        })
        recyclerView = rootView.findViewById(R.id.content_list)

        adapter = HistoryAdapter(requireContext())
        adapter?.setItemClickListener(object : HistoryAdapter.OnHistoryItemClickListener {
            override fun onHistoryItemClick(entity: History) {
                var uri: Uri? = null
                try {
                    uri = Uri.parse(entity.url)
                } catch (e: Exception) {
                    uri = null
                }
                if (uri == null) {
                    return
                }
                val info = TabInfo.create(
                    System.currentTimeMillis().toString(),
                    entity.title,
                    uri
                )
                val resultData = Intent()
                resultData.putExtra(Const.Key.TAB_INFO, info)

                activity?.let { act ->
                    act.setResult(Activity.RESULT_OK, resultData)
                    act.finish()
                }
            }
        })
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter
        context?.let {
            recyclerView?.addItemDecoration(DividerItemDecoration(it, LinearLayoutManager.VERTICAL))
        }

        presenter = HistoryPresenterImpl(context, this)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        loadData(pageNo, pageSize)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun showHistory(result: List<History>?) {
        swipeRefreshLayout?.isRefreshing = false
        if (result == null || result.size < pageSize) {
            swipeRefreshLayout?.loadFinish(false, false)
        } else {
            swipeRefreshLayout?.loadFinish(false, true)
        }
        adapter?.appendDataList(result)
        adapter?.notifyDataSetChanged()
    }

    override fun showEmptyResult() {}

    private fun loadData(_pageNo: Int, _pageSize: Int) {
        presenter?.let {
            it.getHistory(_pageNo, _pageSize)
        }
    }
}
