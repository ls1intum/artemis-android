# Native Composer Resize Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans inline. Obtain one fresh whole-PR code review after implementation.

**Goal:** Align Android PR 804 with iOS PR 554's new bounded drag-to-resize interaction and emoji headings.

**Architecture:** A small immutable resize model owns gesture arithmetic; a Material grabber owns pointer/accessibility input. The native text field measures its natural height and available expansion, remaining independent of source editing. Integrate within the existing surface with bounded parent constraints.

**Tech Stack:** Kotlin, Compose Material 3, JUnit/Robolectric, API 37 phone and Pixel Tablet emulators.

**Spec:** ../specs/2026-10-25-android-composer-resize.md

## Global constraints

- Preserve source, selection, IME composition, clipboard/history and draft ownership.
- One rounded surface and toolbar; no fullscreen or new dependencies.
- 48 dp grabber target; parent constraints and toolbar stay authoritative.
- Exclude unrelated push-module changes; use the existing isolated worktree.

## Review focus

- Moving the handle must not compound pointer translations or steal text gestures.
- Interrupted gestures/animations and smaller windows must begin at rendered height.
- Expanded long drafts, attachments and completions must leave usable controls and caret visibility.
- Resize/tap/accessibility actions must preserve the editor document and focus.
- Session changes and recreation must not transfer another editor's expansion.

## Tasks

### Task 1: Bounded native resizing

Files: create `ComposerResizeState.kt` and `ComposerResizeHandle.kt` beside `MarkdownTextField.kt`; extract the native editor into `BasicMarkdownTextField.kt`; integrate the surface budget in `MarkdownTextField.kt`; add strings in `markdown_textfield_strings.xml`. Tests: `ComposerResizeStateTest.kt`, `ComposerResizeUiTest.kt`.

- [x] Add tests for absolute drag translation, clamps, release direction/nearest size, horizontal rejection, cancellation and compact recovery. Run focused suite and observe missing API RED.
- [x] Add Compose tests for actual up/down/horizontal drags, tap/semantics, source/selection/focus preservation, session reset, recreation, constrained Send/caret visibility. Observe RED before integrating.
- [x] Implement the focused model/handle, constrained editor sizing and session state. Use window-coordinate pointer positions and current rendered height. Run focused tests GREEN, then the conversation suite.
- [x] Refine existing transparent emoji headings without changing categories or search; run emoji suite and both module lint tasks plus full APK assembly.

### Task 2: Review, native verification and publication

- [x] Dispatch one fresh reviewer on the whole PR with the pinned iOS diff and review focus. Reproduce actionable findings with failing tests, fix them immediately, run the full relevant suites.
- [x] Test the actual app on phone/tablet: drag/tap, keyboard, formatting, long draft, rotation, enlarged text and completion. Save screenshots and screencasts to Desktop; fix the batched findings and confirm once.
- [x] Record exact checks, update PR description, commit and push only feature files. Confirm PR remains ready and report CI status.

## Execution record

Pre-flight: model/handle/editor share expansion in dp and an actual available-height limit; source editing remains separate. The grabber is the only new gesture owner. Baseline PR remote checks (build, JUnit, E2E) are green.


## Completed verification and review

The clean-worktree verification passes 248 conversation tests (including all 10
server-backed tests) and 9 emoji tests, zero failures/errors/skips. Both module
lint tasks and productionUnrestrictedDebug APK assembly pass.

The independent whole-PR review identified three Important issues and one Minor
issue. All were fixed: constrained attachment/editor layout, discontinuous
collapse, cold-flow session restoration, and horizontally dominant diagonal
gestures. Regressions reproduced each before the fixes. The restoration fix also
keeps one native editor through send-state updates, and scopes the native editor,
history, resize animation and pointer session to the document. A large-font
regression reproduced inadequate expansion room; collapsed limits now match the
iOS regular/compact intent without shrinking text.

Native phone and Pixel Tablet runs confirm up/down drag, tap, continued focused
typing, fixed Send, portrait/landscape, a 20-line draft and 130% text. The phone
also passes with animator duration scale zero (126 → 756 → 126 physical pixels).
Screenshots and two decoded native screencasts are on Desktop under
`Artemis Android Composer/resize-2026-10-25`. Previous galleries are preserved.

A final iOS comparison picked up commit
`78356bf29f45c80b0dfa27e92f7a9bf86c9c06d0`. Five additional parity tests check
quote toolbar actions, tag-named attachments, nested references, combining tails
and private-use scalars. A real tag-named-file/reference defect was reproduced
and fixed. Six content-provider tests cover unknown/misleading sizes, exact
limits, missing content and incomplete filename cursors. Four reproduced old
upload failures before introducing bounded reads and safe naming, all off the UI
thread. Cancelled uploads propagate cancellation and retain the draft.

Explicit Android adaptations remain Material controls, the existing emoji
catalog, lossless editable unsupported source and safe metadata previews. No
review finding was deferred. Physical hardware/OEM keyboards and manual TalkBack
operation were outside the emulator pass.

Final app-source verification: `73acefa5`, APK SHA-256 `fa5d6afe3cf8b022f5f4f3691f9f295237ebfd23eb7bec236301251b90c92f52`. The recordings capture resize commit `9837cb8a`; final-build screenshots confirm the subsequent codec/upload hardening with unchanged UI.
