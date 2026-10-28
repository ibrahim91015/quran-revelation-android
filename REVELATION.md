# Quran Revelation

An independent GPL-3.0 fork of [Quran for Android](https://github.com/quran/quran_android),
based on upstream `main` at `cd6a99ed096ea1ec8dc1b1f99364244b05001bdd` (October 27, 2026).
The standalone release is `3.6.5-revelation.1`, targeting Android 16 (API 36).

## Install

Download the signed `Quran-Revelation-3.6.5-revelation.1.apk` from this fork's latest
GitHub release. Its application ID is `com.ibrahim91015.quran.revelation`, so it can
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

Use the Gradle wrapper, the JDK specified in `.github/workflows/.java-version`,
and Android SDK 37 for compilation. The app targets API 36 and supports API 24+.

```sh
./gradlew testMadaniDebug testReleaseUnitTest verifySqlDelightMigration -PdisableFirebase
./gradlew :app:lintMadaniRelease -PdisableFirebase
python3 tools/verify_revelation_sources.py
```

AndroidX FileProvider assumes POSIX path separators. Its bookmark-export tests
must run on Linux; the included GitHub workflow runs the complete regression
suite there. Other tests can also be run on Windows.

Release builds need your own signing key. Supply `STORE_FILE`, `STORE_PASSWORD`,
`KEY_ALIAS` and `KEY_PASSWORD` as Gradle project properties, preferably through
`ORG_GRADLE_PROJECT_...` environment variables. Never commit signing credentials.

```sh
./gradlew :app:assembleMadaniRelease -PdisableFirebase
./gradlew :app:connectedMadaniReleaseAndroidTest -PdisableFirebase -PinstrumentedBuildType=release
```

The device tests exercise the minified, signed release build on API 36 using
real audio: gapless MP3/Opus streaming, pause/resume and background playback,
downloaded gapless playback with networking disabled, and gapped streaming with
verse repeats. They seek to Al-‘Alaq's last verse and require playback to reach
Al-Qalam's second verse, proving that the next surah's audio actually plays.
The device needs Internet to prepare fixtures. Use a dedicated emulator because
the offline test temporarily disables and restores Wi-Fi/mobile data.

The live endpoint checker probes Al-‘Alaq and Al-Qalam in every advertised format,
plus every gapless timing archive, and compares all 114 surahs with Tanzil.
These checks establish availability at test time; audio hosting is external.

Keep the release signing key backed up securely: future updates must use the
same key. Published source remains under the upstream GPL-3.0 license.
