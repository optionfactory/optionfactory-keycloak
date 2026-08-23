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

Brand it with CSS variables (see below) — no need to fight specificity.

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

Override in your `:root`; all have library defaults.

| variable | default | controls |
|---|---|---|
| `--theme-border-color` | `var(--bs-border-color)` | input-group reveal button border, locale dropdown border, otp tiles |
| `--theme-background-color` | `#ffffff` | `.form-control` background (theming contract) |
| `--theme-text-color` | `var(--bs-body-color)` | `.form-control` text color |
| `--keycloak-body-bg` | white gradient | page background (any `background` value) |
| `--keycloak-header-background` | `none` | brand header background (e.g. `url(...) center/contain no-repeat`) |
| `--keycloak-header-color` | `#ededed` | header text color |
| `--keycloak-heading-color` | `#b3b3b3` | h1–h4 color |
| `--keycloak-card-background-color` | `transparent` | login card background |
| `--keycloak-card-box-shadow` | `none` | login card shadow |
| `--keycloak-card-border-color` | `#0066CC` | login card top border, otp checked tile |
| `--keycloak-input-group-button-color` | `#0d6efd` | password-reveal icon color |
| `--keycloak-form-control-invalid-border-color` | `#dc3545` | invalid input border |
| `--keycloak-checkbox-bg-color` | `#0d6efd` | checked checkbox color |
| `--keycloak-social-providers-box-shadow` | | social buttons + locale dropdown shadow |
| `--keycloak-left-background` | `none` | cards layout: left panel background (multi-layer ok) |
| `--keycloak-cards-inset` | `1.5em` | cards layout: side inset of the login column |

### CSS specificity ladder

`keycloak-bootstrap.css` uses CSS nesting and a deliberate specificity ladder:

- **tier 0** — skin defaults via `:where()` (e.g. `.form-control:where(input, select)`, `.form-check :where(input[type=checkbox])`): (0,1,0). Any consumer selector naming the class wins, regardless of load order.
- **tier 1** — interaction states (`:hover`, `:focus`, `[aria-invalid]`, `:checked`): one notch up. Match the pseudo-class to override.
- Structural hooks (`.form-group`, `.input-group > .btn`) stay at their bootstrap-equivalent specificity.

So consumer overrides can use plain class selectors: `.form-control { border: 1px solid ... }` beats the library skin; `input.form-control` gymnastics are unnecessary.

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
- Per-page CSS: `body[data-page-id=login-<pageId>]` (stock attribute; the library does not render `bodyClass`).
- Custom step templates should use `kc*Class` properties (`kcFormCheckClass`, `kcCheckboxClass`/`kcCheckboxLabelClass`, `kcInputClass`, `kcFormGroupClass`, ...) rather than hardcoded classes, so they stay themable across theme families — the `kcCheckbox*` family resolves under both this theme (`form-check`) and stock `keycloak.v2` (`pf-v5-c-check`).
- Missing message keys render as the key itself — define them per theme/locale.

## Upgrading keycloak

1. Re-diff `template.ftl` against the new upstream `base/login/template.ftl`; re-apply the marked `opfa:modification` hunks (import, two macro calls, left-card include, footer-card block). The signature line must stay stock.
2. Re-check `theme.properties` for new/renamed `kc*Class` properties used by base pages.
3. Review `keycloak-bootstrap.css` against upstream markup changes.
4. `${conf.*}` placeholders (server-config values) are expanded by `opfa-freemarker-configurable` in [optionfactory-keycloak-themes](../optionfactory-keycloak-themes) — in both message bundles and `theme.properties` values (including `styles=`, `themeHeaders.*`, keyed resources). Unknown `${...}` refs stay verbatim; `${sys.*}`/`${env.*}` substitution happens earlier at theme load.
