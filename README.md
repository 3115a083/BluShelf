# BluShelf

BluShelf is a local-first Android app for organizing physical video and audio media, including Blu-ray, UHD Blu-ray, DVD, VHS, CD and vinyl.

## Features

- Virtual shelf designed around readable physical-media spines
- Video and audio shelves with multiple editions and owned copies
- Barcode-assisted cataloging and batch scanning
- Metadata and sleeve artwork with editable results
- Search, filters, sorting and fast shelf navigation
- Custom fields, tags, collections and structured physical locations
- Wishlist, Watchlist and listening list
- Swipe mode with a temporary shortlist
- Ratings and manual viewing/listening history
- Optional links to digital copies on Plex, Jellyfin and Navidrome/OpenSubsonic
- CSV and JSON import/export
- Encrypted backups and optional WebDAV synchronization
- Material 3 UI with light/dark themes and adaptive layouts
- German and English localization
- No ads, analytics or telemetry

## Status

BluShelf is under active development. The Android application is built with Kotlin and Jetpack Compose. The current early test version supports a local CSV import and a basic media list. Features listed above describe the product direction and are not all implemented yet.

## Build

Install JDK 17 and Android SDK 36, then run:

```shell
./gradlew test lintDebug assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Early CSV format

Use UTF-8 CSV with a header row. `title`, `kind`, and `format` are required. `kind` must be `VIDEO` or `AUDIO`. Optional columns currently supported are `original_title`, `year`, `barcode`, `location`, `rating`, `favorite`, `played`, and `notes`. Ratings range from 0.5 to 5 in half-star steps. Boolean values accept `true`, `false`, `yes`, `no`, `ja`, `nein`, `1`, and `0`.

## Privacy

BluShelf is designed to work locally without an account. Network access is used only for features configured or invoked by the user, such as metadata lookup, media-server connections and synchronization. Credentials are not included in normal collection exports.
