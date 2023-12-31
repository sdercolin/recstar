# RecStar

A UTAU style reclist recorder application for Desktop/iOS/Android.

This project is currently on an early development stage.

If you have any feedback, please join our [Discord Server](https://discord.gg/TyEcQ6P73y) and find the #recstar-prototype channel.

## Download

See the [releases page](https://github.com/sdercolin/recstar/releases) for the latest version.

### Desktop

- Windows: `~win64.zip`
- macOS (Intel): `~mac-x64.dmg`
- macOS (Apple Silicon): `~mac-arm64.dmg`
- Ubuntu: `~amd64.deb`

For other types of Linux os, please try building it by yourself.

### Android

#### APK

Attached in the release page.

#### Play Store

Get the latest version via Play Store with the following public test link:
https://play.google.com/store/apps/details?id=com.sdercolin.recstar

### iOS
Get the latest version via TestFlight with the following link:
https://testflight.apple.com/join/jBfhclHr

## Getting started with development

RecStar is built with [Compose Multiplatform](https://github.com/JetBrains/compose-jb).

See the [README of the project template](README-compose.md) for instructions on how to get started.

### Other recommended settings

1. Install the `Kotlin KDoc Formatter` plugin, and use the following settings:
   [![KDoc Formatter settings](readme_images/kdoc_settings.png)](readme_images/kdoc_settings.png)
2. Run `./gradlew addKtlintFormatGitPreCommitHook` once to add a pre-commit hook that will automatically format your
   code before committing.
3. If in string definition files (e.g. [StringsEnglish.kt](shared/src/commonMain/kotlin/ui/string/StringEnglish.kt)), if
   the formatter of your Android Studio is always turning the wildcard imports into single imports, adjust the settings
   to allow wildcard imports on package `ui.string`.

### Contributors

Logo designed by InochiPM.
