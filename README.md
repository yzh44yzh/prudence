# Prudence

A personal expense tracker for Android. Single user, fully offline — your data never leaves the device.

- **Record expenses** — amount, date, category, optional note
- **Hierarchical categories** — a user-editable tree of arbitrary depth; deleting a category safely reassigns its expenses to its parent
- **Reports** — totals and drill-down category breakdowns for this week, this month, or any custom interval
- **CSV export** — full backup (RFC 4180) via the share sheet

## Tech

Kotlin, Jetpack Compose (Material 3), Room, Navigation 3, DataStore, manual DI. Single module, unidirectional data flow. minSdk 26.

## Build

```sh
./gradlew :app:assembleDebug      # build the APK
./gradlew :app:testDebugUnitTest  # run the unit tests
```

## Docs

- [SPEC.md](SPEC.md) — the MVP specification
- [GLOSSARY.md](GLOSSARY.md) — domain vocabulary
- [docs/agents/](docs/agents/) — agent workflow configuration
