The Android app loads the complete generated `dist/` directory from
`feature/exercise-view/src/main/assets/model-review/`. The WebView serves only that
bundled directory under the synthetic origin `https://artemis-model.invalid`.
Network requests, navigation, content/file access and persistent storage are disabled.
Native feedback stays outside the diagram. The bridge receives no credentials.

Build the viewer as described in README.md, then copy all of `dist/` (including
licenses and manifest) to the assets directory above. Verify the resulting manifest
before committing. Test fixtures and development dependencies in this source kit
are outside Android's source sets and are not included in the APK.
