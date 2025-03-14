# How to deal with mend security issues

This is a documentation of the process I used to fix security issues reported by mend on Github. It 
will hopefully help me and others with the process in the future.


## Analyse project dependencies
- See the "Dependency Hierarchy" included in the mend github issue details
- Often you can find the reported dependency in the `sentry-external-modules.txt` file. 
- Run a gradle dependency scan via `.\gradlew.bat compileProductionUnrestrictedReleaseKotlin --scan` (Windows)
  - After the run, you have to accept the terms, click the provided link, and enter your email to access the scan result
  - Under "Dependencies" you can search for your dependency

**Note:** 
The latter command compiles your whole `app` gradle project, so you might find that the dependency version
reported by mend and the local scan actually differ. This is because mend compiles and scans each gradle module
separately (at least I think so). With the local scan, there might be additional version constraints introduced
from other modules, that lead to different version.

In case this is the case for a reported security vulnerability (mend says you use the vulnerable version, but
actually in the scan it shows a version that is >= the mentioned fixed version), you can either close the
issue as a false-positive (but mend may reopen the issue in the future), or anyways introduce constraints as shown in the following section. 


## A new security issue was opened by mend - What now?

Here are the most basic steps to fix the issue:
- Do we even need the vulnerable root dependency? If not, we can remove it.
- Is there already a newer version of the (root) dependency introducing the vulnerability? If yes, upgrade to the new version.

Most probably, the reported vulnerable dependency is introduced transitively by other root dependencies that we actually need. 
You can either completely exclude the vulnerable sub-dependency in case we do not need it in the app (maybe the dependency 
was introduce via a gradle bill-of-materials definition), or introduce a version constraint for the affected sub-dependency.

Edit `AndroidLibraryConventionPlugin.applySecurityIssuePatches` accordingly.

After your edits, you can rerun the gradle security scan, and observe that the dependency is no longer listed or upgraded to the specified version.
