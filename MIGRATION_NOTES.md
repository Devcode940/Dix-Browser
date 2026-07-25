# Dix Browser — Notes

**Repo:** https://github.com/Devcode940/Dix-Browser
**State:** 100% Kotlin (no `.java`), unified packages.

> ⚠️ Could not be compiled in the authoring environment (no Android SDK).
> Run `./gradlew assembleDebug` in Android Studio to validate. The items most
> likely to need a touch-up are listed under "Still to verify".

## Source facts

- 69 Kotlin source files, 0 Java.
- Namespace `com.devcode940.web`; `applicationId` unchanged.
- Path == package for every file (the original had a path/package split that
  created two parallel class hierarchies and a dead `ricky.easybrowser.*`
  package referenced from the layouts).
- AGP 8.5.2, Kotlin 2.0.20, SDK 35 / min 24, JDK 17.

## What the code actually does (not what the old summary docs claimed)

- **Browsing works.** `WebViewSecurityConfig` enables JavaScript and DOM storage
  by default — the original shipped with JS disabled, so it could not render the
  modern web. File-access-from-file and safe-browsing remain locked down.
- **Tabs.** `TabCacheManager` is an LRU fragment cache; `NewTabFragmentV2` hosts
  a `PageNestedWebView` per tab. Refresh uses `WebView.reload()` (a real reload,
  not a re-navigation that destroys state).
- **History.** The active renderer records history via a `SecureWebViewClient`
  subclass (`onPageFinished` → `IHistoryController.addHistory`), inserted on an
  IO coroutine scope. The history screen paginates from Room.
- **Downloads.** `BrowserDownloadManager` wraps the system `DownloadManager`.
- **Session restore.** Open tabs are saved to prefs (Gson) on destroy and
  restored on next launch.

## Things that were removed (they were stubs / dead code)

The original repo carried a large amount of scaffolding that looked like
features but was never wired in. These were deleted, not left as `TODO` shells:

- Hilt (plugin + `@HiltAndroidApp` + `di/AppModule`) — nothing used injection.
- RxJava 2 — three trivial DB calls were wrapped in it; converted to Coroutines.
- `DesktopModeManager`, `FullscreenManager`, `TranslateManager`,
  `OfflinePageManager`, `AdBlocker`, `BookmarkManager`, the password managers,
  the Gecko view stubs, `PageWebView`/`EasyWebView`/`PlaceholderView` (a second,
  unused renderer), the orphan `ui/browser` MVVM layer, and several feature
  fragments (`Summarizer`, `ResourceSniffer`, `Incognito`, `Toolbar`,
  `Video`, duplicate `ui/history` and `ui/download` fragments).
- AI-authored planning docs that overstate the project's state.

Net: 107 → 69 source files with no loss of actually-wired functionality.

## Still to verify

- **Compile.** This is the open risk. The package flatten, the coroutines
  conversion, and the back-press / activity-result migration are unverified.
- **Back gesture.** `onBackPressedDispatcher.addCallback(this) { … }` is used
  in place of the deprecated `onBackPressed`; confirm behaviour on the target
  API level.
- **`SecureWebViewClient` subclassing.** History is recorded by subclassing the
  shared secure client in `PageNestedWebView`; confirm SSL/error handling still
  behaves as intended.
- **CI workflow.** `.github/workflows/android.yml` is a Maven pipeline for a
  Gradle Android project — it will not build the app.

## Build

```bash
git clone https://github.com/Devcode940/Dix-Browser.git
cd Dix-Browser
./gradlew assembleDebug    # JDK 17, Android SDK 35
```
