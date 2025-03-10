# ------------------------------ LICENSE AGREEMENT NOTICE ------------------------------
# By using this dockerfile, e.g. by building it or executed it's derived
# images you agree the terms of the following license agreements:
# - Android Software Development Kit License Agreement (https://developer.android.com/studio/terms.html)
# - Android SDK Preview License Agreement (https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/AndroidSDKPreviewLicenseAgreement)
# - Intel Android Extra License (https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/IntelAndroidExtraLicense)
# ------------------------------ LICENSE AGREEMENT NOTICE ------------------------------


FROM thyrlian/android-sdk:9.1 as builder

RUN sdkmanager "build-tools;33.0.1" "build-tools;30.0.3" "platforms;android-34" "platform-tools" "emulator"

WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
RUN ./gradlew --version

COPY build-logic build-logic
COPY download-dependencies download-dependencies

# Download all library dependencies
RUN ./gradlew -p download-dependencies/ app:dependencies

FROM builder as app-image

COPY . .

ENTRYPOINT ["./gradlew"]
RUN ./gradlew build -Dskip.e2e=true -Dskip.debugVariants=true -Dskip.flavor.unrestricted=true
