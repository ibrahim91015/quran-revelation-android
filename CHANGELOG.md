# Changelog

This file tracks changes specific to **Quran Revelation for Android**. The original application’s development history is available in [upstream Quran for Android](https://github.com/quran/quran_android).

## [3.6.5-revelation.2] — 2026-10-28

- Keep playback and paused notifications when returning to the picker; add a now-playing card with pause/resume.
- Open the current playing ayah from notifications and provide reciter/order metadata.
- Wait for complete gapless files before advancing; handle delayed final-verse tracking.
- Fix mushaf margin hit detection and scaled/padded coordinates; smooth distant revelation jumps.
- Add four configurable accent palettes, Material 3 activity themes/controls, and optional sajdah tones.
- Fix the release-only Advanced Settings bookmark-enum reflection crash.
- Detect the original app and import accessible copies of audio/translation folders without replacing existing files.

See the [release notes](docs/releases/v3.6.5-revelation.2.md) for test evidence and import limitations.

## [3.6.5-revelation.1] — 2026-10-28

The first signed release of this fork with automatic revelation-order audio progression, based on upstream `main` at `cd6a99ed096ea1ec8dc1b1f99364244b05001bdd`.

### Added

- A Revelation tab listing all 114 surahs in Tanzil’s traditional revelation order.
- Audio progression in the selected tab’s order, including Al-‘Alaq → Al-Qalam → Al-Muzzammil, ending at An-Nasr.
- The complete 89-entry reciter catalog from the supplied official Quran.com 3.6.4 APK, including 70 gapless entries and 44 Opus alternatives.
- A separately signed app identity, `com.ibrahim91015.quran.revelation`, for installation alongside the original.
- Automated order, reciter-catalog, streaming, offline, and Android 16 playback checks, with evidence attached to the release.

### Changed

- Updated the fork’s code to the upstream snapshot used for this release.
- Prefer downloaded audio while retaining streaming fallback for later surahs in a Revelation session.
- Keep canonical navigation and audio order for sessions started from the regular Surahs tab.
- Adjust launcher shortcuts, storage, and provider authorities for the clone’s identity.

### Validation

- 588 regression tests passed, plus database migration verification.
- Four Android 16 emulator playback tests passed.
- All 336 live audio/timing checks passed; all 114 revelation positions matched Tanzil.
- Release lint had zero errors; signature and 16 KiB alignment checks passed.
- The signed APK passed a separate UI playback check from Al-‘Alaq into Al-Qalam.

See the [full release notes](docs/releases/v3.6.5-revelation.1.md) for installation details, test scope, checksums, and known limitations. Proposed work belongs in the [roadmap](ROADMAP.md).

[3.6.5-revelation.1]: https://github.com/ibrahim91015/quran-revelation-android/releases/tag/v3.6.5-revelation.1
[3.6.5-revelation.2]: https://github.com/ibrahim91015/quran-revelation-android/releases/tag/v3.6.5-revelation.2
