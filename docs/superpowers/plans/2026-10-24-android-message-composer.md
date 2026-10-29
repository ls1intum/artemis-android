# Android message composer

## Design

One rounded, subtly translucent Material surface contains a growing native text
field and one action row. Quick actions offer attachments, Aa, emoji, member
mentions and channel/course references. Aa replaces these with horizontally
scrolling formatting controls. Send stays outside the scroll area. There is no
fullscreen mode or edit/preview tab. Controls use 48 dp targets, resource-based
accessibility labels, semantic light/dark colors, and Android's system pickers.

The iOS implementation supplied the feature baseline (inline styles, links,
lists, quotes and code); the supplied Slack examples supplied the interaction
model. Android uses Compose controls and native selection, clipboard and IME
behavior rather than reproducing the iOS toolbar arrangement.

## Document boundary

Persisted drafts, transport messages, autocomplete and attachment payloads remain
Markdown. The native text field receives visible text, selection and composition.
An identity-offset visual transformation paints styles only. MarkdownEditEngine
maps native replacements to source-aware operations; MarkdownDocument retains
independent glyph marks, hyperlink labels and atomic references/attachments.

MarkdownPresentation uses JetBrains Markdown's GFM syntax tree with exact source
ranges, plus Artemis references and supported inline HTML. The CommonMark escape
decoder matches the existing Markwon message renderer. Unsupported source remains
editable and is preserved on an untouched send. Sending does not globally
re-serialize existing Markdown. Safe code delimiters and local emphasis encoding
are checked against both presentation and the actual message renderer in tests.

The earlier source-text VisualTransformation design was replaced during the
reliability review: native selection and clipboard operations must operate on
visible text. Parsing and rewriting were also reduced to avoid the reproduced
seconds-long typing stall in heavily marked drafts.

## Reliability work

- Native copy/cut, selection, IME composition, keyboard shortcuts, undo and redo.
- Partial and overlapping styles, literal punctuation, code fences/languages,
  entities, links and immutable Artemis/image/file payloads.
- Enter continuation and exit for lists/quotes, including empty-item toolbar state.
- Link dialogs rebase around asynchronous uploads without overwriting other edits.
- Picker targets captured before launch, with launchers kept outside transitions.
- Draft/cache/autosave identity includes host and thread; delayed uploads recheck
  the latest draft after suspended reads and retain request credentials and URL.
- Forward comments and history have source-specific sessions; comments are frozen
  at Send before asynchronous recipient fan-out.
- Constrained-height forward sheets reserve room for Send and the composer.

## Verification

Use the productionUnrestrictedDebug variant and the isolated local QA backend:

```sh
SERVER_URL=http://localhost:18088 ./gradlew \
  :feature:metis:conversation:testProductionUnrestrictedDebugUnitTest \
  :feature:metis:conversation:lintProductionUnrestrictedDebug \
  :app:assembleProductionUnrestrictedDebug -Dskip.e2e=false
```

Native checks use the actual app signed in to Local Composer QA / general on:

- Phone: API 37, 1080 × 2400, 420 dpi.
- Pixel Tablet AVD: API 37, physical 2560 × 1600, 320 dpi, with no wm size override;
  landscape, portrait, 130% text, keyboard open and a 20-line draft.

Final evidence and the exact verification results are recorded in
`Desktop/Artemis Android Composer/verification.md`; screenshots and two
native screen recordings are under the same directory. No temporary QA activity
or manifest route is part of the app. Existing push-notification changes are
preserved. The 25 October follow-up requests a PR; see the iOS 554 alignment plan.
