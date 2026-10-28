# Quran Revelation for Android

Read and listen to complete surahs in **traditional revelation order**, with the familiar Quran for Android reading experience.

This is an independent, unofficial fork of [Quran for Android](https://github.com/quran/quran_android). Its main addition is a **Revelation** tab with audio that follows the same surah sequence. Start Al-‘Alaq (96), and playback continues automatically to Al-Qalam (68), then Al-Muzzammil (73), through to An-Nasr (110).

[![Verification](https://github.com/ibrahim91015/quran-revelation-android/actions/workflows/revelation-verification.yml/badge.svg?branch=main)](https://github.com/ibrahim91015/quran-revelation-android/actions/workflows/revelation-verification.yml)
[![Latest release](https://img.shields.io/github/v/release/ibrahim91015/quran-revelation-android?display_name=tag&sort=date)](https://github.com/ibrahim91015/quran-revelation-android/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/ibrahim91015/quran-revelation-android/total)](https://github.com/ibrahim91015/quran-revelation-android/releases)

**[Download the APK](https://github.com/ibrahim91015/quran-revelation-android/releases/latest)** · **[Release notes](docs/releases/v0.0.3.md)** · **[Roadmap and ideas](ROADMAP.md)** · **[Build and testing guide](REVELATION.md)**

## What is different from the original?

| Area | Original Quran for Android | This fork |
| --- | --- | --- |
| Home navigation | Surahs, Juz’, and Bookmarks | Adds a Revelation tab with all 114 surahs in Tanzil’s traditional sequence. |
| Audio progression | Canonical surah order | Revelation sessions follow revelation order; ordinary Surahs-tab sessions keep canonical order. |
| Quran text and numbering | Canonical surah numbers and normal verse order | Preserves the same numbers, verse order, and mushaf pages. The extra index changes which surah comes next. |
| Installation | `com.quran.labs.androidquran` | `com.ibrahim91015.quran.revelation`, displayed as **Quran Revelation**, with separate storage and signing. Both apps can be installed together. |
| Listening controls | Upstream playback interface | Keeps audio while browsing surahs, adds a persistent now-playing card, current-ayah notification links, and optional sajdah tones. |
| Appearance | Upstream appearance options | Adds Blue, Emerald, Violet, and Amber accents across Material 3 native/Compose controls. |
| Tab navigation | Upstream index behavior | Independent saved offsets, tap-to-top, recitation markers/double taps, and drag-to-reorder tabs. |
| Browsing while listening | Upstream audio tracking | Pause and browse another location without losing your place; confirm before replacing an automatically paused recitation. |
| Distribution | The upstream project’s official channels | Signed standalone APKs from this fork’s GitHub releases. |

The sequence comes from [Tanzil’s revelation-order table](https://tanzil.net/docs/revelation_order), which orders surahs by their **first revealed verses**. It does not claim a chronology for every individual ayah.

The reciter catalog retains all **89 recitation entries** from the supplied official Quran.com 3.6.4 APK, including **70 gapless entries** and **44 Opus alternatives**. These are recitation variants, so the counts are not counts of distinct people. Audio files are downloaded or streamed rather than bundled into the APK.

## Install and listen

1. Open the [latest release](https://github.com/ibrahim91015/quran-revelation-android/releases/latest) and download its signed `.apk` file. Checksums and validation reports are attached there too.
2. Open the APK on your Android device and allow installation from that browser or file manager. On first launch, let the app download the required Quran pages.
3. Open **Revelation**, select a surah and reciter, and press Play. Playback follows revelation order and stops after An-Nasr.

The current release, **0.0.3**, targets **Android 16 / API 36**, with a minimum of **Android 7.0 / API 24**. Install it over either legacy revelation release to keep this fork's data. Fork versions now use **v0.0.x**, independently of the upstream **3.6.5** source base; the higher internal Android version code preserves upgrade compatibility.

Reopening with a playing or paused recitation returns to its current ayah. Without audio, a warm return keeps your tab and scroll position; a fresh start uses your saved first tab. Tap a tab to go to its top, double-tap the recitation's originating tab to find its green row, or hold and drag tabs to change their order. [Audio handoff and navigation details](docs/releases/v0.0.3.md).

Downloaded audio is preferred. Revelation sessions stream missing files as needed, including across surah boundaries, and **can use mobile data**. Download the reciter’s desired surahs ahead of time for offline listening. Gapless reciters provide continuous audio within each surah; opening a new streamed surah can involve buffering.

## Validation and current limits

The [latest validation report](https://github.com/ibrahim91015/quran-revelation-android/releases/tag/v0.0.3) covers playback, tab state and gestures, background/notification navigation, audio handoffs, and the existing touch/import/theme/coexistence checks.

Version **0.0.3** passed **606 Linux regression tests**, **10 Android 16 device tests**, **336 live endpoint checks**, and release lint with **zero errors**. The release includes the results, checksums, and platform limitations.

The first release established this baseline:

- **588** passing regression tests and database migration checks.
- **4** passing Android 16 playback tests covering gapless MP3/Opus, pause/resume, background playback, offline audio, and gapped audio with repeats.
- **336** passing live audio/timing endpoint checks and a full comparison of the 114-surah sequence with Tanzil.
- Release lint with **zero errors**, APK signature verification, and 16 KiB zip-alignment checks.
- A separate UI playback check of the signed release APK that automatically advanced from Al-‘Alaq to Al-Qalam.

See the [first release's attached evidence](https://github.com/ibrahim91015/quran-revelation-android/releases/tag/v3.6.5-revelation.1). Automated device testing uses an **Android 16 emulator**. The user confirmed physical-phone installation, streaming, and downloaded playback for revelation.1; each update still benefits from fresh phone testing. Nonblocking lint warnings are recorded in the attached reports. External audio-host availability can change after a test run.

Advanced Settings can detect the original app and copy audio/translation assets from an accessible exported folder. Android's private-storage restrictions prevent silently sharing another app's downloads. See the [import instructions](docs/releases/v3.6.5-revelation.2.md#importing-existing-downloads).

Quran.com account sync requires OAuth registration for this independently signed app. Local reading, bookmarks, downloads, and audio work without an account.

## Improvements and future features

The next useful areas to explore are physical-device validation, downloading a complete revelation-order listening queue, clearer “now / next” controls, and better recovery from interrupted streams. Other ideas include chronological reading navigation, memorization session presets, accessibility and translation improvements, and a Revelation view in Android Auto.

These are **proposals**, with priorities and acceptance criteria in [ROADMAP.md](ROADMAP.md). [Open an issue](https://github.com/ibrahim91015/quran-revelation-android/issues/new) to suggest a feature or describe a problem. Shipped changes are recorded in [CHANGELOG.md](CHANGELOG.md).

## Build this fork

```sh
git clone https://github.com/ibrahim91015/quran-revelation-android.git
cd quran-revelation-android
./gradlew :app:assembleMadaniDebug -PdisableFirebase
```

Use the JDK version specified in [`.github/workflows/.java-version`](.github/workflows/.java-version) and the SDK requirements in the [build and testing guide](REVELATION.md#build-and-validation). Release signing and device-test instructions are in that guide as well.

The upstream acknowledgments and reuse notices follow.

## Credits

* madani images from [quran images project](https://github.com/quran/quran.com-images) on github.
* qaloon images used with permission of Nous Memes Editions Et Diffusion (Tunisia).
* naskh images used with permission of SHL Info Systems.
* translation, tafsir and Arabic data come from [quranenc](https://quranenc.com) and [King Saud University](https://quran.ksu.edu.sa). a small number of translations also come from [tanzil](http://tanzil.net).

## Contributing

For this fork’s bugs and feature requests, use [this repository’s issues](https://github.com/ibrahim91015/quran-revelation-android/issues) and review the [roadmap](ROADMAP.md) before beginning a larger change. For contributions to the original application, see upstream’s [PRs Welcome](https://github.com/quran/quran_android/issues?q=is%3Aissue+is%3Aopen+label%3A%22PRs+Welcome%22) label.

Use [`quran_android-code_style.xml`](quran_android-code_style.xml) for Android Studio / IntelliJ code styles. Import it by copying it to the Android Studio/IntelliJ IDEA codestyles folder. For Android Studio, that folder is located at `~/.AndroidStudio[Version]/config/codestyles` (the root folder name may differ depending on the host machine and Android Studio version, but the rest of the path should be same). After copying the `quran_android-code_style.xml`, go to Code Style preferences screen and choose `quran_android-code_style` from Code Style Schemes.

Please set your Android studio kotlin code style based on [Kotlin Coding Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html). You can configure it from menu Settings | Editor | Code Style | Kotlin, click on "Set from…" link in the upper right corner, and select "Predefined style / Kotlin style guide" from the menu.

May Allah reward all the awesome [Contributors and Translators](https://github.com/quran/quran_android/blob/main/CONTRIBUTORS.md).


## Setup

### Command Line

You can build the fork from the command line by running `./gradlew :app:assembleMadaniDebug -PdisableFirebase`. See [REVELATION.md](REVELATION.md#build-and-validation) for the toolchain, signing, and verification steps.

### Android Studio / IntelliJ

Choose "Import Project," and choose the `build.gradle` file from the top level directory. Under "Build Variants" (a tab on the left side), choose "madaniDebug."

## Using Quran for Android code in other projects

The intention behind open sourcing Quran for Android is two fold - first, to allow developers to help contribute to the app, thus speeding up the development of new features and ideas. Second, to give back to the community and serve as a code reference.

Quran for Android costs money to run - all the data (pages, audio files, and translations) are hosted on servers that people volunteer their money to pay for every month. Moreover, the data itself is the work of various scholars, organizations, or reciters, many of whom provide this data free for usage for the benefit of the ummah.

Therefore, people planning on taking this project and profiting from it (by way of ads, in app purchases, etc) are in fact stealing from the work of the contributors of this project, and from the people who volunteer to pay for the servers (since they increase the bandwidth costs on them instead of covering them themselves).

Please keep use of this code for non-profit purposes only. Also, please note that the project is under the GPL 3 license, which requires that modifications to this code be open sourced as well. Please note that the data is licensed under the various licenses of the data's authors (typically, this is [CC BY-NC-ND](https://creativecommons.org/licenses/by-nc-nd/2.0/), but may differ depending on the source of the data).


## Open Source Projects Used

* [AndroidX](https://developer.android.com/jetpack/androidx/)
* [Kotlin](https://kotlinlang.org)
* [Material Design Components](https://github.com/material-components/material-components-android)
* [AndroidSlidingUpPanel](https://github.com/umano/AndroidSlidingUpPanel)
* [OkHttp](https://github.com/square/okhttp)
* [RxJava](https://github.com/ReactiveX/RxJava)
* [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [Moshi](https://github.com/square/moshi)
* Metro (`dev.zacsweers.metro`) for dependency injection.
* [Timber](https://github.com/JakeWharton/timber)
* [dnsjava](http://dnsjava.org)
* [NumberPicker](https://github.com/ShawnLin013/NumberPicker)
