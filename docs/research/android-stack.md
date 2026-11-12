# Research: Current Android stack for a local-first app

**Ticket:** [#2 — Current Android stack for a local-first app](https://github.com/yzh44yzh/prudence/issues/2)
**Date:** 2026-11-12. All versions and distribution figures verified against the sources below as of this date.
**Context:** Prudence — personal, single-user, local-only Android expense tracker. No backend, no sync. Owner knows Go, wants Kotlin, first Android app.

## TL;DR recommendation

| Concern | Recommendation | Latest version (Nov 2026) |
|---|---|---|
| Language | Kotlin | 2.4.21 |
| UI | Jetpack Compose + Material 3 | BOM 2026.09.00 (compose 1.12.1, material3 1.4.0) |
| Local storage | Room | 2.8.5 |
| DI | Manual DI now; Hilt if the app grows | Dagger/Hilt 2.60.1 |
| Charts | Vico | 3.3.1 |
| minSdk | 26 (Android 8.0) | — |
| targetSdk / compileSdk | 36+ / 37 | — |
| Navigation | Navigation 3 (or none at MVP size) | 1.2.1 |
| Build | AGP 9.4.x, Gradle 9, KSP (never kapt), version catalog | AGP 9.4.0 |

---

## 1. UI: Jetpack Compose, not XML views

**Compose is the answer; do not start with XML views.**

- Google states plainly: "Jetpack Compose is Android's recommended modern toolkit for building native UI." — https://developer.android.com/compose
- Google's official position since 2023 is Compose-first: https://developer.android.com/blog/posts/android-ui-development-is-compose-first
- All current guidance, codelabs, the architecture samples, and the Compose Bill of Materials (BOM) assume Compose. XML layouts are legacy: maintained, but not where documentation or investment goes.
- Compose is declarative Kotlin — mentally closer to Go's "build the thing from data" style than to the imperative view-tree mutation of XML/Android views.
- Latest BOM: **2026.09.00**, mapping to compose-ui/foundation/material 1.12.1 and material3 1.4.0. — https://developer.android.com/develop/ui/compose/bom/bom-mapping
- Since Kotlin 2.0 the Compose compiler ships as a Kotlin Gradle plugin (`org.jetbrains.kotlin.plugin.compose`) — no separate compiler version pinning. — https://kotlinlang.org/docs/releases.html
- Compose 1.10+ requires `minSdk` 23. — https://apilevels.com/ (Android 6 footnote)

For Prudence: every screen (expense entry, category tree, report views) is a composable; Material 3 gives date pickers, cards, lists, and theming out of the box.

## 2. Local storage: Room

**Room is the default, officially recommended, and sufficient. SQLDelight is a legitimate alternative only if Kotlin Multiplatform is a real goal — it is not for Prudence.**

### Room (recommended)

- Official data-storage guidance names Room as the solution for structured on-device data: https://developer.android.com/training/data-storage and https://developer.android.com/training/data-storage/room
- Latest stable **2.8.5** (2026-09-09). — https://developer.android.com/jetpack/androidx/releases/room
- Since 2.7.0 (2025-04) Room is a Kotlin Multiplatform library and generates **Kotlin code via KSP by default**; it requires Kotlin language 2.0+ and recommends KSP2. — Room 2.7.0 release notes, same page.
- Room 2.8 raised its minSdk to API 23. — Room 2.8.0-rc02 notes.
- Kotlin-first API surface: `suspend` DAO functions and `Flow` for observable queries — expenses and category trees surface to the UI as streams for free.
- Schema export + auto-migrations + the Room Gradle plugin (`androidx.room`, requires a `schemaDirectory`) cover the one thing a personal app still must get right: not destroying user data on schema change.
- `room-ktx` is merged into `room-runtime` since 2.7.0 — just depend on `room-runtime` + `room-compiler` (ksp).

### SQLDelight (alternative, not recommended here)

- Latest **2.4.1** (2026-10-06), actively maintained by Cash App. — https://github.com/sqldelight/sqldelight/releases
- SQL-first: you write `.sq` files, it generates typesafe Kotlin. Great if you think in SQL and want KMP. For a first Android app it adds a second toolchain (its own Gradle plugin, dialects, migration-file conventions) for no benefit over Room on Android-only.

### Others

- **Realm** (MongoDB): MongoDB deprecated Atlas Device SDKs/Realm in September 2024. Do not adopt. — https://www.mongodb.com/docs/atlas/device-sdks/
- **DataStore** (Jetpack): the right tool for small key-value settings (replaces SharedPreferences). Useful for e.g. default currency or last-used category; not for expenses. — https://developer.android.com/topic/libraries/architecture/datastore

## 3. Dependency injection: manual first, Hilt if it grows

- **Hilt is Jetpack's recommended DI library**, built on Dagger (compile-time, KSP-based). — https://developer.android.com/training/dependency-injection (section "Use Hilt in your Android app")
- Latest Dagger/Hilt **2.60.1** (2026-07). Note: Hilt minSdk is now 23; the Hilt Gradle plugin requires AGP 9. — https://github.com/google/dagger/releases
- Google's own guidance is to understand **manual DI** first (an app-container class wiring constructor dependencies by hand): https://developer.android.com/training/dependency-injection/manual. For a single-module app with a handful of objects (database, DAOs, a repository or two, ViewModels) manual DI is ~50 lines, zero magic, and trivially testable.
- **Koin** is the popular runtime-resolution alternative — pleasant API but reflection-ish runtime graph and weaker compile-time guarantees than Hilt.

Recommendation for Prudence: start with **manual DI** (constructor injection everywhere, one `AppContainer`); it fits MVP scale and teaches the pattern Hilt automates. Adopt Hilt only if the object graph becomes painful.

## 4. Charting: Vico

Report charts need bar (spending over time within an interval), line, and pie (category breakdown). There is no official Google chart library.

- **Vico** — https://github.com/patrykandpatrick/vico (3.2k stars, Apache-2.0, actively maintained). Compose-native, Compose-Multiplatform. Column (bar), line, and candlestick cartesian charts are stable; **pie/donut charts exist but are marked experimental** (breaking changes possible). Latest stable **3.3.1**. — https://guide.vico.patrykandpatrick.com/
- **MPAndroidChart** — revived as v4.0.1: a Kotlin rewrite with a Compose module, nine chart types including pie. Views-first heritage; distributed via JitPack, not Maven Central. — https://github.com/PhilJay/MPAndroidChart

Recommendation: **Vico**. Compose-native API, Maven Central, active development. The experimental status of its pie chart is acceptable — a pie breakdown can also ship as a stacked bar or a list initially. MPAndroidChart v4 is the fallback if Vico's pie support disappoints.

## 5. `minSdkVersion`: 26

Distribution data (cumulative usage, October 2026 StatCounter data, published 2026-11-03) — https://apilevels.com/:

| API level | Android | Covers |
|---|---|---|
| 24 (7.0) | Nougat | 99.5% |
| **26 (8.0)** | **Oreo** | **99.2%** |
| 28 (9) | Pie | 98.2% |
| 30 (11) | R | 93.5% |
| 33 (13) | Tiramisu | 80.3% |
| 35 (15) | — | 57.9% |
| 36 (16) | Baklava | 41.6% |
| 37 (17) | Cinnamon Bun | 4.0% |

Hard floors that matter:

- **AndroidX libraries require `minSdk` 24 since July 2026**; Google Play services likewise. — apilevels.com footnotes
- Room 2.8, Hilt 2.60, Compose 1.10 each require 23.

Recommendation: **`minSdk = 26`**. Rationale: 99.2% of devices, comfortably above the AndroidX 24 floor, and **API 26 includes `java.time` natively** (below 26 you'd need core-library desugaring) — important for Prudence, where dates and intervals are domain primitives. For a single-user personal app this choice is nearly free anyway: it only has to run on the owner's phone.

Companion settings: `targetSdk = 36` (Google Play requires 36+ for new apps since 2026-08-31 — irrelevant for sideloading, but the right default), `compileSdk = 37`. — https://support.google.com/googleplay/android-developer/answer/11926878

## 6. Everything else a Go developer should know before locking the stack

- **Kotlin is the language** — latest stable **2.4.21** (2026-10-08); 2.5.0 due December 2026. Java is not needed anywhere. — https://kotlinlang.org/docs/releases.html
- **Coroutines and `Flow` are the concurrency model.** There are no goroutines/channels; think structured concurrency: `suspend` functions, `viewModelScope`, `StateFlow` in the UI. Room and everything modern speak Flow. — https://developer.android.com/kotlin/coroutines
- **Official app architecture**: UI (Compose) → ViewModel → (repository) → Room, with unidirectional data flow. For Prudence's size, a single Gradle module with this layering is right; do not modularize. — https://developer.android.com/topic/architecture
- **Build tooling**: Android Studio + **AGP 9.4.0** (2026-09; 9.0 landed 2026-01 and bundles Kotlin support — no separate `kotlin-android` plugin application), Gradle 9, Kotlin DSL (`build.gradle.kts`), and a version catalog (`gradle/libs.versions.toml`) from day one. — https://developer.android.com/build/releases/agp-9-0-0-release-notes
- **KSP, never kapt.** kapt is deprecated; Room and Hilt both run on KSP2. — https://developer.android.com/build/migrate-to-ksp
- **Navigation**: for a 3–4 screen app, Navigation 3 (`androidx.navigation3`, stable **1.2.1**, 2026-11-04) is the Compose-native choice — a typed back stack you own. At MVP size even a hand-rolled `when` over screen state is defensible. — https://developer.android.com/jetpack/androidx/releases/navigation3
- **Dates**: with `minSdk 26`, use `java.time` (`LocalDate`) directly; `kotlinx-datetime` only becomes interesting if KMP ever happens.
- **Testing**: Room tests run against an in-memory DB; Compose has UI-test APIs; plain JUnit for report/rollup logic. The domain logic (category tree rollup, interval aggregation) is pure Kotlin — keep it Android-free so it tests on the JVM in milliseconds.
- **Reference app**: Google's `nowinandroid` sample is the canonical "current stack" codebase to crib patterns from. — https://github.com/android/nowinandroid
- **Backup**: even local-only apps get auto-backup to the user's Google account by default; for a finance app, decide deliberately whether the DB participates. — https://developer.android.com/guide/topics/data/autobackup

## Sources

- Compose recommendation: https://developer.android.com/compose ; https://developer.android.com/blog/posts/android-ui-development-is-compose-first
- Compose BOM mapping (2026.09.00, compose 1.12.1, material3 1.4.0): https://developer.android.com/develop/ui/compose/bom/bom-mapping
- Room releases (2.8.5 stable, 2.7 KMP/KSP changes, minSdk 23): https://developer.android.com/jetpack/androidx/releases/room
- Data storage overview: https://developer.android.com/training/data-storage
- SQLDelight releases (2.4.1): https://github.com/sqldelight/sqldelight/releases
- Realm deprecation: https://www.mongodb.com/docs/atlas/device-sdks/
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
- DI guidance (Hilt recommended; manual DI): https://developer.android.com/training/dependency-injection and .../dependency-injection/manual
- Dagger/Hilt releases (2.60.1, minSdk 23, AGP 9 requirement): https://github.com/google/dagger/releases
- Vico docs (3.3.1, chart types, pie experimental): https://guide.vico.patrykandpatrick.com/ ; https://github.com/patrykandpatrick/vico
- MPAndroidChart v4: https://github.com/PhilJay/MPAndroidChart
- API distribution + AndroidX minSdk floor: https://apilevels.com/ (October 2026 StatCounter data, updated 2026-11-03)
- Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Kotlin releases (2.4.21): https://kotlinlang.org/docs/releases.html
- AGP 9 release notes: https://developer.android.com/build/releases/agp-9-0-0-release-notes
- Navigation 3 releases (1.2.1 stable): https://developer.android.com/jetpack/androidx/releases/navigation3
- App architecture guide: https://developer.android.com/topic/architecture
- nowinandroid: https://github.com/android/nowinandroid
