# Prudence — MVP Spec

_Personal expense tracking for Android. Single user, local-only. Status: spec complete, ready for implementation tickets. Source of decisions: [Map: Prudence MVP spec](https://github.com/yzh44yzh/prudence/issues/1). Domain vocabulary: [GLOSSARY.md](./GLOSSARY.md)._

## 1. Scope

**In scope**

- Record expenses: amount, date, category, optional note
- User-editable category tree of arbitrary depth
- Reports over an interval: total + category breakdown with rollup
- CSV export of all expenses (backup)
- Android only, single user, no accounts, local-only storage

**Out of scope (this effort)**

- iOS; cloud sync; accounts/multi-user
- Income tracking; multi-currency; receipt photos; recurring expenses; budgets
- CSV import / restore (one-way export only)
- Localization

**Post-MVP fog** (not specified, not scheduled): onboarding beyond seed categories, theming/visual polish, backup-restore UX.

## 2. Domain model

- **Expense** = amount + date + exactly one category + optional free-text note.
  - Date is day-granularity (`java.time.LocalDate`); no time-of-day.
  - Amount is a `Long` of **minor units** (kopecks/cents); never floating point.
  - Display currency is a single app-wide setting (DataStore), used only for formatting; expenses carry no currency.
- **Category** = node in one user-editable tree of arbitrary depth. Reports roll children up into ancestors.
  - **Delete**: the category's expenses are reassigned and its child categories reparented to the deleted category's parent. Nothing is lost.
  - **Move**: any category (with subtree) may move under any other category at any time. No tree history — reorganizing changes rollup of past intervals too.
  - **Uncategorized**: one permanent root category; undeletable; sink for root-category deletions.
- **First run** seeds a flat root-level list: Food, Transport, Housing, Health, Entertainment (+ Uncategorized). All except Uncategorized are editable/deletable.

## 3. Stack

| Concern | Choice |
|---|---|
| Language / toolkit | Kotlin 2.4, Jetpack Compose (BOM 2026.09.00) + Material 3 |
| Storage | Room 2.8.5 (KSP2), exported schema + auto-migrations |
| DI | Manual app container (no Hilt/Koin) |
| Navigation | Navigation 3 (1.2.1), type-safe `@Serializable` routes |
| Settings | DataStore: display currency, last-used category |
| Charts | **None** — inline amount bars are plain Compose drawing |
| minSdk / target / compile | 26 / 36 / 37 |
| Build | AGP 9.4, Gradle 9, version catalog, KSP (not kapt) |

Full findings: [docs/research/android-stack.md](https://github.com/yzh44yzh/prudence/blob/research/android-stack/docs/research/android-stack.md) (on branch `research/android-stack`). Reference project: nowinandroid.

## 4. Architecture

- Single `app` Gradle module, packages by feature: `expenselist`, `addexpense`, `categories`, `report`, `export`, `data`, `settings`.
- Unidirectional data flow: Compose UI ← `StateFlow` ← ViewModel ← repository ← Room. DAO `Flow` streams make lists and reports live.
- One ViewModel per screen (four total). UI sees domain types (Expense, Category), never Room entities.
- Manual app container wires database → DAOs → repositories → ViewModel factories.
- Four Navigation 3 destinations: List (home), AddExpense, Categories, Report. No bottom-nav scaffold, no drawer.

## 5. Screens

Structure per the winning prototype (**Variant B**; all variants preserved at [prototype/core-screens.html](https://github.com/yzh44yzh/prudence/blob/prototype/core-screens/prototype/core-screens.html)):

1. **Expense list = home.** App bar shows the running current-month total; icons for Report and Settings. List grouped by date (newest first), rows show category, note (if any), amount. Persistent **"+ Add expense"** button pinned at the bottom.
2. **Add expense** (full-screen): big centered amount entry; date row defaulting to today; **category as a chip path** — selected path chips (`Food ✕ Groceries ✕`) remove levels, chips below offer the next level, tap selects; optional note; Save.
3. **Categories** (from ⚙ settings icon): indented tree; per-row rename (✎) and move/delete (⋮) with the semantics in §2; Uncategorized locked at the bottom; ＋ in the app bar adds a category.
4. **Report** (from 📊 icon): see §6.
5. **Settings** (⚙): display currency; **Export CSV**.

## 6. Reports

- **Two sections only**: (a) total spent in the interval; (b) category breakdown. No spending-over-time chart.
- **Breakdown**: ranked list, one tree level at a time, sorted by amount descending. Row = category name, rolled-up total, percentage (of interval total at top level, of the parent category when drilled), inline amount bar. Tap drills into children with a back path up. Categories with zero spending in the interval are hidden.
- **Interval picker**: presets **This week / This month / Custom…** + **‹ ›** arrows shifting the interval by its own length. Custom = two date pickers (start, end), end ≥ start, both endpoints inclusive.
- **Calendar**: ISO weeks Monday–Sunday; calendar months; current presets include not-yet-passed days as zero.
- **Empty interval**: total shown as 0 with a "No expenses in this interval" message; no breakdown.

## 7. CSV export

- Always a **full export of all expenses** — the backup mechanism.
- Columns: `date,amount,category,note` — one header row.
  - `date`: ISO, `2026-11-12`
  - `amount`: major units, dot decimal, two places, `42.10`; no symbol, no thousands separators
  - `category`: full tree path, `Food/Groceries`
  - `note`: free text
- Dialect: strict RFC 4180 (comma; `"`-quoting on comma/quote/newline; CRLF), UTF-8.
- Filename `prudence-<export-date>.csv`; delivered via the system **share sheet**.
- No import in the MVP.

## 8. Definition of done for the MVP

The MVP is complete when: expenses can be added/edited/deleted with the §2 model; the category tree can be managed with the §2 semantics; the four §5 screens behave as specified; the report matches §6; CSV export matches §7; data survives app updates via Room auto-migrations.
