# BAMF Streaks

A floating **BAMF announcer button** that hovers over Mobile Legends: Bang Bang.
Tap it mid-game to open the streak pad, tap your streak, and the BAMF announcer
calls it — First Blood, Double Kill, Triple Kill, Maniac, Savage, Shut Down,
Legendary, and more.

It plays the calls *over* the game. It does not touch the game's files.

## Why this exists

Mobile Legends has no custom-announcer feature, and replacing the game's own
audio would require root plus trip anti-cheat file checks (ban risk). The BAMF
pack is the announcer from Heroes of Newerth (Legion vs Hellbourne gives it
away). This app is the honest route: the BAMF voice for your streaks with no
root, no game mods, and no ban risk.

Kill-streak mapping:

| MLBB streak      | BAMF clip        |
|------------------|------------------|
| First Blood      | `first_blood`    |
| Double Kill      | `double_kill`    |
| Triple Kill      | `triple_kill`    |
| Maniac (4)       | `quad_kill`      |
| Savage (5)       | `5_kills`        |
| Shut Down        | `smackdown`      |
| Legendary        | `immortal`       |

Bonus pad: Massacre, Genocide, Victory.

## Install (sideload)

1. Build or grab the APK.
2. Open it on the phone — allow **Install unknown apps** when asked.
3. Open **BAMF Streaks**, tap **SHOW BAMF BUTTON**.
4. Allow **Display over other apps** when asked.
5. Switch to MLBB. Drag the gold BAMF button wherever your thumb rests.
   Tap it for the streak pad; tap a streak to fire the call.

To remove: tap **REMOVE** in the app, or force-stop it — the notification has
a STOP action too.

## Build

**Android Studio / Gradle** (normal environments): open the project and run
`assembleDebug`. Standard AGP project, no exotic dependencies.

**Manual** (no Gradle): `./build.sh` with `ANDROID_HOME` set — uses
`aapt2`/`javac`/`d8`/`apksigner` directly. This is how the release APK was
built (the sandbox's Gradle daemon refused to handshake, so the toolchain
went manual).

**Audio**: the announcer MP3s are *not* committed here — they are game audio
from your own copy of the BAMF pack. Convert the OGGs to MP3, drop them in
`app/src/main/assets/` (see `PUT_YOUR_MP3S_HERE.txt`), then build.

Also in this repo: `soundboard/BAMF-Streaks-Soundboard.html` — a standalone
offline tap soundboard (same clips, no install needed).

## Permissions

- `SYSTEM_ALERT_WINDOW` — the floating button over the game.
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — keeps the
  button alive while you play.
- `POST_NOTIFICATIONS` — the persistent notification with STOP.

No network, no accounts, no analytics. The app is fully offline.

## License

Code is yours to do with as you wish. The BAMF audio belongs to its
respective game publisher — bring your own copy.
