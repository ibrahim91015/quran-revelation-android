# Roadmap and feature ideas

These are proposed improvements for **Quran Revelation for Android**. They are a starting point for discussion, with no promised delivery dates. The current release is documented in [CHANGELOG.md](CHANGELOG.md) and the [release notes](docs/releases/v0.0.3.md).

Version 0.0.3 adds independent tab positions, tab reordering/gestures, source-specific playback markers, background return to the playing ayah, and protected handoffs when browsing during recitation. Fork version numbers are now independent of upstream.

Revelation.2 adds the picker now-playing card, paused notification retention, current-ayah links, four accent choices, sajdah cues, safer mushaf margins, and accessible-folder imports. Physical installation, streaming, and downloaded playback were confirmed by the user for revelation.1. The remaining work below extends that foundation.

The aim is to make reading and listening in revelation order dependable and easy to understand, while preserving the Quran text, canonical numbering, verse order, upstream attribution, and existing installation identity.

## Suggested next priorities

| Priority | Improvement | Benefit |
| --- | --- | --- |
| 1 | Physical-device playback validation | Establish confidence beyond the Android 16 emulator used for the first release. |
| 2 | Revelation-order download queue | Prepare a whole listening session for offline use without finding and downloading each surah separately. |
| 3 | Visible listening queue and surah controls | Make the current order, upcoming surah, and difference between verse and surah navigation clear. |
| 4 | Stream prefetch and recovery | Reduce waiting between surahs and recover more reliably from weak or interrupted connections. |

### Physical-device validation

Test the signed APK on physical Android 16 ARM64 phones, including a 16 KiB page-size environment where available. Cover Bluetooth/headset controls, calls and audio-focus interruptions, screen-off playback, battery restrictions, notification permissions, and installation beside the original app. Include an older Android version within the supported range.

**Acceptance:** record the device/build details and results for streaming, downloaded audio, pause/resume, repeats, and the complete Al-‘Alaq → Al-Qalam transition. Keep emulator and physical-device results distinct in future release notes.

### Revelation-order downloads

Let the reader choose a reciter, starting surah, and number of surahs or an ending revelation position. Show total progress and an estimated download size where the server provides it. Support cancel/resume, Wi-Fi-only downloads, and a clear indication of which surahs are ready offline.

**Acceptance:** a prepared queue plays in airplane mode, crosses every selected surah boundary correctly, and keeps existing reciter IDs and downloaded files usable.

### Queue visibility and navigation

Show “Playing in revelation order,” the current revelation position, and the next surah. Offer separate next/previous **surah** actions alongside the existing verse controls. Let the reader inspect the queue before starting and choose where the session should end.

**Acceptance:** Al-‘Alaq displays Al-Qalam as next; ordinary Surahs-tab sessions remain canonical; repeat settings and the final An-Nasr boundary stay correct.

### Prefetch, retries, and recovery

Explore preparing the next surah before the current one finishes. Add understandable buffering/retry states, bounded retries, and recovery after Wi-Fi/mobile-data changes. Persist the reciter, playback order, verse, and repeat settings when a session can be restored safely.

**Acceptance:** test real Media3 playback with slow networks, network loss, recovery, and missing files. Measure transition delays before promising seamless playback between surahs. Preserve the existing gapless timing behavior within each surah.

## Further ideas to explore

| Area | Possible feature | Consideration |
| --- | --- | --- |
| Reading | Next/previous surah navigation in revelation order and a clear route back to the revelation index. | Keep canonical mushaf pages and verse numbering intact. |
| Memorization | Named practice sessions with reciter, selected surahs, repeat presets, and a saved stopping point. | Build on the current repeat and bookmark behavior. |
| Reading goals | Optional local goals, progress, and reminders for a chosen revelation-order sequence. | Make progress tracking optional and explain what is stored on the device. |
| Accessibility | Screen-reader labels that distinguish canonical surah number from revelation position; larger touch targets. | Include the reciter picker, playback controls, and custom ayah toolbar in testing. |
| Languages and layouts | Translate the fork’s added labels and test the four-tab layout in RTL, large text, tablets, and foldables. | Many layouts already come from upstream; focus on the fork’s additions and regressions. |
| Android Auto | A Revelation browsing category and appropriate queue/skip behavior for car and headset controls. | Validate media-session behavior on actual supported devices before release. |
| Reciter maintenance | Automated comparison with a documented reference catalog and checks for audio/timing updates. | Preserve stable IDs and distinguish recitation variants from distinct reciters. |
| Diagnostics | An optional export of app version, reciter ID, selected audio format, and recent playback errors. | Let readers review the contents and exclude personal reading history by default. |
| Account sync | Independently configured OAuth sign-in for supported bookmark/progress sync. | Requires registration for this app’s identity; keep local use available without an account. |
| Releases | Repeatable signed release builds, clearer upgrade instructions, and scheduled upstream compatibility reviews. | Keep signing credentials private and preserve the key needed to update installed copies. |

## Propose or choose an improvement

[Open an issue](https://github.com/ibrahim91015/quran-revelation-android/issues/new) with the problem you want to solve, how the feature should work, and whether it matters for offline use, streaming, or both. For audio problems, include the app version, reciter, surah/ayah, and Android version when possible.

Before implementing a larger idea, agree on its behavior and a small set of acceptance tests. Move an item into the changelog only after it ships.
