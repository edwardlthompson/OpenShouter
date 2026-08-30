# IzzyOnDroid / F-Droid Submission Recipe

## Package Details

- **Application ID:** `org.openshouter`
- **Source Repository:** `https://github.com/edwardlthompson/OpenShouter`
- **Release Channel:** GitHub Releases APKs (`app-release.apk`)
- **License:** MIT (Pure FOSS, ADR-0002)

## Reproducible Builds & Verification

- Built with `SOURCE_DATE_EPOCH` reproducible timestamp support.
- APK SHA256 checksums are attached to each GitHub Release.
- Verification command:
  ```bash
  sha256sum examples/android/app/build/outputs/apk/release/app-release.apk
  ```
