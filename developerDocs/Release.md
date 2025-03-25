# How to release new app versions

## Checklist
- Create a new draft release on github. 
  - Make the automatically generated release notes more readable by sorting them and removing irrelevant information (eg Dependabot version updates)
- Test all the newly introduced features and the basic app functionality (such as sending and receiving posts)
- Update the app version name and code in `app/build.gralde.kts` and commit that to `develop`
- Merge `develop` into `main`
- Build a new app bundle and release it on the Play Console
  - For the PlayStore release notes, shorten the Github release notes and only include the most relevant information for the end users


### Release versioning
- For breaking changes, introduce an new major version (1.x.x -> 2.0.0)
- Increase the minor version (x.1.x -> x.2.0), if either there are huge new features added (based on own judgement), or when the database version changes
- For other changes, but increase the patch version (x.x.1 -> x.x.2)

### Release notes
You can use the scripts in `/scripts/releaseNotes` to sort and format the release notes for the github
and PlayStore release notes.

In Android Studio, you can copy the functions in the scripts to a scratch file and call the methods 
there (use "Interactive mode" to immediately access the returned formatted strings).

### Building a new app bundle
For now, we do this locally, but it is planned to shift this step to Github actions


#### Locally
- Make sure the version-name and -code changes are synced with gradle
- In android studio, go to "Build > Generate Signed App Bundle / APK"
- Select "Android App Bundle"
- Provide the signing keystore and password
- Choose "productionUnrestrictedRelease" build variant