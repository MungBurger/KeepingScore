# Changelog

## [Unreleased]
### Changed
- Updated Java version from 8 to 21 in build.gradle.kts to match the installed JDK
- Added custom task to check for outdated dependencies
- Updated Android Gradle Plugin from 8.11.1 to 8.3.0
- Updated Gradle wrapper to version 8.13
- Added android.suppressUnsupportedCompileSdk=35 to gradle.properties
- Updated dependencies to their latest available versions:
  - androidx.fragment:fragment:1.6.2
  - com.google.code.gson:gson:2.10.1
  - androidx.media3:media3-common:1.2.1
  - androidx.room:room-runtime:2.6.1
  - androidx.room:room-compiler:2.6.1

### Fixed
- Fixed AndroidManifest.xml by properly placing the screenOrientation attribute inside the activity tag
- Added @Ignore annotation to GameStats parameterized constructor to resolve Room warning
- Successfully built project with Gradle in VS Code