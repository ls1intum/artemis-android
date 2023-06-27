# Artemis Android

## Project structure
The project structure is heavily inspired by [nowinandroid](https://github.com/android/nowinandroid). 
The modularization is heavily borrowed from the [one described in nowinandroid](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md).
The modularization helps keeping the app maintainable, fastens up build times and allows it to easily scale with new features.

The modularization is split up into two parent modules:
- **core**: Shares common code between features and code not directly related to features.
  - common: Code used in every other module
  - data: Request and send data over the network
  - datastore: Permanently store data in the application, e.g. access tokens.
  - model: Represent the data sent by the server, e.g. exercises, lectures. Uses kotlinx serialization.
  - ui: Common ui needed by more than one feature module.
  - websocket: Receive updates from the server.
- **feature**: Submodules directly implementing features such as the dashboard.


The following libraries and tools are utilized:
- Jetpack Compose: UI
- Koin: Dependency Injection
- Kotlin Flows: Reactive programing

## License
By building the dockerfile or using its derived images, you accept the terms in the following license agreements:
* [Android Software Development Kit License Agreement](https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/AndroidSoftwareDevelopmentKitLicenseAgreement-20190116) (or read it [here](https://developer.android.com/studio/terms.html))
* [Android SDK Preview License Agreement](https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/AndroidSDKPreviewLicenseAgreement)
* [Intel Android Extra License](https://raw.githubusercontent.com/thyrlian/AndroidSDK/master/EULA/IntelAndroidExtraLicense)