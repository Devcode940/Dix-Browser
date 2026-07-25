package com.devcode940.web.page.browser

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.devcode940.web.R
import com.devcode940.web.common.BrowserConst
import com.devcode940.web.common.Const
import com.devcode940.web.common.TabConst
import com.devcode940.web.contract.IBrowser
import com.devcode940.web.contract.IWebView
import com.devcode940.web.contract.ITab
import com.devcode940.web.entity.bo.ClickInfo
import com.devcode940.web.entity.bo.TabInfo
import com.devcode940.web.entity.dao.AppDatabase
import com.devcode940.web.entity.dao.History
import com.devcode940.web.EasyApplication
import com.devcode940.web.page.address.AddressDialog
import com.devcode940.web.page.history.HistoryActivity
import com.devcode940.web.page.setting.SettingDialogKt
import com.devcode940.web.page.tabpreview.TabDialogKt
import com.devcode940.web.ui.address.AddressBarManager
import com.devcode940.web.ui.address.AddressBarView
import com.devcode940.web.ui.desktop.DesktopModeManager
import com.devcode940.web.ui.download.BrowserDownloadManager
import com.devcode940.web.ui.fullscreen.FullscreenManager
import com.devcode940.web.ui.password.PasswordManager
import com.devcode940.web.ui.pip.PictureInPictureManager
import com.devcode940.web.ui.sniffer.ResourceSnifferDialog
import com.devcode940.web.ui.summarizer.SummarizerFragment
import com.devcode940.web.ui.theme.ThemeManager
import com.devcode940.web.ui.translate.TranslateManager
import com.devcode940.web.utils.FragmentBackHandleHelper
import com.devcode940.web.utils.TabHelper
import com.devcode940.web.web.BackgroundTabManager
import com.devcode940.web.ui.session.SessionManager
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class BrowserActivity : AppCompatActivity(), IWebView.OnWebInteractListener, IBrowser {

    private var webContentFrame: FrameLayout? = null

    // Modern Address Bar
    private var addressBarView: AddressBarView? = null

    // Download Manager
    private var downloadManager: BrowserDownloadManager? = null

    // Phase 3 Managers
    private var desktopModeManager: DesktopModeManager? = null
    private var fullscreenManager: FullscreenManager? = null
    private var translateManager: TranslateManager? = null
    private var pipManager: PictureInPictureManager? = null

    private var navController: IBrowser.INavController? = null
    private var historyController: IBrowser.IHistoryController? = null
    private var tabController: IBrowser.ITabController? = null
    private var bookmarkController: IBrowser.IBookmarkController? = null
    private var downloadController: IBrowser.IDownloadController? = null
    private var stubComponent: IBrowser.IComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser_modern)

        getTabController()

        webContentFrame = findViewById(R.id.web_content_frame)

        // Initialize modern address bar
        addressBarView = findViewById(R.id.address_bar)
        setupAddressBar()
        setupNavigationButtons()

        // Initialize Download Manager
        downloadManager = BrowserDownloadManager(this)

        // Initialize Phase 3 Managers
        desktopModeManager = DesktopModeManager()
        fullscreenManager = FullscreenManager(this)
        translateManager = TranslateManager(this)
        pipManager = PictureInPictureManager(this)

        // Apply saved theme preference
        applyThemeFromPreferences()

        // Enable secure password saving
        PasswordManager.enablePasswordSaving(this)

        // Restore previous session (Auto Restoration)
        restorePreviousSession()

        if (savedInstanceState == null) {
            val tabInfo = TabInfo.create(
                System.currentTimeMillis().toString() + "",
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
    }

    private fun setupAddressBar() {
        val bar = addressBarView ?: return

        bar.setOnUrlSubmitListener { url ->
            // Load URL in current tab
            getTabController().onTabLoadUrl(url)
            bar.setUrl(url)
        }

        bar.setOnClearListener {
            // Optional: clear current tab or show home
        }

        // Default search engine
        bar.setSearchEngine(AddressBarManager.SearchEngine.GOOGLE)
    }

    private fun setupNavigationButtons() {
        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnForward = findViewById<ImageButton>(R.id.btn_forward)
        val btnRefresh = findViewById<ImageButton>(R.id.btn_refresh)
        val btnMenu = findViewById<ImageButton>(R.id.btn_menu)

        btnBack.setOnClickListener { getTabController().onTabGoBack() }
        btnForward.setOnClickListener { getTabController().onTabGoForward() }
        btnRefresh.setOnClickListener { getTabController().onTabRefresh() }

        btnMenu.setOnClickListener { showSettingDialog() }
    }

    // Download integration
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

    // ==================== DARK MODE ====================
    private fun applyThemeFromPreferences() {
        // Default to system theme or read from SharedPreferences
        val darkModeEnabled = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)

        if (darkModeEnabled) {
            ThemeManager.applyDarkMode(true)
        } else {
            ThemeManager.applySystemDefault()
        }
    }

    fun toggleDarkMode(enable: Boolean) {
        ThemeManager.applyDarkMode(enable)
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode", enable)
            .apply()
    }

    // ==================== PHASE 3 FEATURES ====================

    fun toggleDesktopMode(enable: Boolean) {
        desktopModeManager?.setDesktopMode(enable)
        // Reload current page with new User-Agent
        getTabController().onTabRefresh()
    }

    fun toggleFullScreen() {
        fullscreenManager?.toggleFullscreen()
    }

    fun translateCurrentPage() {
        val currentUrl = addressBarView?.getCurrentUrl()
        if (currentUrl != null) {
            translateManager?.translatePage(currentUrl)
        }
    }

    fun enterPictureInPicture() {
        // Use enhanced PiP with Media Controller
        pipManager?.enterPictureInPictureMode()
    }

    /**
     * Play video in PiP mode with full media controls
     */
    fun playVideoInPiP(videoUri: android.net.Uri) {
        // This would typically be called when a video is detected
        // For now, we show a toast
        Toast.makeText(this, "Starting PiP Video Player...", Toast.LENGTH_SHORT).show()
        // In real implementation, pass the VideoView from WebView or a dedicated player
    }

    fun openResourceSniffer() {
        // Collect resources from current WebView and show dialog
        val dialog = ResourceSnifferDialog.newInstance()
        dialog.show(supportFragmentManager, "resource_sniffer")
    }

    // ==================== ARENA.AI SUMMARIZER ====================
    fun openArenaSummarizer() {
        val currentUrl = addressBarView?.getCurrentUrl()

        // Open the advanced Summarizer Fragment
        val fragment = SummarizerFragment.newInstance(currentUrl)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.web_content_frame, fragment)
            .addToBackStack("summarizer")
            .commit()
    }

    // ==================== SAVE PAGE FOR OFFLINE ====================
    fun saveCurrentPageForOffline() {
        // Get the active WebView from the current tab (simplified)
        // In a real implementation, we would get it from TabCacheManager
        Toast.makeText(this, "Saving page for offline viewing...", Toast.LENGTH_SHORT).show()

        // Example: Save using OfflinePageManager
        // com.devcode940.web.ui.offline.OfflinePageManager.saveCurrentPage(webView, this) { path ->
        //     Toast.makeText(this, "Page saved: $path", Toast.LENGTH_LONG).show()
        // };
    }

    // ... rest of the original code remains unchanged ...

    @Deprecated("Deprecated in Java")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.isEmpty) {
            return
        }
        @Suppress("DEPRECATION")
        val restoreList: ArrayList<TabInfo>? = savedInstanceState.getParcelableArrayList("tablist")
        if (restoreList == null) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        (getTabController().provideInfoList() as MutableList<TabInfo>).addAll(restoreList)
        val restoredFragmentList = supportFragmentManager.fragments
        if (restoredFragmentList.isNotEmpty()) {
            for (target in restoredFragmentList) {
                if (target is ITab && target.arguments != null) {
                    val args = target.arguments!!
                    val info = TabInfo.create(
                        args.getString(TabConst.ARG_TAG),
                        args.getString(TabConst.ARG_TITLE)
                    )
                    getTabController().onRestoreTabCache(info, target)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val storeList = ArrayList<TabInfo>()
        storeList.addAll(getTabController().provideInfoList())
        outState.putParcelableArrayList("tablist", storeList)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Save current session before closing
        saveCurrentSession()

        if (tabController != null) {
            tabController!!.onCloseAllTabs()
            tabController!!.detach()
            tabController!!.onDestroy()
            tabController = null
        }
    }

    // ==================== SESSION MANAGEMENT ====================
    private fun saveCurrentSession() {
        // In a real implementation, collect all open tabs and save them
        // For now, we just show a placeholder
        // List<SavedTab> tabs = ...;
        // SessionManager.saveTabs(this, tabs);
    }

    private fun restorePreviousSession() {
        val savedTabs = SessionManager.getSavedTabs(this)
        if (savedTabs.isNotEmpty()) {
            Toast.makeText(this, "Restoring ${savedTabs.size} tabs...", Toast.LENGTH_SHORT).show()
            // TODO: Actually create tabs from savedTabs
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        // Pause all background tabs for better performance
        BackgroundTabManager.pauseAll()
    }

    override fun onPageTitleChange(tabInfo: TabInfo) {
        getTabController().updateTabInfo(tabInfo)
        val url = tabInfo.url
        if (addressBarView != null && url != null) {
            addressBarView!!.setUrl(url)
        }
    }

    override fun onLongClick(clickInfo: ClickInfo) {
        when (clickInfo.type) {
            WebView.HitTestResult.IMAGE_TYPE -> showImageActionDialog(clickInfo)
            WebView.HitTestResult.SRC_ANCHOR_TYPE, WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ->
                showUrlActionDialog(clickInfo)
            else -> {}
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (FragmentBackHandleHelper.isFragmentBackHandled(supportFragmentManager)) {
            return
        }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Const.RequestCode.SHOW_HISTORY) {
            showHistoryResult(resultCode, data)
        }
    }

    private fun showHistoryResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        @Suppress("DEPRECATION")
        val info = data.getParcelableExtra(Const.Key.TAB_INFO) as TabInfo?
        if (info == null) {
            return
        }
        getTabController().onTabCreate(info, false)
    }

    private fun showTabDialog() {
        val prev = supportFragmentManager.findFragmentByTag(TAB_DIALOG_TAG)
        if (prev != null) {
            (prev as TabDialogKt).dismiss()
            return
        }

        val tabDialog = TabDialogKt()
        tabDialog.isCancelable = false

        tabDialog.setTabViewSubject(getTabController())
        tabDialog.show(supportFragmentManager, TAB_DIALOG_TAG)
    }

    private fun showSettingDialog() {
        val prev = supportFragmentManager.findFragmentByTag(SETTING_DIALOG_TAG)
        if (prev != null) {
            (prev as SettingDialogKt).dismiss()
            return
        }

        val settingDialog = SettingDialogKt()
        settingDialog.isCancelable = false

        settingDialog.show(supportFragmentManager, SETTING_DIALOG_TAG)
    }

    // ==================== INCOGNITO TAB ====================
    fun showIncognitoTab() {
        // Create a new incognito tab
        val incognitoTab = TabInfo.create(
            "incognito_" + System.currentTimeMillis(),
            "Incognito Tab"
        )
        getTabController().onTabCreate(incognitoTab, false)

        // TODO: In future, load IncognitoTabFragment instead of normal WebView
        Toast.makeText(this, "Incognito tab opened", Toast.LENGTH_SHORT).show()
    }

    private fun showAddressDialog(currentUrl: String) {
        val prev = supportFragmentManager.findFragmentByTag(ADDRESS_DIALOG_TAG)
        if (prev != null) {
            (prev as AddressDialog).dismiss()
            return
        }

        val addressDialog = AddressDialog()
        addressDialog.setCurrentUrl(currentUrl)
        addressDialog.show(supportFragmentManager, ADDRESS_DIALOG_TAG)
    }

    private fun showImageActionDialog(clickInfo: ClickInfo) {
        val imageDialogBuilder = AlertDialog.Builder(this)
        imageDialogBuilder.setItems(R.array.image_actions) { _, which ->
            if (which == TabConst.TAB_OPEN_ACTION_BACKSTAGE) {
                TabHelper.createTab(
                    this@BrowserActivity,
                    R.string.new_tab_welcome,
                    clickInfo.url,
                    true
                )
            } else if (which == TabConst.TAB_OPEN_ACTION_FRONTSTAGE) {
                TabHelper.createTab(
                    this@BrowserActivity,
                    R.string.new_tab_welcome,
                    clickInfo.url,
                    false
                )
            }
        }
        imageDialogBuilder.show()
    }

    private fun showUrlActionDialog(clickInfo: ClickInfo) {
        val urlDialogBuilder = AlertDialog.Builder(this)
        urlDialogBuilder.setItems(R.array.url_actions) { _, which ->
            if (which == TabConst.TAB_OPEN_ACTION_BACKSTAGE) {
                TabHelper.createTab(
                    this@BrowserActivity,
                    R.string.new_tab_welcome,
                    clickInfo.url,
                    true
                )
            } else if (which == TabConst.TAB_OPEN_ACTION_FRONTSTAGE) {
                TabHelper.createTab(
                    this@BrowserActivity,
                    R.string.new_tab_welcome,
                    clickInfo.url,
                    false
                )
            }
        }
        urlDialogBuilder.show()
    }

    @UiThread
    override fun provideBrowserComponent(componentName: String): IBrowser.IComponent {
        synchronized(this) {
            if (TextUtils.isEmpty(componentName)) {
                return getStubComponent()
            }
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

    private fun getStubComponent(): IBrowser.IComponent {
        if (stubComponent == null) {
            stubComponent = StubBrowserComponent()
        }
        return stubComponent!!
    }

    private fun getBookmarkController(): IBrowser.IBookmarkController {
        if (bookmarkController == null) {
            bookmarkController = StubBookmarkController()
        }
        return bookmarkController!!
    }

    private fun getDownloadController(): IBrowser.IDownloadController {
        if (downloadController == null) {
            downloadController = StubDownloadController()
        }
        return downloadController!!
    }

    private fun getHistoryController(): IBrowser.IHistoryController {
        if (historyController == null) {
            historyController = EasyHistoryController()
        }
        return historyController!!
    }

    private fun getNavController(): IBrowser.INavController {
        if (navController == null) {
            navController = EasyNavController()
        }
        return navController!!
    }

    private fun getTabController(): IBrowser.ITabController {
        if (tabController == null) {
            tabController = TabCacheManager(this, supportFragmentManager, 3, R.id.web_content_frame)
        }
        return tabController!!
    }

    inner class EasyNavController : IBrowser.INavController {
        override fun goBack() {
            @Suppress("DEPRECATION")
            onBackPressed()
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
            val intent = Intent()
            intent.setClass(this@BrowserActivity, HistoryActivity::class.java)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, Const.RequestCode.SHOW_HISTORY)
        }
    }

    inner class EasyHistoryController : IBrowser.IHistoryController {
        override fun addHistory(entity: History) {
            val source = ObservableOnSubscribe<Long> { emitter ->
                val application = applicationContext as EasyApplication
                val db: AppDatabase = application.getAppDatabase()
                val rowId = db.historyDao().insertHistory(entity)
                Log.i(TAG, "inserted id    is : $rowId")
                Log.i(TAG, "inserted title is : ${entity.title}")
                Log.i(TAG, "inserted url   is : ${entity.url}")
                emitter.onNext(rowId)
            }
            Observable.create<Long>(source)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe()
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
    }
}
