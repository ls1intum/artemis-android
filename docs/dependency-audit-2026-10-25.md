# Dependency audit — 25 October 2026

Audited against `develop` commit `bc8e724c455ea6d1c6c6bf6ea5f641d2d1c6a81b` after #802. This covers every library and plugin in `gradle/libs.versions.toml`, versions in build logic and buildSrc, the wrapper, SDK/JDK, GitHub Actions and Docker declarations. Publisher Maven metadata was checked for all pinned catalog coordinates and all 503 external module coordinates found across 60 production and unit-test runtime classpaths. Project components and synthetic versions such as Guava's empty `listenablefuture` artifact are not release candidates.

## Updates

| Dependency | Before | Updated version |
| --- | --- | --- |
| Guava | 32.0.1-jre in the app; 33.6.0-jre in some tests | 33.7.2-jre |
| Objenesis | 2.6 forced in all library configurations | 3.6 as a test dependency constraint |
| Google Play services basement | Constraint 18.0.2; resolved 18.9.0 | 18.12.0 |
| Protobuf Java | Constraint 3.25 | 4.36.2; remains a constraint, not an added dependency |
| Ktor IO / HTTP CIO | Constraint 3.1.1 | Shared Ktor catalog version 3.6.0 |
| Docker JDK | OpenJDK 17.0.12 | Temurin 25.0.5+8, matching CI; archive SHA-256 verified |
| Docker Android command-line tools | 16.0 | 23.0 |
| Docker Android platform / build tools | API 34; build tools 30.0.3 and 33.0.1 | API 37.0 and build tools 37.0.0, matching the catalog |

The old security constraints were attached to each library's resolvable configurations. Those constraints were not published to application consumers, leaving the packaged app on Guava 32.0.1. Both application and library convention plugins now declare the same catalog-backed constraints on `implementation` and `testImplementation`. The resolved app graph selects Guava 33.7.2-jre, failureaccess 1.0.3 and Play services basement 18.12.0. All test classpaths that contain Objenesis select 3.6. The existing Bouncy Castle and Espresso floors and Firebase measurement-connector exclusion are preserved.

[Guava 33.7.2](https://github.com/google/guava/releases/tag/v33.7.2) fixes CVE-2026-102554. [Objenesis 3.6](https://github.com/easymock/objenesis/releases/tag/3.6) fixes parent-classloader caching. [Protobuf's Java compatibility guarantee](https://protobuf.dev/support/cross-version-runtime-guarantee/) supports older 3.x generated code with the 4.x runtime; [4.36.x is actively supported](https://protobuf.dev/support/version-support/). Protobuf is absent from the audited runtime graphs, and the constraint does not introduce it.

The standalone Docker dependency build also needs to register the Compose compiler plugin used by the shared conventions, read the catalog through `VersionCatalogsExtension`, and import BOMs as platforms. Without these changes it fails before resolution or silently leaves Firebase Messaging unresolved. Its dependency report now completes with no `FAILED` entries across configurations.

## Catalog review

Every artifact sharing a version reference was checked, including the deprecated Accompanist artifacts, each Koin/Coil/Ktor/Krossbow module and the AndroidX test libraries. Versionless Compose and Firebase artifacts remain managed by their latest stable BOMs. Other transitive dependencies remain controlled by their owning libraries/BOMs; changing every transitive version independently would bypass those compatibility choices.

| Version reference or coordinate | Stable version | Decision |
| --- | --- | --- |
| `accompanist` | 0.36.0 | Current stable; retained |
| `accompanistPermissions` | 0.37.3 | Current stable; retained |
| `androidDesugarJdkLibs` | 2.1.5 | Current stable; retained |
| `androidGradlePlugin` | 9.4.1 | Current stable; retained |
| `androidxActivity` | 1.13.0 | Current stable; retained |
| `androidxAppCompat` | 1.8.0 | Current stable; retained |
| `androidxComposeBom` | 2026.09.00 | Current stable; retained |
| `androidxCredentials` | 1.6.0 | Current stable; retained |
| `androidxDataStore` | 1.3.0 | Current stable; retained |
| `androidxLifecycle` | 2.11.0 | Current stable; retained |
| `androidxNavigation` | 2.10.2 | Current stable; retained |
| `androidxPaging` | 3.5.3 | Current stable; retained |
| `androidxPagingCompose` | 3.5.3 | Current stable; retained |
| `coil` | 3.6.3 | Current stable; retained |
| `coil2` | 2.7.0 | Current stable; retained |
| `kotlin` | 2.4.21 | Current stable; retained |
| `kotlinxCoroutines` | 1.11.0 | Current stable; retained |
| `kotlinxDatetime` | 0.8.0-0.6.x-compat | Current stable; retained |
| `kotlinxSerializationJson` | 1.12.0 | Current stable; retained |
| `koin` | 4.2.2 | Current stable; retained |
| `kover` | 0.9.11 | Current stable; retained |
| `krop` | 0.2.0 | Current stable; retained |
| `krossbow` | 10.1.0 | Current stable; retained |
| `ktor` | 3.6.0 | Current stable; retained |
| `markwon` | 4.6.2 | Current stable; retained |
| `mockk` | 1.14.11 | Current stable; retained |
| `ossLicensesPlugin` | 0.13.0 | Current stable; retained |
| `placeholderMaterial` | 2.0.0 | Current stable; retained |
| `room` | 2.8.5 | Current stable; retained |
| `sentry-android` | 8.61.1 | Current stable; retained |
| `work` | 2.12.0 | Current stable; retained |
| `browser` | 1.10.0 | Current stable; retained |
| `androidx.security:security-crypto` | 1.1.0 | Current stable; retained |
| `com.auth0.android:jwtdecode` | 2.1.0 | Current stable; retained |
| `org.bouncycastle:bcprov-jdk18on` | 1.86 | Current stable; retained |
| `androidx.test.espresso:espresso-core` | 3.7.0 | Current stable; retained |
| `de.drick.compose:edge-to-edge-preview` | 0.10.1 | Current stable; retained |
| `com.google.firebase:firebase-bom` | 35.1.0 | Current stable; retained |
| `com.google.android.gms:play-services-basement` | 18.12.0 | Updated constraint; see above |
| `com.google.guava:guava` | 33.7.2-jre | Updated constraint; see above |
| `org.objenesis:objenesis` | 3.6 | Updated constraint; see above |
| `com.google.android.gms:play-services-oss-licenses` | 17.5.2 | Current stable; retained |
| `com.google.protobuf:protobuf-java` | 4.36.2 | Updated constraint; see above |
| `org.robolectric:robolectric` | 4.17 | Current stable; retained |
| `me.onebone:toolbar-compose` | 2.3.5 | Current stable; retained |
| `com.google.firebase.appdistribution` | 5.3.1 | Current stable; retained |
| `com.google.devtools.ksp` | 2.3.12 | Current stable; retained |
| `com.google.gms.google-services` | 4.5.0 | Current stable; retained |

Kotlin remains aligned between the catalog and both buildSrc plugin declarations. Accompanist 0.36.0 is the final release for the deprecated artifacts; Permissions is independently current at 0.37.3. Coil 2.7.0 remains for Markwon's Coil 2 integration; Coil 3 is independently current at 3.6.3. The datetime compatibility artifact is retained to preserve its public Instant API. Prerelease AGP, Kotlin, Room and other library versions are excluded.

## Tooling and infrastructure review

| Tool | Decision |
| --- | --- |
| Gradle wrapper 9.8.2 | Current stable, verified against the [Gradle versions API](https://services.gradle.org/versions/current) |
| AGP 9.4.1 / Kotlin 2.4.21 / KSP 2.3.12 | Current stable; retain compatible toolchain |
| JaCoCo 0.8.15 / Kover 0.9.11 / Sentry plugin 6.24.0 | Current stable |
| Temurin 25.0.5+8 | Current JDK 25 LTS update, retained in all four workflows and used in Docker |
| compileSdk / targetSdk 37; minSdk 30 | Retained; newer minor SDK platforms do not require changing the application's API contract |
| Android build tools 37.0.0 / command-line tools 23.0 | Current stable, verified against Google's SDK repository metadata |
| `thyrlian/android-sdk:10.0` | Latest published base image; its old Java/SDK tools are upgraded in the Dockerfile |
| Artemis / PostgreSQL E2E stack | Intentionally follows Artemis develop/latest and its database version; no older fixed version to bump |
| `ellerbrock/alpine-bash-curl-ssl:latest` | Already floating at its publisher's latest image; no newer published version selected |

All ten action repositories were checked against GitHub release metadata and tag commits. `checkout@v7`, `setup-java@v6`, `upload-artifact@v7`, `download-artifact@v8`, `setup-android@v4`, `gradle/actions@v6`, `android-version-actions@v1.2.3` and `upload-google-play@v1` already resolve to their newest stable release commits. `test-reporter@v3` and `sign-android-release@v1` contain additional upstream commits beyond their numbered releases; switching to those releases would roll them back. Existing action references are retained.

## Validation

Local validation passed on the pinned Temurin 25.0.5+8: release assembly, 202 unit tests (46 suites), and 113 E2E tests (35 suites), with no failures, errors or skipped tests. E2E used a fresh isolated Artemis/PostgreSQL stack. Graph assertions passed across all 60 audited classpaths. The Docker toolchain stage built and its JDK, command-line tools and SDK files were verified in a running container. CI results and additional Docker checks are recorded in the pull request. The unchanged post-#802 E2E workflow [40169463486](https://github.com/ls1intum/artemis-android/actions/runs/40169463486) passed on the audited base. #802 changes communication topics, not the quiz connection test that failed intermittently on the previous develop run.
