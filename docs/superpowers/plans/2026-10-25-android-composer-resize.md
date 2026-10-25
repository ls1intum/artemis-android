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

- [ ] Add tests for absolute drag translation, clamps, release direction/nearest size, horizontal rejection, cancellation and compact recovery. Run focused suite and observe missing API RED.
- [ ] Add Compose tests for actual up/down/horizontal drags, tap/semantics, source/selection/focus preservation, session reset, recreation, constrained Send/caret visibility. Observe RED before integrating.
- [ ] Implement the focused model/handle, constrained editor sizing and session state. Use window-coordinate pointer positions and current rendered height. Run focused tests GREEN, then the conversation suite.
- [ ] Refine existing transparent emoji headings without changing categories or search; run emoji suite and both module lint tasks plus full APK assembly.

### Task 2: Review, native verification and publication

- [ ] Dispatch one fresh reviewer on the whole PR with the pinned iOS diff and review focus. Reproduce actionable findings with failing tests, fix them immediately, run the full relevant suites.
- [ ] Test the actual app on phone/tablet: drag/tap, keyboard, formatting, long draft, rotation, enlarged text and completion. Save screenshots and screencasts to Desktop; fix the batched findings and confirm once.
- [ ] Record exact checks, update PR description, commit and push only feature files. Confirm PR remains ready and report CI status.

## Execution record

Pre-flight: model/handle/editor share expansion in dp and an actual available-height limit; source editing remains separate. The grabber is the only new gesture owner. Baseline PR remote checks (build, JUnit, E2E) are green.
