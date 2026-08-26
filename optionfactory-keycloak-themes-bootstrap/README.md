# optionfactory-keycloak-themes-bootstrap

A `bootstrap` login theme: keycloak's `base` templates with every `kc*Class` property mapped to bootstrap 5 classes, bootstrap css/js/icons loaded from CDN (SRI-pinned), and a small head-injection contract for derived themes.

For visual inspection of every stock page rendered through this theme (and through derived themes), see [optionfactory-keycloak-themes-preview](../optionfactory-keycloak-themes-preview): `mvn test` writes the gallery, a `@Disabled` test serves it, and its aside edits this theme's `:root` tokens live in every frame at any viewport.

`template.ftl` is upstream's `base/login/template.ftl` (26.7.2) plus a few **marker-wrapped hunks** — the macro signature is byte-identical to upstream, so keycloak upgrades are a re-diff, not a merge. All hunks are wrapped in `<#-- opfa:modification start/end -->` comments; all but one only add, the exception dropping upstream's `col-md-10` grid wrapper around the title of `displayRequiredFields` pages.

## Quick start

```properties
# theme/mytheme/login/theme.properties
parent=bootstrap
locales=it
```

You get: bootstrap + bootstrap-icons + `keycloak-bootstrap.css` (head order below), styled stock pages (login, register, reset/update password, otp, select-authenticator, recovery codes, webauthn, terms, oauth grant, device code...), a styled locale dropdown, the optionfactory.net `img/favicon.ico` fallback, and button rows by default: any container holding nothing but a pair of buttons renders them equal-width side by side, whichever of the four shapes upstream used (`<form class="form-actions">`, `#kc-form-buttons`, a `kcFormButtonsWrapperClass` div, a bare `.form-group`). Add `flex-column` to opt a row out. Three pages stay stacked because upstream splits the pair across two forms (`webauthn-register`, `webauthn-error`) or mixes it in with other content (`login-recovery-authn-code-config`) — no selector reaches across that.

**Inheritance trap**: `theme.properties` merges **per key** — setting a key in a child theme replaces the parent's whole value (no merging of lists). This applies to `meta=` too: the library ships `meta=viewport==... format-detection==telephone==no format-detection==email==no`; if your theme sets `meta=`, restate the viewport or mobile scaling breaks. Same for `styles=`/`scripts=`/`stylesCommon=` (use keyed `styles.<id>=` entries, which merge per id, or `themeHeaders` links).

Add your own stylesheet the standard keycloak way:

```properties
styles=css/mytheme.css
```

Brand it with the CSS tokens below — no need to fight specificity.

## Head injection contract

Emitted in this order (the cascade is the contract):

1. `googleFonts` property → preconnects + stylesheet
2. `baseHeaders.0..10` — **library-level** tags (bootstrap, icons, `keycloak-bootstrap.css`)
3. `keycloak-floating-labels.css`, when `floatingLabels=true` (see below)
4. `styles=` / `stylesCommon=` / `scripts=` — standard keycloak properties, load **after** bootstrap so they override it at equal specificity
5. `themeHeaders.0..50` — **consumer-level** arbitrary tags, last word in the cascade

`baseHeaders`/`themeHeaders` values are raw HTML; `{resources}` and `{commonResources}` placeholders expand to the theme resource paths.

```properties
# consumer theme
themeHeaders.0=<script src="https://www.googletagmanager.com/gtm.js?id=GTM-XXX" type="text/javascript"></script>
themeHeaders.1=<script src="{resources}/js/mytheme.js"></script>
themeHeaders.2=<link rel="stylesheet" href="{resources}/css/mytheme.css">
```

Remote urls and arbitrary tags (`preconnect`, GTM, CDN scripts) are only expressible via `themeHeaders`: the stock `styles`/`scripts` mechanisms and the new keyed `styles.*`/`favicons.*` resources are local-path only. Prefer `styles=`/`scripts=` for local resources; when overriding `template.ftl`, keep the same ordering rules (see `opfa-head.ftl`).

`baseHeaders`/`themeHeaders` (and all other `theme.properties`) values support `${conf.*}` placeholders, expanded from server configuration by `opfa-freemarker-configurable` (see [optionfactory-keycloak-themes](../optionfactory-keycloak-themes)):

```properties
# keycloak.conf
login-theme-conf--gtm-id=GTM-XXX
# theme.properties
themeHeaders.0=<script src="https://www.googletagmanager.com/gtm.js?id=${conf.gtm-id}" type="text/javascript"></script>
```

## Overriding template.ftl

Avoid it if you can (everything below is reachable without forking). If you must, fork from this theme's `template.ftl` and reuse the head macros instead of copying loops:

```ftl
<#-- opfa:modification start -->
<#import "opfa-head.ftl" as opfaHead>
<#-- opfa:modification end -->
...
    <#-- before the styles blocks -->
    <@opfaHead.googleFonts/>
    <@opfaHead.baseHeaders/>
    ...
    <#-- after the scripts block -->
    <@opfaHead.themeHeaders/>
```

`opfa-head.ftl` and `left-card.ftl` resolve through theme inheritance (child-first), so a derived theme overriding them customizes every inheriting page without touching the layout.

## Theming variables

Set these in your `:root`. Authoritative defaults and per-token notes live in the `:root`
block at the top of `theme/bootstrap/login/resources/css/keycloak-bootstrap.css` — that
block is the API reference, so this table lists names only and cannot drift out of date.

| token | controls |
|---|---|
| `--opfa-accent` | primary button, password-reveal icon, card top border, checked checkbox, selected otp tile, locale switcher |
| `--opfa-accent-hover` | the accent one shade down — hover/focus text of accent-coloured controls; defaults to a derived shade |
| `--opfa-accent-subtle` | a tint of the accent behind a highlighted row (the locale menu); defaults to a derived mix |
| `--opfa-accent-contrast` | text/glyph drawn on top of `--opfa-accent` (set to `#000` for a light accent) |
| `--opfa-danger` | invalid field border and error message |
| `--opfa-icon-invalid` | the glyph inside an invalid field — restate it when you change `--opfa-danger`, the hex is baked into the svg |
| `--opfa-icon-check` | the checkmark glyph — white, like bootstrap's own; restate the url to recolour it |
| `--opfa-primary` | primary button; defaults to `--opfa-accent` |
| `--opfa-primary-contrast` | primary button text; defaults to `--opfa-accent-contrast` |
| `--opfa-primary-border` | primary button border at rest; defaults to `--opfa-primary` |
| `--opfa-primary-hover` | primary button bg+border on hover; defaults to a derived darker shade |
| `--opfa-primary-active` | primary button bg+border when active; defaults to a derived darker shade |
| `--opfa-primary-disabled` | primary button bg+border when disabled; defaults to `--opfa-primary` |
| `--opfa-secondary` | secondary and outline-secondary buttons (`kcButtonDefaultClass`/`kcButtonSecondaryClass`); defaults to bootstrap's own grey |
| `--opfa-secondary-contrast` | text on the secondary button, and its text when an outline one fills |
| `--opfa-secondary-border` | secondary/outline border at rest; defaults to `--opfa-secondary` |
| `--opfa-secondary-hover` | secondary bg+border on hover; defaults to a derived darker shade |
| `--opfa-secondary-active` | secondary bg+border when active; defaults to a derived darker shade |
| `--opfa-secondary-disabled` | secondary bg+border when disabled; defaults to `--opfa-secondary` |
| `--opfa-surface-bg` | what bootstrap paints fields, menus and raised rows with (drives `--bs-body-bg`) |
| `--opfa-body-color` | body and field text (drives `--bs-body-color`) |
| `--opfa-checkbox-size` | checkbox/radio control size; the gutter and vertical offset derive from it |
| `--opfa-checkbox-gap` | space between the control and its label |
| `--opfa-elevation` | raised surfaces: social buttons, locale menu |
| `--opfa-social-bg` | background of the social buttons and the organization chooser rows (white by default) |
| `--opfa-font-family` | page font (drives `--bs-body-font-family`) |
| `--opfa-heading-color` | h1–h6 and the brand header (drives `--bs-heading-color`) |
| `--opfa-header-background` | brand header background, e.g. `url(logo.svg) center/contain no-repeat` |
| `--opfa-header-text` | set to `none` to hide the realm name that would sit on top of a header logo |
| `--opfa-body-bg` | page background; defaults to `--bs-body-bg`, set it separately when inputs and page differ |
| `--opfa-card-border-color` | login card top border; defaults to `--opfa-accent` |
| `--opfa-card-bg` | card surface; `transparent` by default, so the page shows through as it does on the stock pages |
| `--opfa-card-radius` | card corner radius (`0`) |
| `--opfa-card-shadow` | card shadow (`none`) |
| `--opfa-card-max-width` | card width cap (`400px`) |
| `--opfa-card-padding` | card padding (`15px`) |
| `--opfa-card-min-height` | card floor (`auto`). Anything taller than the content pins the action row to the card's bottom — the same mechanism the viewport triggers on a phone. Set it inside a media query to pin on one viewport only |
| `--opfa-left-background` | cards layout: left panel background (multi-layer ok) |
| `--opfa-cards-inset` | cards layout: side inset of the login column |

The theme also **consumes** these bootstrap variables. Override them the way you would in
any bootstrap app — one value moves every component that reads it, this theme's included:

`--bs-border-color` (inputs, reveal button, otp tiles, locale menu, checkbox) ·
`--bs-border-radius` · `--bs-tertiary-bg` (menu row hover) ·
`--bs-danger-{text-emphasis,bg-subtle,border-subtle}` (the error alert).

`--bs-body-bg` and `--bs-body-color` are driven by `--opfa-surface-bg`/`--opfa-body-color`, so
set those instead. The library declares **no bootstrap colour in a bare `:root`** — that
out-specifies bootstrap's own `[data-bs-theme=dark]` block and pins the theme to light mode for
good; the two above are declared under `:root:not([data-bs-theme="dark"])` so dark mode still
reaches them. Keep that rule if you add tokens.

A whole rebrand is usually four lines:

```css
:root {
    --opfa-accent: #7a1f3d;
    --opfa-accent-contrast: #ffffff;
    --opfa-elevation: 0 10px 18px -5px #00000022;
}
body { background: linear-gradient(90deg, #faf7f8 0, #fff) 0 0/cover; }
```

### Overriding the library css

`keycloak-bootstrap.css` loads before your `styles=` sheet, so at equal specificity you
win. **To change something, copy its selector verbatim into your sheet — including any
`:hover` / `[aria-invalid="true"]` part.** The file contains no `!important` and no
selector you cannot restate. Reach for the tokens above first.

The file is not wrapped in `@layer`, deliberately: bootstrap arrives from the CDN
unlayered, and unlayered declarations beat every layer, so layering this file would hand
back every equal-specificity contest to bootstrap (`.form-label`, `.form-control` padding,
the focus border) and the skin would evaporate. That only changes if bootstrap is ever
vendored same-origin and imported with `layer(bootstrap)`.

### Migrating from the `--keycloak-*` / `--theme-*` era

| was | now |
|---|---|
| `--theme-border-color` | `--bs-border-color` |
| `--theme-background-color` | `--bs-body-bg` |
| `--theme-text-color` | `--bs-body-color` |
| `--keycloak-input-group-button-color`, `--keycloak-checkbox-bg-color` | `--opfa-accent` |
| `--keycloak-form-control-invalid-border-color` | `--opfa-danger` |
| `--keycloak-social-providers-box-shadow` | `--opfa-elevation` |
| `--keycloak-cards-inset` | `--opfa-cards-inset` |
| `--keycloak-header-color`, `--keycloak-heading-color` | `--bs-heading-color` |
| `--keycloak-alert-color` / `-bg` / `-border-color` | `--bs-danger-text-emphasis` / `-bg-subtle` / `-border-subtle` |
| `--keycloak-body-bg` | `--opfa-body-bg` |
| `--keycloak-header-background` | `--opfa-header-background` |
| `--keycloak-card-border-color` | `--opfa-card-border-color` |
| `--keycloak-left-background` | `--opfa-left-background` |
| `--keycloak-card-background-color`, `--keycloak-card-box-shadow` | `.keycloak-card { ... }` |
| `#kc-header-wrapper { display: none }` | `--opfa-header-text: none` |

## Cards layout (two-column)

Enable per theme:

```properties
cards=true
```

```properties
# messages_it.properties — left panel content (absent keys = empty panel with background only)
leftCardTitle=Area Clienti
leftCardText=...
```

- Left panel markup lives in `left-card.ftl` — **override the file in your theme** to customize it; brand it via `--opfa-left-background`.
- The **footer card** is per-page: define a `footerCard` section in a page template and it renders below the login box (non-empty section = shown; property gates the layout, section gates the page):

```ftl
<@layout.registrationLayout ...; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "footerCard">
        ...content...
    <#elseif section = "form">
        ...
```

- Below 992px the layout collapses to a single column (left panel hidden).
- `cards` unset/false: the layout blocks don't render at all — stock DOM.

## Floating labels (opt-in)

```properties
floatingLabels=true
```

The label starts inside the field and floats to its top edge once the field is focused or
filled — with **no javascript moving nodes around**. Bootstrap's own `.form-floating` wants the
control and its label as direct siblings, control first; keycloak emits neither order, in two
shapes (a bare `.form-label` on `login`, `login-password`, `update-email`...; the
`.keycloak-label-wrapper` div on the user-profile pages), which is why themes have historically
reparented the DOM in a script. `keycloak-floating-labels.css` reads the control's state through
`:has()` on the `.form-group` instead: both shapes work untouched, and a page that loses its
wrappers in a keycloak upgrade keeps working.

The flag emits two things: the stylesheet (right after `baseHeaders`, so your `styles=` sheet
still wins) and a parser-blocking one-liner right after the fields that sets `placeholder=" "`
on every field without one — CSS reads the empty state from `:placeholder-shown`, which never
toggles unless a placeholder attribute exists. It runs before the first paint, so there is no
flash.

| token | controls |
|---|---|
| `--opfa-floating-field-height` | field height (default `3.5rem`, bootstrap's own) |
| `--opfa-floating-padding-x` | horizontal inset shared by the value and its label |
| `--opfa-floating-padding-block` | vertical inset of both, at rest — the label sits on the value's own line |
| `--opfa-floating-padding-block-floated` | the value's vertical padding once the label has floated; set it equal to the one above when the label leaves the field entirely and the value should not move |
| `--opfa-floating-label-color` | label colour |
| `--opfa-floating-label-transform` | how the label moves when it floats |
| `--opfa-floating-label-background` | painted behind the floated label — a flat page-background colour notches the field's top border |

The label's resting position comes from `inset`, not padding, so it stays out of the transform:
`--opfa-floating-label-transform` values are plain offsets of the label text itself, unscaled.
A label parked astride the field's top border — the notched-outline look — is therefore just:

```css
:root {
    --opfa-floating-field-height: 52px;
    --opfa-floating-padding-x: 16px;
    --opfa-floating-padding-block: 15px;
    --opfa-floating-padding-block-floated: 15px;   /* the value never shifts */
    --opfa-floating-label-transform: scale(.85) translateY(-30px);
    --opfa-floating-label-background: var(--bs-body-bg);
}
```

Worth knowing:

- A field with a real placeholder (`inputTypePlaceholder` annotation) keeps it, and its label
  then floats permanently — placeholder and floating label compete for the same pixels, so pick
  one per field.
- `<select>` and `html5-date`/`html5-time` inputs never match `:placeholder-shown`, so their
  labels float permanently. Same as bootstrap.
- A group with `inputHelperTextBefore` keeps the stock stacked layout: the helper renders above
  the control, where the hoisted label would land on it instead of on the field.
- A multivalued user-profile attribute renders several controls under one label; the label
  floats over the first.
- Chrome's autofill *preview* (before the user picks an entry) does not float the label —
  `:autofill` only matches a committed value. Bootstrap has the same gap.
- Browser floor: `:has()` and `:dir()`, i.e. Firefox 121 / Chrome 120 / Safari 16.4 — the floor
  `keycloak-bootstrap.css` already sets with `:has()` for the invalid state.

`mvn test` renders the opt-in variant to `target/theme-preview-floating-labels/` next to the
default gallery, so both paths stay visually checkable.

## Page template conventions

- **Subtitles** render in-form: `<p id="kc-page-subtitle">${msg("someTitle2")}</p>` at the top of the form section. The library styles it, collapses it when empty (`:empty`), and keeps it above alerts (`order: -1`). Empty message values collapse cleanly — define `...Title2=` (empty) or omit.
- The `* required fields` hint on `displayRequiredFields` pages (`.subtitle`) ships styled: right-aligned, 0.875em, muted. Both the wrapper div and the inner span carry `.subtitle`, so override size/colour on `span.subtitle` to avoid compounding.
- **Block buttons** (`kcButtonBlockClass`) are `w-100 keycloak-button-block`: full width plus the 0.5rem gap to whatever sits above them. The gap is a
  library rule rather than a `mt-2` utility, so an action row can take it back off a pair sharing a line — utilities carry `!important` and no rule could.
- Per-page CSS: `body[data-page-id=login-<pageId>]` (stock attribute; the library does not render `bodyClass`).
- Custom step templates should use `kc*Class` properties (`kcFormCheckClass`, `kcCheckboxClass`/`kcCheckboxLabelClass`, `kcInputClass`, `kcFormGroupClass`, ...) rather than hardcoded classes, so they stay themable across theme families — the `kcCheckbox*` family resolves under both this theme (`form-check`) and stock `keycloak.v2` (`pf-v5-c-check`). Put the class on the **input** too (`kcCheckboxInputClass`), not just the wrapper: bootstrap skins the control through `.form-check-input`, and a classless input renders as a native widget.
- Missing message keys render as the key itself — define them per theme/locale.

## Upgrading keycloak

1. Re-diff `template.ftl` against the new upstream `base/login/template.ftl`; re-apply the marked `opfa:modification` hunks (import, three macro calls, left-card include, footer-card block, floating-label placeholder shim, and the two that drop the `col-md-10` wrapper). The signature line must stay stock.
2. Re-check `theme.properties` for new/renamed `kc*Class` properties used by base pages.
3. Review `keycloak-bootstrap.css` against upstream markup changes.
4. `${conf.*}` placeholders (server-config values) are expanded by `opfa-freemarker-configurable` in [optionfactory-keycloak-themes](../optionfactory-keycloak-themes) — in both message bundles and `theme.properties` values (including `styles=`, `themeHeaders.*`, keyed resources). Unknown `${...}` refs stay verbatim; `${sys.*}`/`${env.*}` substitution happens earlier at theme load.
