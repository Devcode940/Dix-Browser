package com.devcode940.web.page.browser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.devcode940.web.EasyApplication
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.common.TabConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.ITab
import com.devcode940.web.contract.IWebView
import com.devcode940.web.entity.bo.ClickInfo
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.History
import com.devcode940.web.page.address.AddressDialog
import com.devcode940.web.page.history.HistoryActivity
import com.devcode940.web.page.setting.SettingDialogKt
import com.devcode940.web.page.tabpreview.TabDialogKt
import com.devcode940.web.ui.address.AddressBarManager
import com.devcode940.web.ui.address.AddressBarView
import com.devcode940.web.ui.download.BrowserDownloadManager
import com.devcode940.web.ui.pip.PictureInPictureManager
import com.devcode940.web.ui.session.SessionManager
import com.devcode940.web.ui.theme.ThemeManager
import com.devcode940.web.utils.FragmentBackHandleHelper
import com.devcode940.web.utils.TabHelper
import com.devcode940.web.BackgroundTabManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BrowserActivity : AppCompatActivity(), IWebView.OnWebInteractListener, IBrowser {

    private var webContentFrame: FrameLayout? = null
    private var addressBarView: AddressBarView? = null
    private var downloadManager: BrowserDownloadManager? = null
    private var pipManager: PictureInPictureManager? = null

    private var navController: IBrowser.INavController? = null
    private var historyController: IBrowser.IHistoryController? = null
    private var tabController: IBrowser.ITabController? = null
    private var bookmarkController: IBrowser.IBookmarkController? = null
    private var downloadController: IBrowser.IDownloadController? = null
    private var stubComponent: IBrowser.IComponent? = null

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val historyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let(::handleHistoryResult)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser_modern)

        getTabController()

        webContentFrame = findViewById(R.id.web_content_frame)

        addressBarView = findViewById(R.id.address_bar)
        setupAddressBar()
        setupNavigationButtons()

        downloadManager = BrowserDownloadManager(this)
        pipManager = PictureInPictureManager(this)

        applyThemeFromPreferences()
        restorePreviousSession()

        if (savedInstanceState == null) {
            val tabInfo = TabInfo.create(
                System.currentTimeMillis().toString(),
                getString(R.string.new_tab_welcome)
            )
            getTabController().onTabCreate(tabInfo, false)
        } else {
            val prevDialog = supportFragmentManager.findFragmentByTag(TAB_DIALOG_TAG)
            if (prevDialog is TabDialogKt) {
                prevDialog.setTabViewSubject(getTabController())
                prevDialog.dismiss()
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (!FragmentBackHandleHelper.isFragmentBackHandled(supportFragmentManager)) {
                finish()
            }
        }
    }

    private fun setupAddressBar() {
        val bar = addressBarView ?: return
        bar.setOnUrlSubmitListener { url ->
            getTabController().onTabLoadUrl(url)
            bar.setUrl(url)
        }
        bar.setOnClearListener { }
        bar.setSearchEngine(AddressBarManager.SearchEngine.GOOGLE)
    }

    private fun setupNavigationButtons() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { getTabController().onTabGoBack() }
        findViewById<ImageButton>(R.id.btn_forward).setOnClickListener { getTabController().onTabGoForward() }
        findViewById<ImageButton>(R.id.btn_refresh).setOnClickListener { getTabController().onTabRefresh() }
        findViewById<ImageButton>(R.id.btn_menu).setOnClickListener { showSettingDialog() }
    }

    fun onDownloadStart(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String,
        contentLength: Long
    ) {
        downloadManager?.startDownload(url, userAgent, contentDisposition, mimeType, contentLength)
        Toast.makeText(this, getString(R.string.download_started), Toast.LENGTH_SHORT).show()
    }

    private fun applyThemeFromPreferences() {
        val darkModeEnabled = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        if (darkModeEnabled) ThemeManager.applyDarkMode(true) else ThemeManager.applySystemDefault()
    }

    fun toggleDarkMode(enable: Boolean) {
        ThemeManager.applyDarkMode(enable)
        getSharedPreferences("settings", MODE_PRIVATE).edit().putBoolean("dark_mode", enable).apply()
    }

    fun enterPictureInPicture() {
        pipManager?.enterPictureInPictureMode()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        BackgroundTabManager.pauseAll()
    }

    override fun onPageTitleChange(tabInfo: TabInfo) {
        getTabController().updateTabInfo(tabInfo)
        val bar = addressBarView
        val url = tabInfo.url
        if (bar != null && url != null) {
            bar.setUrl(url)
        }
    }

    override fun onLongClick(clickInfo: ClickInfo) {
        when (clickInfo.type) {
            WebView.HitTestResult.IMAGE_TYPE -> showImageActionDialog(clickInfo)
            WebView.HitTestResult.SRC_ANCHOR_TYPE,
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> showUrlActionDialog(clickInfo)
            else -> {}
        }
    }

    private fun handleHistoryResult(data: Intent) {
        @Suppress("DEPRECATION")
        val info = data.getParcelableExtra(TAB_INFO_KEY) as TabInfo?
        if (info != null) {
            getTabController().onTabCreate(info, false)
        }
    }

    private fun showTabDialog() {
        val prev = supportFragmentManager.findFragmentByTag(TAB_DIALOG_TAG)
        if (prev is TabDialogKt) {
            prev.dismiss()
            return
        }
        val tabDialog = TabDialogKt().apply {
            isCancelable = false
            setTabViewSubject(getTabController())
        }
        tabDialog.show(supportFragmentManager, TAB_DIALOG_TAG)
    }

    private fun showSettingDialog() {
        val prev = supportFragmentManager.findFragmentByTag(SETTING_DIALOG_TAG)
        if (prev is SettingDialogKt) {
            prev.dismiss()
            return
        }
        val settingDialog = SettingDialogKt().apply { isCancelable = false }
        settingDialog.show(supportFragmentManager, SETTING_DIALOG_TAG)
    }

    private fun showAddressDialog(currentUrl: String) {
        val prev = supportFragmentManager.findFragmentByTag(ADDRESS_DIALOG_TAG)
        if (prev is AddressDialog) {
            prev.dismiss()
            return
        }
        AddressDialog().apply {
            setCurrentUrl(currentUrl)
            show(supportFragmentManager, ADDRESS_DIALOG_TAG)
        }
    }

    private fun showImageActionDialog(clickInfo: ClickInfo) {
        AlertDialog.Builder(this)
            .setItems(R.array.image_actions) { _, which ->
                val backstage = which == TabConst.TAB_OPEN_ACTION_BACKSTAGE
                TabHelper.createTab(this, R.string.new_tab_welcome, clickInfo.url, backstage)
            }
            .show()
    }

    private fun showUrlActionDialog(clickInfo: ClickInfo) {
        AlertDialog.Builder(this)
            .setItems(R.array.url_actions) { _, which ->
                val backstage = which == TabConst.TAB_OPEN_ACTION_BACKSTAGE
                TabHelper.createTab(this, R.string.new_tab_welcome, clickInfo.url, backstage)
            }
            .show()
    }

    @UiThread
    override fun provideBrowserComponent(componentName: String): IBrowser.IComponent {
        synchronized(this) {
            if (TextUtils.isEmpty(componentName)) return getStubComponent()
            return when (componentName) {
                BrowserConst.BOOKMARK_COMPONENT -> getBookmarkController()
                BrowserConst.DOWNLOAD_COMPONENT -> getDownloadController()
                BrowserConst.HISTORY_COMPONENT -> getHistoryController()
                BrowserConst.NAVIGATION_COMPONENT -> getNavController()
                BrowserConst.TAB_COMPONENT -> getTabController()
                else -> getStubComponent()
            }
        }
    }

    private fun getStubComponent(): IBrowser.IComponent =
        stubComponent ?: StubBrowserComponent().also { stubComponent = it }

    private fun getBookmarkController(): IBrowser.IBookmarkController =
        bookmarkController ?: StubBookmarkController().also { bookmarkController = it }

    private fun getDownloadController(): IBrowser.IDownloadController =
        downloadController ?: StubDownloadController().also { downloadController = it }

    private fun getHistoryController(): IBrowser.IHistoryController =
        historyController ?: EasyHistoryController().also { historyController = it }

    private fun getNavController(): IBrowser.INavController =
        navController ?: EasyNavController().also { navController = it }

    private fun getTabController(): IBrowser.ITabController =
        tabController
            ?: TabCacheManager(this, supportFragmentManager, 3, R.id.web_content_frame)
                .also { tabController = it }

    @Deprecated("Deprecated in Java")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.isEmpty) return
        @Suppress("DEPRECATION")
        val restoreList: ArrayList<TabInfo>? = savedInstanceState.getParcelableArrayList(TAB_LIST_KEY)
        if (restoreList != null) {
            getTabController().provideInfoList().addAll(restoreList)
        }
        val restored = supportFragmentManager.fragments
        for (target in restored) {
            val args = target.arguments ?: continue
            if (target is ITab) {
                val info = TabInfo.create(
                    args.getString(TabConst.ARG_TAG),
                    args.getString(TabConst.ARG_TITLE)
                )
                getTabController().onRestoreTabCache(info, target)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(TAB_LIST_KEY, ArrayList(getTabController().provideInfoList()))
    }

    override fun onDestroy() {
        super.onDestroy()
        SessionManager.saveTabs(
            this,
            getTabController().provideInfoList().mapNotNull { tab ->
                tab.uri?.toString()?.let { url -> SessionManager.createSavedTab(tab.title ?: "", url) }
            }
        )
        ioScope.cancel()
        tabController?.let {
            it.onCloseAllTabs()
            it.detach()
            it.onDestroy()
        }
        tabController = null
    }

    private fun restorePreviousSession() {
        val saved = SessionManager.getSavedTabs(this)
        if (saved.isEmpty()) return
        val controller = getTabController()
        for (tab in saved) {
            val url = tab.url
            if (url.isNullOrBlank()) continue
            val info = TabInfo.create(System.currentTimeMillis().toString(), tab.title, Uri.parse(url))
            controller.onTabCreate(info, true)
        }
    }

    inner class EasyNavController : IBrowser.INavController {
        override fun goBack() {
            onBackPressedDispatcher.onBackPressed()
        }

        override fun goForward() {
            getTabController().onTabGoForward()
        }

        override fun goHome() {
            getTabController().onTabGoHome()
        }

        override fun showTabs() {
            showTabDialog()
        }

        override fun showAddress(currentUrl: String) {
            showAddressDialog(currentUrl)
        }

        override fun showSetting() {
            showSettingDialog()
        }

        override fun showHistory() {
            historyLauncher.launch(Intent(this@BrowserActivity, HistoryActivity::class.java))
        }
    }

    inner class EasyHistoryController : IBrowser.IHistoryController {
        override fun addHistory(entity: History) {
            ioScope.launch {
                try {
                    val app = applicationContext as EasyApplication
                    val db: AppDatabase = app.getAppDatabase()
                    val rowId = db.historyDao().insertHistory(entity)
                    Log.i(TAG, "history insert id=$rowId title=${entity.title} url=${entity.url}")
                } catch (e: Exception) {
                    Log.e(TAG, "history insert failed", e)
                }
            }
        }
    }

    class StubDownloadController : IBrowser.IDownloadController
    class StubBookmarkController : IBrowser.IBookmarkController
    class StubBrowserComponent : IBrowser.IComponent

    companion object {
        private const val TAG = "BrowserActivity"
        private const val SETTING_DIALOG_TAG = "setting_dialog"
        private const val TAB_DIALOG_TAG = "tab_dialog"
        private const val ADDRESS_DIALOG_TAG = "address_dialog"
        private const val TAB_INFO_KEY = "tabInfo"
        private const val TAB_LIST_KEY = "tablist"
    }
}
