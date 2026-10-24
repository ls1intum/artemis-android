# Android alignment with iOS PR 554

Reference: https://github.com/ls1intum/artemis-ios/pull/554 at
`da316f0fc86ef948be4c0f13bb6828aeae307107` (25 October 2026).
The user's existing Slack-style brief remains authoritative: one rounded composer,
no fullscreen, full-width native text, fixed Send, Aa replacing quick actions.

Match behavior using Compose and Material components. Keep Android's send icon,
48 dp targets, system picker, native IME/selection and WorkManager outbox. A literal
SwiftUI port and replacing the editing engine would add risk without improving
platform consistency. Preserve unsupported Markdown as editable source rather
than copying iOS's blanket read-only lock.

Align fixed add/close controls; put course references under add; make # directly
open channels. Add bounded attachment name/type previews and safe rename/remove,
retaining exact URLs and undo. Keep existing categorized emoji search, improving
its native targets/accessibility. Draw quotes as indented rules and code backgrounds
across the editor width. Preserve Markdown styles and reference payloads in native
copy/paste. Match structural Enter/paste behavior and keep suggestions/editor/Send
usable when the keyboard limits height.

Guard send cleanup by captured document/context/revision, persist changed drafts
without a cancelable debounce gap, and reconcile retained composers with newer
saved drafts. Existing host/thread upload ownership and successful-edit refresh
must remain covered. Keep push-notification changes out of the PR.

Move prior native evidence to Desktop/Artemis Android Composer; put alignment
comparison and updated evidence there. Verify meaningful regressions, module suites,
server-backed conversation tests, full app build and lint. Inspect phone/tablet in
one batched native pass, fix observed defects together, and confirm once. Then
commit only composer-related files and open the requested PR.
