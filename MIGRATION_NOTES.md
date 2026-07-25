# Dix Browser — Java → Kotlin Migration Notes

**Date:** 2026-07-25
**Source:** `Dix.zip` from https://github.com/Devcode940/Dix-Browser
**Result:** 100% Kotlin source (0 `.java` files), unified package structure.

> ⚠️ This migration could not be compiled in this environment (no Android SDK).
> Open the project in Android Studio and run a Gradle sync + build to validate.
> The notes below list the fixes applied and the items most likely to need touch-ups.

---

## What was converted

All 51 Java files (~4,900 LOC) were ported to Kotlin. Final main-source stats:
**107 Kotlin files / 6,867 LOC / 0 Java files.**

Areas converted: `common/`, `contract/`, `entity/` (bo + dao/Room), `utils/`,
`web/` (legacy, webkit, gecko + the existing Kotlin security layer),
`widget/` (BrowserNavBar, WebSiteLogo, pull-to-refresh PtrAdapter/PtrLayout),
`page/` (browser, frontpage, history, tab, tabpreview, address, setting),
and the entry points (`EasyApplication`, `MainActivity`).

## Structural fixes applied (the original did NOT build)

These were all pre-existing breakages in the uploaded `Dix.zip`; they were fixed
during the migration so the Kotlin project is internally consistent.

1. **Kotlin syntax inside a `.java` file** — `BrowserActivity.java#restorePreviousSession()`
   used `val`, `isNotEmpty()`, and `${...}`. Ported to valid Kotlin.
2. **Package/path mismatch everywhere** — files physically under
   `…/web/easybrowser/` declared packages both with and without the `easybrowser`
   segment, creating two parallel class hierarchies. Unified by moving all sources
   up to `com/devcode940/web/*` so **path == package** for all 110 Kotlin files
   (namespace kept as `com.devcode940.web`; `applicationId` unchanged).
3. **Layouts referenced a dead package** — every custom View in the layouts used
   the old `ricky.easybrowser.*` package (e.g. `<ricky.easybrowser.web.webkit.AddressBar>`).
   Repointed all 8 to `com.devcode940.web.*`.
4. **Missing dependency** — `FrontPagePresenterImpl`, `HistoryPresenterImpl`, and
   `BrowserActivity` use **RxJava 2**, which was not in the Gradle build. Added
   `rxjava:2.2.21` and `rxandroid:2.1.1`.
5. **Missing classes** — `BrowserActivity` referenced `ui.desktop.DesktopModeManager`
   and `ui.fullscreen.FullscreenManager`, which did not exist. Added minimal
   implementations so the references resolve.
6. **Interface/property gaps** — `BrowserActivity` called `ITabController.onTabRefresh()`
   (not on the interface) and read `TabInfo.url` (no such field). Added
   `onTabRefresh()` to `ITabController` (+ implementation in `TabCacheManager`)
   and a derived `TabInfo.url` (`= uri?.toString()`).
7. **Parcelable** — added the `kotlin-parcelize` plugin (it was in the version
   catalog but not applied) and converted `TabInfo` to `@Parcelize`.
8. **Test stubs** — `ExampleInstrumentedTest`/`ExampleUnitTest` were in the wrong
   package (`ricky.easybrowser`) and used the removed `androidx.test.InstrumentationRegistry`.
   Rewritten as Kotlin in the correct package with current test APIs.

## Things to verify / likely touch-ups in Android Studio

- **Nullability:** the original code is loose with nulls; some Kotlin call sites
  use `!!` or casts (e.g. `provideInfoList() as MutableList` in
  `BrowserActivity.onRestoreInstanceState`). Review if the IDE flags anything.
- **Deprecated APIs** (suppressed, not errors): `onBackPressed`, `startActivityForResult`,
  `onActivityResult`, `getParcelable*`, `getColor`, `userVisibleHint`.
- **RxJava** is preserved for fidelity. A nicer follow-up is to convert the two
  presenters (+ history insert) to Coroutines/Flow (the project already depends on
  `kotlinx-coroutines`).
- **CI workflow** `.github/workflows/` uses a *Java/Maven* pipeline for an Android
  *Gradle* project — it will not build the app. Replace with a Gradle workflow.
- **Double `web` package** (`com.devcode940.web.web.webkit`, `.web.legacy`,
  `.web.gecko`) is preserved for consistency; cosmetic only — consider flattening
  to `com.devcode940.web.webkit` etc. as a follow-up (requires moving files +
  updating refs).
- **WebView security config disables JavaScript by default** (`WebViewSecurityConfig`),
  which the modernized `PageNestedWebView` applies — so pages that need JS won't
  work until you opt in. This matches the original behavior; change deliberately.

## Build

```bash
git clone <this-repo>
cd <project>
# Android Studio Hedgehog+, JDK 17, SDK 35
./gradlew assembleDebug
```
