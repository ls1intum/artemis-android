# ------------------------------ LICENSE AGREEMENT NOTICE ------------------------------
# By using this dockerfile, e.g. by building it or executed it's derived
# images you agree the terms of the following license agreements:
# - Android Software Development Kit License Agreement (https://developer.android.com/studio/terms.html)
# - Android SDK Preview License Agreement (https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/AndroidSDKPreviewLicenseAgreement)
# - Intel Android Extra License (https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/IntelAndroidExtraLicense)
# ------------------------------ LICENSE AGREEMENT NOTICE ------------------------------


FROM thyrlian/android-sdk:10.0 AS android-toolchain

# The SDK image only publishes linux/amd64 and still bundles Java 17. Use the same
# Temurin build as CI; Gradle and the convention plugins require Java 25.
ADD --checksum=sha256:68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb \
    https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.5%2B8/OpenJDK25U-jdk_x64_linux_hotspot_25.0.5_8.tar.gz /tmp/temurin.tar.gz
ENV JAVA_HOME=/opt/java/temurin-25
RUN mkdir -p "$JAVA_HOME" && \
    tar -xzf /tmp/temurin.tar.gz -C "$JAVA_HOME" --strip-components=1 && \
    rm /tmp/temurin.tar.gz

ENV PATH="${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/23.0/bin:${PATH}"
RUN sdkmanager "cmdline-tools;23.0"
# Keep the platform and build tools aligned with gradle/libs.versions.toml.
RUN sdkmanager "build-tools;37.0.0" "platforms;android-37.0" "platform-tools" "emulator" && \
    test -f "${ANDROID_HOME}/platforms/android-37.0/android.jar"

FROM android-toolchain AS builder

WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
RUN ./gradlew --version

COPY build-logic build-logic
COPY download-dependencies download-dependencies

# Download all library dependencies
RUN ./gradlew -p download-dependencies/ app:dependencies

FROM builder AS app-image

COPY . .

ENTRYPOINT ["./gradlew"]
RUN ./gradlew build -Dskip.e2e=true -Dskip.debugVariants=true -Dskip.flavor.unrestricted=true
