# How to release new app versions

## Checklist
- Create a new draft release on github. 
  - Make the automatically generated release notes more readable by sorting them and removing irrelevant information (eg Dependabot version updates)
- Test all the newly introduced features and the basic app functionality (such as sending and receiving posts)
- Update the app version name in `app/build.gralde.kts` and commit that to `develop` (the app version *code* will be set by the Github action)
- Merge `develop` into `main`, this will build a new app bundle and release it as a draft on the Play Console
  - For the PlayStore release notes (configured in the Play Console), shorten the Github release notes and only include the most relevant information for the end users
- "Save and Publish" the new internal release and promote it to production if applicable

### Release versioning
- For breaking changes, introduce an new major version (1.x.x -> 2.0.0)
- Increase the minor version (x.1.x -> x.2.0), if either there are huge new features added (based on own judgement), or when the database version changes
- For other changes, but increase the patch version (x.x.1 -> x.x.2)

### Release notes
You can use the scripts in `/scripts/releaseNotes` to sort and format the release notes for the github
and PlayStore release notes.

In Android Studio, you can copy the functions in the scripts to a scratch file and call the methods 
there (use "Interactive mode" to immediately access the returned formatted strings).

### How to add a new release
We created a github action to build, sign and deploy a new release. It is called "Create new release" and can be triggered manually or by merging / pushing to `main`.
This workflow will create a new app version draft in the internal testing track in the Play Console.

