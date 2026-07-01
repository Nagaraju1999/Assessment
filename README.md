# Stock Market Tracker

A native Android stock market tracking app built with Kotlin, Jetpack Compose, and Clean Architecture. Track a personal watchlist with live polling prices, view historical price charts across multiple time ranges, and configure price alerts that notify you in-app and via system notification when a stock crosses your target.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Module Guide](#module-guide)
5. [Tech Stack](#tech-stack)
6. [Getting Started](#getting-started)
7. [API Configuration](#api-configuration)
8. [Testing](#testing)
9. [Offline Strategy](#offline-strategy)
10. [Smart Polling & Rate Limiting](#smart-polling--rate-limiting)
11. [Price Trend Indicator](#price-trend-indicator)
12. [Error Handling](#error-handling)
13. [Performance Notes](#performance-notes)
14. [Design Notes](#design-notes)
15. [Roadmap](#roadmap)
16. [Contributing](#contributing)
17. [License](#license)

---

## Features

- **Watchlist** — search for any publicly traded symbol and track it with live prices that poll every 30 seconds while the screen is visible.
- **Price charts** — a custom Canvas-drawn historical price chart on the stock details screen, with five selectable time ranges (1D / 1W / 1M / 3M / 1Y).
- **Price trend indicator** — a lightweight linear-regression trend signal (up/down/flat, a projected next price, and a confidence percentage) computed from the same candle data already shown on the chart.
- **Price alerts** — configure "notify me when AAPL goes above $200"-style alerts. Triggered alerts fire both an in-app message and a system notification, and tapping the notification deep-links straight into that stock's details screen.
- **Smart, adaptive polling** — each alert is checked on its own schedule that tightens automatically as price approaches its target, backed by a client-side rate limiter so request volume never exceeds the API's budget.
- **Offline-aware** — when connectivity drops, the watchlist falls back to the last successfully fetched price instead of blanking out, clearly labeled "Offline · last updated HH:mm" so stale data is never confused with live data.
- **Dark mode and dynamic color** — follows the system theme automatically, with Material You dynamic color on Android 12+.

---

## Architecture

```
Presentation Layer (feature-watchlist, feature-stock-details, feature-alerts)
   Composable UI -> ViewModel (StateFlow + SharedFlow)
            |
            v  depends on
Domain Layer (domain) -- pure Kotlin, zero Android imports
   Use Cases -> Repository Interfaces -> Domain Models
            ^
            |  implements
Data Layer (data)
   Repository Impl -> Remote/Local Data Sources -> Mappers
        |                                   |
        v                                   v
core-network                         core-database
   Retrofit/OkHttp                      Room
```

The app follows Clean Architecture with an MVVM presentation layer and the Repository pattern, with a strict unidirectional dependency flow: feature modules depend on `domain`, never on each other. Only the `app` module is allowed to depend on all three feature modules, since it's the one place responsible for assembling them into a single navigation graph.

**State management** — every screen exposes exactly one `StateFlow<UiState>` (an immutable data class) for everything the screen renders, and a separate `SharedFlow<Event>` for one-time signals like Snackbar messages that must never replay on recomposition or configuration change.

**Dependency injection** — Hilt throughout. Every repository, data source, and use case is constructor-injected; there are no service locators or manual wiring beyond what Hilt generates.

---

## Project Structure

```
StockMarketTracker/
├── app/                          Application shell — no business logic
│   ├── MainActivity.kt           Single Compose host activity
│   ├── StockTrackerApplication.kt  @HiltAndroidApp entry point
│   ├── navigation/AppNavHost.kt  Assembles all 3 feature nav graphs
│   └── di/AppModule.kt           Supplies BuildConfig.FINNHUB_API_KEY
│
├── core-common/                  Reusable Android-aware utilities
│   ├── dispatcher/DispatcherProvider.kt
│   ├── network/NetworkMonitor.kt
│   ├── extensions/FlowExtensions.kt, NumberExtensions.kt
│   ├── formatter/DateFormatter.kt
│   └── logger/AppLogger.kt
│
├── core-network/                 Retrofit, OkHttp, DTOs, interceptors
│   ├── api/FinnhubApi.kt
│   ├── dto/QuoteDto.kt, CandleDto.kt, SearchResultDto.kt, CompanyProfileDto.kt
│   ├── interceptor/AuthInterceptor.kt, NetworkResultInterceptor.kt
│   ├── mapper/NetworkMapper.kt
│   ├── throttle/ApiRateLimiter.kt
│   └── di/NetworkModule.kt
│
├── core-database/                Room database, entities, DAOs
│   ├── entity/WatchlistEntity.kt, AlertEntity.kt
│   ├── dao/WatchlistDao.kt, AlertDao.kt
│   ├── converter/Converters.kt
│   ├── migration/DatabaseMigrations.kt
│   ├── StockDatabase.kt
│   └── di/DatabaseModule.kt
│
├── core-ui/                      Shared Compose components and theme
│   ├── theme/StockTrackerTheme.kt, Color.kt, Typography.kt, Shape.kt
│   └── components/LoadingIndicator.kt, ErrorState.kt, EmptyState.kt,
│                   PriceChangeChip.kt, StockTrackerTopBar.kt
│
├── domain/                       Pure Kotlin — no Android imports
│   ├── model/Stock.kt, StockCandle.kt, StockAlert.kt, CompanyProfile.kt,
│   │          StockSearchResult.kt, SmartPollingCalculator.kt,
│   │          PriceTrend.kt, PriceTrendCalculator.kt
│   ├── repository/StockRepository.kt, AlertRepository.kt
│   ├── usecase/watchlist/, stock/, alert/   (9 use cases total)
│   ├── exception/DomainException.kt
│   └── result/Result.kt
│
├── data/                         Repository implementations
│   ├── repository/StockRepositoryImpl.kt, AlertRepositoryImpl.kt
│   ├── source/remote/StockRemoteSource.kt
│   ├── source/local/StockLocalSource.kt, AlertLocalSource.kt
│   ├── mapper/StockMapper.kt, AlertMapper.kt
│   └── di/DataModule.kt
│
├── feature-watchlist/            Watchlist screen, search, add/remove
├── feature-stock-details/        Details screen, Canvas price chart
├── feature-alerts/                Alerts CRUD, system notifications
│   (each feature module: presentation/{ui,viewmodel,state}, navigation/, di/)
│
├── gradle/libs.versions.toml     Single source of truth for all versions
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── README.md
```

---

## Module Guide

### `app`
The application shell. Contains `StockTrackerApplication` (`@HiltAndroidApp`), `MainActivity` (a single Compose host activity with edge-to-edge enabled), and `AppNavHost` — the one file in the project that imports all three feature modules' navigation graphs and assembles them, since no feature module depends on another. `AppModule` supplies the Finnhub API key read from `BuildConfig`, which only the `app` module has access to.

### `core-common`
Android-aware utilities with no feature-specific knowledge: `DispatcherProvider` (a testable coroutine dispatcher abstraction), `NetworkMonitor` (a Flow-based connectivity observer built on `ConnectivityManager` callbacks), `pollingFlow`/`asResult` Flow extensions, `DateFormatter`, `AppLogger`, and price/percentage formatting helpers.

### `core-network`
Everything Retrofit-related. `FinnhubApi` declares four endpoints (quote, candles, search, company profile) with no auth parameters — `AuthInterceptor` transparently appends the API key to every outgoing request. `NetworkResultInterceptor` converts HTTP status codes into typed exceptions (`UnauthorizedException`, `RateLimitException`, `ServerException`, a generic `HttpException`) before Retrofit's own generic exception type ever surfaces. DTOs map directly to Finnhub's terse JSON field names, and `NetworkMapper` converts them into intermediate network models with null-safety resolved. `throttle/ApiRateLimiter` is the sliding-window request-budget ceiling consulted by every outgoing call (see [Smart Polling & Rate Limiting](#smart-polling--rate-limiting)).

### `core-database`
Room setup for two tables: `watchlist` (identity fields plus offline-cache price columns) and `alerts`. `WatchlistDao.observeAll()` and `AlertDao.observeActiveAlerts()` both return `Flow` for reactive collection. `MIGRATION_1_2` is a real, tested additive schema migration — not a destructive fallback — supporting the offline price cache.

### `core-ui`
Shared Compose building blocks with no domain knowledge: the Material3 theme (dynamic color on Android 12+, a static brand palette fallback on older devices, full dark mode support), and five reusable components — `LoadingIndicator`, `ErrorState`, `EmptyState`, `PriceChangeChip`, `StockTrackerTopBar` — used across all three features.

### `domain`
A pure Kotlin JVM module with no Android SDK dependency at all, enforced by using the `kotlin.jvm` Gradle plugin rather than `android.library`. Contains every domain model, both repository interfaces, all nine use cases, the `DomainException` sealed hierarchy, and `Result<T>` itself. Also contains the two pure-function calculators behind this app's adaptive behavior: `SmartPollingCalculator` (alert poll-interval interpolation) and `PriceTrendCalculator` (the linear-regression trend signal) — both fully Android-free and directly unit-testable.

### `data`
Bridges `core-network` and `core-database` to the domain layer. `StockRepositoryImpl` implements the watchlist polling loop, the offline cache write-through/read-through strategy, and per-symbol fault isolation, so one quote failing doesn't fail the whole list. `AlertRepositoryImpl` implements the alert-triggering evaluation loop, scheduling each active alert independently using `SmartPollingCalculator`. `StockRemoteSource` is the single seam where low-level network exceptions are translated into domain-level exceptions, and where `ApiRateLimiter` is consulted before every outgoing call.

### `feature-watchlist` / `feature-stock-details` / `feature-alerts`
Each feature follows the same internal layout: `presentation/{ui, viewmodel, state}`, `navigation/`, and a `di/` package where needed. Each ViewModel exposes one `StateFlow<UiState>` and one `SharedFlow<Event>`. Each screen splits into a stateful `*Route` (collects the Hilt ViewModel) and a stateless `*Screen` (a pure function of `UiState`, previewable without DI). `feature-stock-details` additionally contains the custom Canvas-drawn `PriceChart` and the `PriceTrendIndicator` component. `feature-alerts` additionally contains `AlertNotifier`, which posts the system notification when a price alert triggers.

---

## Tech Stack

| Technology | Why |
|---|---|
| **Kotlin 2.0** | K2 compiler, modern coroutines and Flow support |
| **Jetpack Compose + Material3** | Declarative UI, native support for dynamic color and dark theming |
| **Hilt** | Compile-time-safe DI with first-class `hiltViewModel()` and `SavedStateHandle` integration for Compose Navigation |
| **Retrofit + OkHttp** | Declarative API definitions, composable interceptor chain for auth and error mapping |
| **Room** | Type-safe SQLite with native `Flow` support for reactive DAOs |
| **Coroutines + Flow** | `StateFlow`/`SharedFlow` are the idiomatic Compose state-management primitives |
| **Coil** | Compose-native async image loading for the company logo |
| **Navigation Compose** | Type-safe, Compose-native navigation with built-in deep link support, used for the alert-notification-to-details flow |
| **JUnit4 + MockK + Turbine** | MockK is Kotlin-idiomatic with no final-class mocking workarounds; Turbine makes Flow assertions readable |
| **Robolectric** | Lets Room DAO and migration tests run as fast JVM unit tests instead of requiring an emulator |

No third-party charting library is used — a single-series line chart is well within what Compose's `Canvas` draws cleanly, and that kept the dependency surface smaller.

---

## Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Ladybug (2024.2.1) or newer |
| JDK | 17 |
| Gradle | 8.7 (via wrapper — no separate install needed) |
| Android Gradle Plugin | 8.5.2 |
| Compile / Target SDK | 35 |
| Minimum SDK | 26 (Android 8.0) |

### Build & Run

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/StockMarketTracker.git
   cd StockMarketTracker
   ```

2. **Get a free Finnhub API key**
   Register at finnhub.io/register. The free tier provides 60 API calls/minute, which is comfortably enough for this app's 30-second polling interval against a typical watchlist size.

3. **Configure `local.properties`**
   Copy `local.properties.template` to `local.properties` (already gitignored) and add your key:
   ```properties
   FINNHUB_API_KEY=your_finnhub_api_key_here
   sdk.dir=/path/to/your/Android/sdk
   ```

4. **Sync and build**
   Open the project in Android Studio and let it sync, or from the command line:
   ```bash
   ./gradlew build
   ```

5. **Run**
   Select the `app` run configuration and run on an emulator or device running Android 8.0+.

6. **Run the test suite**
   ```bash
   ./gradlew test
   ```
   This runs the full unit test suite across every module, including the Robolectric-backed Room DAO and migration tests — no emulator required.

### A note on configuration

The API key is injected via `BuildConfig.FINNHUB_API_KEY`, generated at build time from `local.properties` — it's never hardcoded in source, and `local.properties` is gitignored. `usesCleartextTraffic="false"` is set in the manifest, so all network calls (the Finnhub API and company logo images) are HTTPS-only. ProGuard rules for release builds keep Retrofit interfaces, Room entities/DAOs, Gson DTOs, and Hilt-generated classes intact.

---

## API Configuration

The app uses the [Finnhub](https://finnhub.io) REST API (`https://finnhub.io/api/v1/`):

| Endpoint | Purpose |
|---|---|
| `GET /quote` | Real-time price quote, used by both watchlist and details-screen polling |
| `GET /stock/candle` | OHLCV historical candles for the price chart, parameterized by resolution and date range per selected time range |
| `GET /search` | Symbol search when adding a stock to the watchlist |
| `GET /stock/profile2` | Company metadata — name, industry, logo, market cap — for the details screen header |

`AuthInterceptor` appends the API key as a `token` query parameter to every outgoing request, so individual endpoint declarations in `FinnhubApi` stay free of authentication boilerplate.

---

## Testing

The project has 215 unit tests across 35 test files, spanning every module — repositories, use cases, ViewModels, mapper classes, and business logic.

| Module | What's Covered |
|---|---|
| `core-common` | `Result` operators, Flow extensions (`asResult`, `pollingFlow`), date formatting, number formatting |
| `core-network` | DTO-to-network-model mapping, `AuthInterceptor` token injection, every HTTP-status-to-exception branch, and `ApiRateLimiter`'s sliding-window acquire/throttle/eviction behavior |
| `core-database` | Both DAOs' queries — including the `observeActiveAlerts` filter — the enum type converter round-trip, and the v1-to-v2 schema migration |
| `domain` | `Result`, the `Stock`/`StockHistory` derived properties, `StockAlert.isSatisfiedBy` (the core alert-triggering rule), `SmartPollingCalculator`'s interval interpolation, `PriceTrendCalculator`'s regression and confidence calculation, and every use case with real validation logic |
| `data` | Both mappers, `StockRemoteSource`'s exception translation and rate-limit enforcement, and both repository implementations — including the per-alert adaptive polling loop and the offline cache write/read paths |
| `feature-watchlist` | `WatchlistUiState` derived properties and full ViewModel coverage (polling states, search debounce, add/remove) |
| `feature-stock-details` | `StockDetailsUiState`, route-string building, and full ViewModel coverage (quote polling, range switching, watchlist toggle, price-trend state wiring) |
| `feature-alerts` | `AlertsUiState` validation, notification content building, and full ViewModel coverage (CRUD, the triggered-alert-to-notification flow) |

Trivial pass-through wrappers with no branching logic are intentionally left untested rather than padding the suite with tests that assert nothing beyond "calling a function calls that function." Room DAO and migration tests run under Robolectric rather than `androidTest`, so the entire suite runs via `./gradlew test` with no emulator required.

---

## Offline Strategy

`WatchlistEntity` carries four nullable cache columns (`cachedPrice`, `cachedChange`, `cachedPercentChange`, `cachedAt`) alongside its identity fields. On every successful poll, `StockRepositoryImpl` writes the live quote into these columns — a write-through cache. When a poll fails, whether from no connectivity, a Finnhub rate limit, or any other network exception, the repository falls back to the cached values instead of zeroing out the row, marking the returned `Stock.isCached = true` with the real `cachedAt` timestamp. The UI surfaces this as "Offline · last updated HH:mm" in the watchlist row, so cached data is never presented as if it were live. If no successful poll has ever happened for a symbol — for example, it was added while offline — the row still renders with its identity (symbol, company name) and zeroed price fields rather than disappearing from the list entirely.

`addToWatchlist` and `searchStocks` both check connectivity up front and fail fast with a clear "no internet" error rather than letting a doomed network call time out, so the user gets immediate feedback when offline.

---

## Smart Polling & Rate Limiting

Alert evaluation uses an adaptive, per-alert schedule rather than checking every active alert on one shared tick. `SmartPollingCalculator` (in `domain`) computes each alert's next check interval as a pure function of how close the current price is to its target — interpolating between a 15-second minimum (price at or very near the target) and a 5-minute maximum (price 5% or further from target). `AlertRepositoryImpl.observeTriggeredAlerts()` runs each active alert on its own independent coroutine loop using this interval, so a watchlist with many alerts doesn't multiply request volume the way a single fixed polling interval applied to every alert would: most alerts, most of the time, sit comfortably far from their target and are checked only a few times an hour, while the handful actually approaching their threshold get checked every few seconds.

As a hard safety ceiling beneath that adaptive behavior, `ApiRateLimiter` (in `core-network`) is a sliding-window limiter capping outgoing Finnhub requests at 50 per 60-second window — below the API's actual 60/minute free-tier limit, leaving headroom for ad-hoc calls like symbol search. It's consulted in `StockRemoteSource`, the single seam every network call already passes through, so every caller (watchlist polling, details-screen polling, alert evaluation, search) is throttled identically with no risk of one caller bypassing it. If the budget is exhausted, the affected call fails with the same `RateLimitExceededException` a server-side 429 would produce, and the existing error-handling path takes over from there — no separate UI or retry logic was needed.

---

## Price Trend Indicator

The stock details screen shows a lightweight trend signal alongside the price chart: a direction (up, down, or flat), a naive next-bar price projection, and a confidence percentage. This is computed by `PriceTrendCalculator` (in `domain`) via ordinary least-squares linear regression over the visible candles' closing prices — the same statistical technique behind a basic trendline, not a machine learning model. Confidence is the regression's R-squared value, shown explicitly so the indicator doesn't overstate its own reliability on a noisy, non-linear series. Below three candles, or while history is loading or has failed, no trend is shown rather than one computed from too little data.

This is presented to the user as a directional signal derived from recent price history, not a financial forecast — the UI copy and the explicit confidence percentage are both deliberate about that framing.

---

## Error Handling

Every data-returning function across the `domain` and `data` layers returns a `Result<T>` (`Success` / `Error` / `Loading`) rather than throwing raw exceptions into the presentation layer. `StockRemoteSource` is the single seam where low-level network exceptions are translated into a closed set of domain exceptions: no internet, unauthorized, rate limit exceeded, server error, timeout, empty response, stock not found, and a catch-all unknown error. Every ViewModel maps these to a user-facing message, preferring the exception's own descriptive copy. A poll failure with no prior data sets a full-screen error state; a poll failure when data already exists instead surfaces a one-time message, so the user is never shown a blank screen for data they already had a moment ago.

---

## Performance Notes

- **Per-symbol fault isolation** — one watchlist stock's quote failing doesn't block or fail the other stocks in the same poll batch.
- **Search debouncing** — the watchlist's stock search waits 350ms after the last keystroke before firing a network request.
- **Stable `LazyColumn` keys** — every list is keyed by a stable identifier, letting Compose skip unnecessary recomposition and correctly animate item insertion and removal.
- **Animated state transitions** — loading, error, empty, and content states crossfade rather than jump-cut, so the UI never feels frozen mid-transition.
- **Cancellation-aware search and chart loading** — both the watchlist search and the details screen's time-range history fetch track their coroutine job and cancel any in-flight request before starting a new one, so rapid input can't produce out-of-order state.
- **Dispatcher abstraction** — all I/O-bound repository work runs on an injected IO dispatcher rather than the caller's context, keeping polling off the main thread without scattering `withContext` calls everywhere.

---

## Design Notes

A few decisions worth calling out for anyone reading through the code:

**Long-press to remove, not a persistent delete icon.** Keeps the watchlist row visually focused on price data. The long-press gesture is also exposed as a discoverable custom accessibility action for screen reader users, since the gesture itself has no visible affordance.

**No charting library.** A single-series line chart is well within what Compose's `Canvas` draws cleanly — avoiding the dependency and APK-size cost of a library for a problem that's straightforward to solve directly.

**`Result<T>` lives in `domain`, not in a shared utilities module.** It's pure Kotlin with zero Android dependencies, and `domain` is the architectural root every other module ultimately depends on, so that's where cross-cutting types with no platform coupling belong.

**One API key binding, defined once.** The key is read from `BuildConfig` in the `app` module — the only module with access to it — and provided into the dependency graph from there, rather than duplicated or partially stubbed in a lower-level module.

**Per-alert coroutines, not a shared poll tick.** `AlertRepositoryImpl.observeTriggeredAlerts()` gives each active alert its own independent coroutine loop rather than checking the whole set on one fixed interval. This is what makes the adaptive interval in `SmartPollingCalculator` actually save requests in practice — a single shared tick would still poll every alert at whatever the tightest alert's interval needed to be.

**Trend indicator framed as a signal, not a forecast.** `PriceTrendCalculator` deliberately surfaces its R-squared confidence value in the UI rather than hiding it — a regression line over a choppy, sideways series is technically computable but not meaningfully predictive, and the confidence percentage makes that visible instead of presenting every trend with equal authority.

---

## Roadmap

- **Background alert evaluation** via WorkManager, so alerts can trigger even when the app isn't in the foreground.
- **Candlestick chart mode** as an alternative to the current line chart, reusing the existing `Canvas` chart infrastructure.
- **Multi-currency support** for non-US symbols as data coverage allows.

### Known limitations

- Polling only runs while the relevant screen is visible; there's no background service yet, so an alert won't trigger while the app is fully backgrounded or killed.
- Prices are USD-only, matching Finnhub's free-tier coverage of US equities.
- The price trend indicator is a simple linear-regression signal over recent closing prices, not a predictive model — it's most informative on a clearly trending series and least informative on a choppy, sideways one (reflected honestly in its confidence percentage).

---

## Contributing

Issues and pull requests are welcome. For larger changes, please open an issue first to discuss what you'd like to change.

When contributing code:
- Follow the existing module boundaries — domain logic belongs in `domain`, platform-specific code belongs in the appropriate `core-*` module, and feature-specific UI belongs in its own `feature-*` module.
- Add unit tests for new repositories, use cases, ViewModels, and mapper classes.
- Run `./gradlew test` before opening a PR.

---

## License

```
MIT License

Copyright (c) 2024 Stock Market Tracker contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
