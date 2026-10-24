# iOS PR 554 Alignment Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans for inline execution; independent draft and attachment work is delegated with disjoint file ownership.

**Goal:** Align Android's formatted message editing with the current iOS follow-up while retaining Android conventions.

**Architecture:** Keep the native Compose/source boundary, Markdown codec and WorkManager outbox. Extend independent attachment and clipboard adapters, draw structural blocks from presentation ranges, and strengthen draft ownership at the persistence/send boundary.

**Tech Stack:** Kotlin, Compose Material 3, Android DataStore, JetBrains Markdown, Robolectric/JUnit, native API 37 phone and Pixel Tablet emulators.

**Spec:** ../specs/2026-10-25-ios554-alignment.md

## Global constraints

- One rounded composer and one toolbar; no fullscreen.
- Native Android controls, 48 dp targets and no extra dependencies.
- Preserve exact attachment URLs, reference payloads, unsupported source, IME composition and newer drafts.
- Exclude existing push-module work from the PR.

## Review focus

Delayed sends/attachments cannot clear a newer draft; retained composers cannot resurrect deleted drafts; clipboard payloads must remain readable outside Artemis; attachment rename must preserve destinations and reject stale targets; structural formatting must survive multiline edits and narrow keyboard-constrained layouts.

## Tasks

- [x] Draft ownership: `ConversationViewModel`, `ReplyState`, `ReplyMode`, provider/handler and storage service. Regressions: immediate disposal, external draft replacement/deletion, parent/body capture, pending send completion and failed edits. Observe RED, implement, run lifecycle/routing tests.
- [x] Attachment management: new `MarkdownAttachments` and `ComposerAttachments`, integrated through the existing history commit. Regressions: exact destination/Unicode labels, stale targets, concurrent upload, rename/remove and undo. Observe missing-API RED, implement, run util/UI tests.
- [x] Block editing: existing parser/document/engine/list helper. Regressions: hidden quote prefix, continuation/exit, multiline block paste, ordered renumbering, code exit and composition. Observe RED, implement, run util/renderer tests.
- [x] Native shell: toolbar, clipboard adapter, block decoration and emoji targets. Regressions: fixed add/send at 220 dp, direct channel action, course references under add, native copy/paste preserving marks/atoms. Observe RED, implement, run UI/emoji tests.
- [x] Verify one integrated build with all conversation and emoji suites, lint and app assembly; inspect actual phone/tablet, refresh Desktop media/comparison, and obtain bounded independent review.
- [x] Create a feature branch, stage only this feature, commit and push, create a PR with the final implementation and validation results.

## Review record

The whole-feature independent review found two Important races. Five new tests
reproduced them: clipboard token delivery after unrelated callbacks/eviction/duplicate
delivery, and attachment completion/newer same-body revisions during edit save.
Retired tokens are rejected before normal editing, and edit completion now checks
the submitted mode, session and document revision. The combined verification run
passes 217 conversation tests (including all 10 backend tests) and 9 emoji tests.
Both module lint tasks and the full debug app build pass in the clean detached
worktree from the feature branch. The native phone pass reproduced an empty
bold/quote to code-block transition defect; two regressions failed before the fix and pass
after it. The landscape check also reproduced a clipped caret after resize; a
Compose regression reproduced it before adding viewport-aware caret scrolling.
Code input disables sentence capitalization and autocorrection. Native retesting
confirms the code background and Enter exit, quote indent/rule and software-keyboard Backspace, completion, attachment rename/remove
and emoji search/insertion. Actual Pixel Tablet verification covers landscape,
portrait, 130% text, an open keyboard and a scrolling 20-line draft.

Updated native PNGs and two decoded/verified MP4s, the pinned iOS comparison and
the exact APK hash are on Desktop under `Artemis Android Composer/alignment-2026-10-25`.
The original captures remain available in the parent gallery.

PR: https://github.com/ls1intum/artemis-android/pull/804, targeting `develop`.
The branch excludes existing push-module modifications. GitHub CI results are
reported on the PR; local evidence does not imply that remote checks have finished.
