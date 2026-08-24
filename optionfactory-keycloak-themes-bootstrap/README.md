# optionfactory-keycloak-themes-bootstrap

A `bootstrap` login theme: keycloak's `base` templates with every `kc*Class` property mapped to bootstrap 5 classes, bootstrap css/js/icons loaded from CDN (SRI-pinned), and a small head-injection contract for derived themes.

For visual inspection of every stock page rendered through this theme (and through derived themes), see [optionfactory-keycloak-themes-preview](../optionfactory-keycloak-themes-preview).

`template.ftl` is upstream's `base/login/template.ftl` (26.7.2) plus a few **purely additive, marker-wrapped hunks** — the macro signature is byte-identical to upstream, so keycloak upgrades are a re-diff, not a merge. All hunks are wrapped in `<#-- opfa:modification start/end -->` comments.

## Quick start

```properties
# theme/mytheme/login/theme.properties
parent=bootstrap
locales=it
```

You get: bootstrap + bootstrap-icons + `keycloak-bootstrap.css` (head order below), styled stock pages (login, register, reset/update password, otp, select-authenticator, recovery codes, webauthn, terms, oauth grant, device code...), a styled locale dropdown, the optionfactory.net `img/favicon.ico` fallback, and sane button-pair layouts (equal choices side-by-side, `name="cancel-aia"` escape hatches stacked below the primary action).

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
3. `styles=` / `stylesCommon=` / `scripts=` — standard keycloak properties, load **after** bootstrap so they override it at equal specificity
4. `themeHeaders.0..50` — **consumer-level** arbitrary tags, last word in the cascade

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
| `--opfa-accent` | primary button, password-reveal icon, card top border, checked checkbox, selected otp tile |
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
| `--opfa-checkbox-size` | checkbox/radio control size; the gutter and vertical offset derive from it |
| `--opfa-checkbox-gap` | space between the control and its label |
| `--opfa-elevation` | raised surfaces: social buttons, locale menu |
| `--opfa-font-family` | page font (drives `--bs-body-font-family`) |
| `--opfa-heading-color` | h1–h6 and the brand header (drives `--bs-heading-color`) |
| `--opfa-header-background` | brand header background, e.g. `url(logo.svg) center/contain no-repeat` |
| `--opfa-header-text` | set to `none` to hide the realm name that would sit on top of a header logo |
| `--opfa-body-bg` | page background; defaults to `--bs-body-bg`, set it separately when inputs and page differ |
| `--opfa-card-border-color` | login card top border; defaults to `--opfa-accent` |
| `--opfa-left-background` | cards layout: left panel background (multi-layer ok) |
| `--opfa-cards-inset` | cards layout: side inset of the login column |

The theme also **consumes** these bootstrap variables. Override them the way you would in
any bootstrap app — one value moves every component that reads it, this theme's included:

`--bs-body-bg` (input/menu/social surfaces) · `--bs-body-color` (body and input text) ·
`--bs-border-color` (inputs, reveal button, otp tiles, locale menu, checkbox) ·
`--bs-border-radius` · `--bs-tertiary-bg` (menu row hover) ·
`--bs-danger-{text-emphasis,bg-subtle,border-subtle}` (the error alert).

The library sets only two bootstrap variables itself, both colour-scheme independent:
`--bs-body-font-family` and `--bs-heading-color`. It deliberately declares **no bootstrap
colour** in `:root` — doing so out-specifies bootstrap's own `[data-bs-theme=dark]` block
and would pin the theme to light mode for good. Keep that rule if you add tokens.

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

- Left panel markup lives in `left-card.ftl` — **override the file in your theme** to customize it; brand it via `--keycloak-left-background`.
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

## Page template conventions

- **Subtitles** render in-form: `<p id="kc-page-subtitle">${msg("someTitle2")}</p>` at the top of the form section. The library styles it, collapses it when empty (`:empty`), and keeps it above alerts (`order: -1`). Empty message values collapse cleanly — define `...Title2=` (empty) or omit.
- The `* required fields` hint on `displayRequiredFields` pages (`.subtitle`) ships styled: right-aligned, 0.875em, muted. Both the wrapper div and the inner span carry `.subtitle`, so override size/colour on `span.subtitle` to avoid compounding.
- Per-page CSS: `body[data-page-id=login-<pageId>]` (stock attribute; the library does not render `bodyClass`).
- Custom step templates should use `kc*Class` properties (`kcFormCheckClass`, `kcCheckboxClass`/`kcCheckboxLabelClass`, `kcInputClass`, `kcFormGroupClass`, ...) rather than hardcoded classes, so they stay themable across theme families — the `kcCheckbox*` family resolves under both this theme (`form-check`) and stock `keycloak.v2` (`pf-v5-c-check`).
- Missing message keys render as the key itself — define them per theme/locale.

## Upgrading keycloak

1. Re-diff `template.ftl` against the new upstream `base/login/template.ftl`; re-apply the marked `opfa:modification` hunks (import, two macro calls, left-card include, footer-card block). The signature line must stay stock.
2. Re-check `theme.properties` for new/renamed `kc*Class` properties used by base pages.
3. Review `keycloak-bootstrap.css` against upstream markup changes.
4. `${conf.*}` placeholders (server-config values) are expanded by `opfa-freemarker-configurable` in [optionfactory-keycloak-themes](../optionfactory-keycloak-themes) — in both message bundles and `theme.properties` values (including `styles=`, `themeHeaders.*`, keyed resources). Unknown `${...}` refs stay verbatim; `${sys.*}`/`${env.*}` substitution happens earlier at theme load.
