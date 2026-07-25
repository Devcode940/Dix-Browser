# Changelog

All notable changes to **Dix Browser** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.0.0] - 2026-07-22

### Added (Phase 3)
- **Desktop Mode** — Switch between mobile and desktop User-Agent
- **Full Screen Mode** — Immersive browsing experience
- **Page Translation** — One-tap translation via Google Translate
- **Resource Sniffer** — Detect and download images, videos, and files from current page (`ResourceSnifferDialog`)
- **Picture-in-Picture Video** — Auto PiP support for video playback
- **Customizable Toolbars** — Show/hide and reorder toolbar buttons (`ToolbarConfigFragment`)
- New menu options for all Phase 3 features
- Resource Sniffer dialog with download support
- Toolbar customization screen

### Changed
- Updated `BrowserActivity` with Phase 3 managers and methods
- Enhanced browser menu with new advanced features

---

## [2.0.0] - 2026-07-22

### Added
- **Address Bar** with smart URL/search detection and multiple search engines (Google, Bing, DuckDuckGo)
- **Download Manager** with progress tracking, notifications, and file opening support
- **Settings Screen** with search engine selection, JavaScript toggle, cookies, and clear data options
- **Enhanced History** with search, individual deletion, and "Clear All" functionality
- **Bookmarks** infrastructure (`BookmarkManager`)
- **Incognito / Private Mode** foundation (`IncognitoManager` + `IncognitoTabFragment`)
- **Dark Mode** support with system theme following (`ThemeManager`)
- **Basic Ad Blocker** using domain blacklist
- **Modern Kotlin architecture** (ViewModel, ViewBinding, MVVM)
- **Version Catalog** (`libs.versions.toml`) for dependency management
- New **browser menu** with "New Incognito Tab" option
- Full English localization (app renamed to **Dix Browser**)

### Changed
- Updated build system to **Gradle 8.8 + AGP 8.5.2 + Kotlin 2.0.20**
- Target SDK raised to **35**
- Replaced legacy Groovy build files with **Kotlin DSL**
- Modernized `BrowserActivity` with new address bar and navigation
- Wired **DownloadListener** directly into WebView

### Fixed
- Replaced broken Maven CI workflow with proper Gradle CI (`android.yml`)
- Updated Gradle wrapper to 8.8

### Removed
- Removed Chinese strings and legacy UI elements

---

## [1.0.0] - 2019 (Original)

### Added
- Basic multi-tab WebView browser
- Tab caching with LRU
- Basic navigation and history
- No-image mode
- Original Chinese UI

---

[Unreleased]: https://github.com/Devcode940/DixBrowser/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/Devcode940/DixBrowser/releases/tag/v2.0.0
[1.0.0]: https://github.com/Devcode940/DixBrowser/releases/tag/v1.0.0