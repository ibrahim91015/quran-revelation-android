# Quran Revelation for Android

An independent GPL-3.0 fork of [Quran for Android](https://github.com/quran/quran_android),
based on upstream `main` at `cd6a99ed096ea1ec8dc1b1f99364244b05001bdd`.
The standalone release is `3.6.5-revelation.1`, targeting Android 16 (API 36).

For the project overview and comparison with the original, see [README.md](README.md).
See the [release notes](docs/releases/v3.6.5-revelation.1.md) for shipped changes
and [ROADMAP.md](ROADMAP.md) for proposed improvements. This guide covers installation,
audio behavior, and developer verification.

## Install

Download the signed `Quran-Revelation-3.6.5-revelation.1.apk` from this fork's latest
[GitHub release](https://github.com/ibrahim91015/quran-revelation-android/releases/latest). Its application ID is `com.ibrahim91015.quran.revelation`, so it can
be installed alongside the original Quran.com application. It uses separate app
storage, provider authorities, launcher shortcuts, and a dedicated signing key.

Allow installation from the browser or file manager you use to open the APK.
On first launch, allow the usual Quran page download. Recitation audio is not
bundled into the APK.

## Revelation playback

Open **Revelation**, select a surah, then press Play. The complete surahs follow
[Tanzil's traditional revelation order](https://tanzil.net/docs/revelation_order):
Al-‘Alaq (96), Al-Qalam (68), Al-Muzzammil (73), and so on, ending at An-Nasr (110).
Canonical surah numbers and the verses within each surah stay unchanged.
Tanzil's sequence is based on each surah's first revealed verses.

Revelation sessions use downloaded audio when available and stream missing audio
as needed, including when crossing a surah boundary. This can use mobile data.
For offline playback, download the desired reciter's surahs beforehand. Gapless
reciters retain their timing databases and continuous playback within a surah;
network buffering can occur while loading a new surah. Verse repeats, bounded
range repeats, previous/next verse controls, pause/resume, playback speed, and
background playback use the same queue order. Playback stops after An-Nasr.
Sessions started from the ordinary Surahs tab retain canonical order.

## Reciters

The catalog preserves all 89 recitation entries from the supplied Quran.com
3.6.4 APK: 70 gapless entries and 44 with an Opus alternative. IDs, translated
name resources, MP3/Opus URLs, storage paths, and timing-database names are
retained. The reference catalog is recorded in
`app/src/test/resources/reference-reciters-3.6.4.json`.

Reference base APK SHA-256:
`3bd6083c4bea4503a724b297a17097c99ae271515ffc1aeb6fec31ab1b368384`.

This fork is independently signed. Quran.com account sync requires separately
registered OAuth configuration; the public upstream source does not include
production OAuth credentials. Local reading, bookmarks, downloads and audio do
not require account sync.

## Build and validation

Fork releases use independent `v0.0.x` tags and matching app version names, beginning
with `v0.0.3`. The internal Android version code starts at `4000003`, above the
legacy `3651002` code, so existing installations upgrade without uninstalling.
Keep increasing that code for every release. Upstream source versions belong in
the release provenance, not in the fork's app version or tag.

Use the Gradle wrapper, the JDK specified in `.github/workflows/.java-version`,
and Android SDK 37 for compilation. The app targets API 36 and supports API 24+.

```sh
./gradlew testMadaniDebug testReleaseUnitTest verifySqlDelightMigration -PdisableFirebase
./gradlew :app:lintMadaniRelease -PdisableFirebase
python3 tools/verify_revelation_sources.py
```

Run the complete regression suite on Linux, as the included GitHub workflow does.
AndroidX FileProvider bookmark-export tests assume POSIX path separators. The
copied-SQLite import fixture also fails under Windows Robolectric; that same test
passes on Linux, and the system-picker import is checked separately on Android.
Pure queue tests and the remaining targeted UI tests can be run on Windows.

Release builds need your own signing key. Supply `STORE_FILE`, `STORE_PASSWORD`,
`KEY_ALIAS` and `KEY_PASSWORD` as Gradle project properties, preferably through
`ORG_GRADLE_PROJECT_...` environment variables. Never commit signing credentials.

```sh
./gradlew :app:assembleMadaniRelease -PdisableFirebase
./gradlew :app:connectedMadaniDebugAndroidTest -PdisableFirebase
python3 tools/check_device_test_results.py
```

The device tests exercise the audio service in the standard Android test build on API 36 using
real audio: gapless MP3/Opus streaming, pause/resume and background playback,
downloaded gapless playback with networking disabled, and gapped streaming with
verse repeats. They seek to Al-‘Alaq's last verse and require playback to reach
Al-Qalam's second verse, proving that the next surah's audio actually plays.
The signed release APK also needs separate installation and playback checks.
The suite also covers picker recreation while paused, notification deep links,
all accent choices with the original app installed, and every verse plus the
audio tail of Al-Kawthar with a deliberately early timing end marker. Screen-off
playback is included. Install the original `com.quran.labs.androidquran` package
on the dedicated test emulator before running the coexistence test.
Navigation tests additionally cover background return to a paused ayah, independent
picker offsets, actual long-press tab dragging, and cancel/confirm replacement of
an automatically paused session. The policy and UI unit tests cover manual pauses
and the default-off warning preference.
The device needs Internet to prepare fixtures. Use a dedicated emulator because
the offline test temporarily disables and restores Wi-Fi/mobile data.

The live endpoint checker probes Al-‘Alaq and Al-Qalam in every advertised format,
plus every gapless timing archive, and compares all 114 surahs with Tanzil.
These checks establish availability at test time; audio hosting is external.

Keep the release signing key backed up securely: future updates must use the
same key. Published source remains under the upstream GPL-3.0 license.
