# Communication editor UX fixes

> Execute inline using systematic debugging and test-driven development.

**Goal:** Fix the four reported regressions in the existing formatted composer, preserving Markdown, native selection, focus, history and accessibility.

**Approach:** Keep the current editor architecture. Reproduce selection through touch and the platform input boundary, verify quote layout using real text measurement, keep keyboard configuration stable while formatting, and reduce grabber whitespace without overlapping text gestures.

- [x] Reproduce the crash and record its originating stack; add gesture/selection regressions covering plain text, formatting, references, copy and replacement.
- [x] Add failing quote layout tests for Enter, empty continuation, adjacent paragraphs, wrapping and selection. Correct paragraph style boundaries, preserving source and caret mapping.
- [x] Add input-session tests for toggling inline code/code block and crossing code boundaries. Preserve the active connection and typing location.
- [x] Add layout tests for a compact empty composer, reachable resizing and Send at narrow sizes/large text. Reduce the grabber's reserved whitespace while keeping a usable touch area.
- [x] Run the complete conversation unit suite, relevant lint and app assembly. Exercise native selection, quote Enter and code toggles on an emulator; report any verification limitations.

Review focus: reversed selections, emoji offsets, an empty quoted last line, asynchronous clipboard probes, software-keyboard composition, and short windows with Send visible.

## Regression evidence

The initial targeted run reproduced the AndroidClipboard selection-menu exception, extra quote paragraphs, restarted input sessions and oversized grabber gap. Follow-up cases exposed code-block selection expansion, reversed multiline selection, composition loss and quote accessibility/caret indentation. These cases now pass: 25 native-rendered Compose UI tests, six code-block editing tests and seven clipboard tests (35 new tests, three existing).

Quote presentation preserves UTF-16 offsets, editable/accessibility text, real clipboard newlines and Markdown. Keyboard options remain fixed while focused and are adopted immediately for the next focus session. The 24 dp visual grabber retains a 48 dp touch target, with its lower extension reserved outside editable text.

Final verification: 273 conversation unit tests passed with no failures, errors or skips. Conversation lint completed with zero errors and six existing warnings. `:app:assembleProductionUnrestrictedDebug` passed. Commands used `-Dskip.e2e=true --offline --max-workers=4`.

Native component checks used a temporary debug activity containing the production composer on the phone and Pixel Tablet emulators. Both survived long-press selection, rendered quote Enter as two lines, retained a single IME session through inline-code/code-block toggles, preserved subsequent typing and kept the grabber-to-editor gap at approximately 24 dp. The tablet pass used dark mode and 130% font scaling; settings were restored afterward. Screenshots, metrics and the final APK hash are in ignored `test-outputs/editor-native/`. The temporary activity was removed, the final APK manifest was checked, and the final APK was installed on both emulators.


## Publication follow-up (26 October)

- [x] Independent final-diff review. Reproduced its composition-decoration finding with two rendered pixel tests: both failed when the IME underline suppressed quote/code painting, and both passed after matching text and paragraph geometry while allowing IME span annotations.
- [ ] Rebase only this follow-up onto current `develop` (the original formatter was squash-merged as PR 804), then repeat affected checks and generate coverage with the updated pinned dependencies.
- [ ] Publish the focused PR with native captures attached; monitor current-head CI and automated review, address actionable feedback, and verify readiness metadata.

Release metadata: the Android repository has no configured milestones and its recent PRs use none. A release preference was requested; retain the existing no-milestone convention unless the user supplies a release.
