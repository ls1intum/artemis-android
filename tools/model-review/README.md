# Offline Artemis model review

Version 1.0.0, bundled from the official npm release **@tumaet/apollon 5.3.1**. This is a local read-only canvas for native result review. Copy the complete `dist/` directory into a native app's bundled resources, preserving relative paths. Load `dist/index.html` in the app's local WebView. Do not ship `node_modules/`, test fixtures, or browser proof images.

## Native API

After navigation completes, check `window.ArtemisModelReview?.isReady === true`. The script is a deferred **classic IIFE**, so local `file:` loading works without JavaScript module CORS allowances. Optional ready callbacks are described below.

```js
window.ArtemisModelReview.renderBase64(base64UTF8JSON)
```

The argument is standard, unwrapped Base64 of UTF-8 JSON. Encode with the platform's UTF-8 encoder; do not encode Unicode text directly with JavaScript `btoa`. The decoded object is:

```json
{
  "model": { "version": "4.2.0", "type": "ClassDiagram", "nodes": [], "edges": [], "assessments": {} },
  "diagramType": "ClassDiagram",
  "feedback": [{ "reference": "Class:element-id", "credits": 1, "positive": true }],
  "selectedReference": "Class:element-id",
  "darkMode": false
}
```

`feedback` may be omitted. `selectedReference` may be omitted or null to clear selection. `darkMode` must be a boolean. Feedback fields besides reference/credits/positive are accepted and retained without displaying arbitrary HTML. Models use Apollon's official `importDiagram` migration for 2.x/3.x/4.x formats. Exercise `diagramType` is authoritative. Initial loading fits the model. Subsequent calls with the same model update feedback, selection and appearance while keeping the canvas mounted and preserving the reader's zoom; selecting a different reference pans to it.

`renderBase64` returns `{ok:true}` or `{ok:false,error:string}`. Invalid inputs replace the previous canvas with an accessible, recoverable error state. There are no tokens, URLs, authentication, or network parameters.

For a clicked element/member/relationship, the callbacks are:

```js
// Android
window.ArtemisModelBridge.selectElement(reference)
// iOS
window.webkit.messageHandlers.modelSelection.postMessage({reference})
```

If feedback `reference` matches the actual element ID exactly, or after its first `:` separator, the callback uses that **original full feedback reference**. Otherwise it sends the raw element ID. IDs are never regenerated. If multiple feedback entries refer to the same ID, the first matching original reference is used. No callback is sent for an unknown ID. Missing or detached native handlers are safe.

Optional lifecycle callbacks:

| Event | Android | iOS |
| --- | --- | --- |
| Script ready | `ArtemisModelBridge.ready()` | `modelReady.postMessage({version:'1.0.0',apollonVersion:'5.3.1'})` |
| Invalid model | `ArtemisModelBridge.renderError(message)` | `modelError.postMessage({message})` |

The ready guard and render return value work without these optional handlers. Optional handler exceptions are swallowed; they cannot break the canvas.

## Read-only and offline behavior

The wrapper uses `ApollonMode.Modelling`, `readonly:true`, `keyboardShortcuts:false`, `enablePopups:false`, `controls:[]`, `tags:false`, and `collaborationEnabled:false`. Feedback stays in the native UI. Selection and color use public `setElementHighlights` and `revealAssessment` APIs. No assessment-authoring or model-write API is exposed to native callers.

Key events, beforeinput, paste, cut, drops, drag-start, context menus and double-clicks are blocked in the capture phase. Node dragging, connection creation and editing popups are disabled by Apollon. Connection and resize handles remain geometrically measurable for routing but are invisible and inert. Pan, touch gestures and wheel zoom remain available. The host should keep the WebView's native navigation policy limited to its bundled resource URL.

The HTML CSP forbids network connections, remote scripts, remote images/fonts, workers, frames, plugins and forms. Link activation is blocked. Bundle metadata contains no external JS imports; all four font URLs are embedded `data:` resources. The build defines `Worker` as `undefined` inside the bundle so Apollon's supported synchronous routing fallback is used for all diagrams. This avoids local-file module-worker restrictions on WKWebView/Android WebView. No global native/browser Worker API is changed. No export, PDF, WASM, CDN or font download asset is needed.

Input limits are 12 MiB encoded Base64, 2,000 nodes and 5,000 relationships. Invalid geometries, missing endpoints, duplicate node/relationship IDs, missing parents and containment cycles fail closed. Very large models near these limits can still take time to route synchronously; device performance must be evaluated separately.

## Reproduce

Verified with Node 24.22.1 and npm 11.19.1. Apollon requires Node >=22 for development/builds.

```sh
npm ci
npm run build
npx playwright install chromium webkit
npm test
```

`package-lock.json` pins all transitive versions and the official Apollon registry integrity. Build/test require development-time npm/browser downloads on a clean machine. Runtime requires none. `npm run build` produces minified JS, CSS with embedded fonts, HTML, license notices and a SHA-256/byte manifest in `dist/`. Two consecutive builds produced identical manifests. The two-platform Playwright suite opens the actual `file:` index in Chromium and WebKit.

The Apollon npm tarball is `https://registry.npmjs.org/@tumaet/apollon/-/apollon-5.3.1.tgz`, integrity `sha512-VaDNg2Ibs5JFMLV2AJIZoJV7SEJ8W01s9+pawCk9mxj5mCp6MaFlpjHMpPpODw/2B/KXNm7npKIMhS7qfB4Lvw==`.

## Licenses and sources

The wrapper follows the Artemis Android MIT license retained in `LICENSE`. Apollon is MIT, Copyright 2024 TUM Applied Education Technologies; upstream source: https://github.com/ls1intum/Apollon. Third-party license texts are copied from every dependency contributing code to the bundle into `dist/licenses/`, with version/license inventory in `dist/THIRD-PARTY-NOTICES.txt`. Retain those files and the generated `*.LEGAL.txt` when redistributing. Inter's embedded fonts use the SIL Open Font License 1.1, copied into `licenses/Inter-OFL.txt` and `dist/licenses/Inter-OFL.txt` from the upstream Apollon source.

Reference diagram fixtures came from the local read-only Apollon checkout's `standalone/webapp/tests/fixtures`. Wrapper sources, build script, lockfile and tests are all in this source kit. The release includes no entire `node_modules` tree.

## Compatibility caveat

The upstream deployment fixture contains relationship `2f98cc2d-5d0f-41c4-b18c-9a1db6920065` with `targetHandle:'right-top'` on a deployment interface. Apollon 5.3.1 exposes only cardinal handles on that interface, so the raw fixture draws 7/8 relationships. The original is retained as `fixtures/deployment-diagram-upstream.json`; the representative `deployment-diagram.json` uses the supported `right` handle and draws all 8. All IDs are identical. The viewer now validates component/deployment interface endpoints before constructing Apollon. Any explicit handle other than `top`, `right`, `bottom`, or `left` rejects the entire model with `{ok:false,error:...}`, an accessible unavailable state and existing `renderError`/`modelError` callbacks: “This model uses an unsupported connection handle and cannot be displayed completely.” Missing relationship endpoint IDs also fail validation. The unchanged original fixture has a browser regression test expecting that error. The viewer never guesses a handle, rewrites an identifier, or presents this known partial rendering as a complete diagram. Other diagram-specific handle combinations still follow the upstream renderer; the new explicit guard is scoped to four-way component/deployment interfaces.
