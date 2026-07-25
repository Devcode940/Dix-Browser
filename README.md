# Dix Browser

A lightweight Android web browser built on `WebView`, migrated from the legacy
Java **EasyBrowser** codebase to **100% Kotlin**.

- **Language:** Kotlin (no Java sources remain)
- **Build:** AGP 8.5.2, Kotlin 2.0.20, Gradle Kotlin DSL, version catalog
- **Min / Target SDK:** 24 / 35 (JDK 17)
- **applicationId:** `com.devcode940.web`
- **Architecture:** Activities + Fragments, ViewBinding, Room, Hilt (Application),
  Coroutines + RxJava 2 (legacy presenters), Material components

## Features

- Multi-tab browsing with LRU fragment caching (`TabCacheManager`)
- Two page renderers: `PageNestedWebView` (nested-scroll, modern) and
  `PageWebView` (legacy, with hide/show address-bar animations)
- Front page with saved-site shortcuts (`FrontPageView` / `SiteAdapterV2`)
- Browsing history (Room) with a paged history screen
- Address dialog, settings dialog, tab overview dialog
- Modern address bar, download manager (via `android.app.DownloadManager`)
- Centralized WebView security config, secure WebChromeClient/WebViewClient,
  content-security-policy interception
- Scaffolding for Phase-3 features: incognito, dark mode, ad blocker,
  desktop mode, fullscreen, page translation, resource sniffer,
  picture-in-picture, offline pages, an in-app summarizer, session restore

## Getting started

```bash
git clone <repo-url>
cd <project>
# Android Studio Hedgehog or newer, JDK 17, Android SDK 35
./gradlew assembleDebug
```

> The project is Kotlin-only. If you are coming from the old `Dix.zip`,
> see [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for the full list of fixes that
> were applied (the uploaded zip did not build).

## Project layout

```
app/src/main/java/com/devcode940/web/
├── EasyApplication.kt        # @HiltAndroidApp, Room + first-boot prefs
├── MainActivity.kt           # launcher
├── common/                   # constants
├── contract/                 # IBrowser, ITab, IWebView, … interfaces
├── entity/                   # bo (TabInfo) + dao (Room: History, WebSite)
├── page/                     # browser, frontpage, history, tab, tabpreview, …
├── ui/                       # address, download, theme, security, pip, …
├── web/                      # WebView views + security layer (webkit/legacy/gecko)
└── widget/                   # BrowserNavBar, WebSiteLogo, ptr (pull-to-refresh)
```

## License

Open source (see repository metadata). Originally derived from
`ricky9090/EasyBrowser`.
