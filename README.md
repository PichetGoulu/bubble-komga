# Bubble-Komga

A fork of [Bubble](https://github.com/nkanaev/bubble) that support [Komga](https://github.com/gotson/komga) as comics source

## Supported Komga features

- Browse the series and thumbnails of one library (no multi-library support)
- Browse the books, thumbnails and read status of a selected series
- View the pages of a selected book
- Go to the next/previous book from the last/first page of a book
- Update the page read progress of a book

## Notable changes from [Bubble](https://github.com/nkanaev/bubble)

- Drop support for local files and archives
- Add support for [Komga](https://github.com/gotson/komga) server using [Retrofit2](https://square.github.io/retrofit/), [RxJava](https://github.com/ReactiveX/RxJava) and [Picasso](https://square.github.io/picasso/)
- Use the read progress feature of Komga (see [Komga issue #25](https://github.com/gotson/komga/issues/25)) instead of a local `sqlite` database
- Convert to AndroidX, update Gradle
- Keep `minSdkVersion 16`
- Refactor all `Fragment`

# Development

## Plan for future version

This code is provided as-is.

My plan for this repo is to provide fix for bugs should some be reported and maintain compatibility with the API of future version of Komga if needed, but I do not plan to add any additional feature for now.

My goal with this fork is:

- To support my own personnal use of Komga
- To provide ideas or a possible baseline for a Komga-specific Android App (see [Komga issue #36](https://github.com/gotson/komga/issues/36))

Feel free to fork it for your own development!

## Build/Install the debug APK

- Build using `./gradlew assembleDebug`
- Push the debug APK to your device using `adb install -r -t app/build/outputs/apk/debug/app-debug.apk`

## Build the release APK

- Build using `./gradlew assembleRelease`

## TODO

- SettingsFragment is ugly but works
- PageImageView is mostly untouched and may need some additional cleanup

# License

Source code is available under the MIT license. See the LICENSE for more info.
