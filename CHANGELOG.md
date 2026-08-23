# Changelog

## 9.13

- [NEW] themes-preview: new module rendering every stock base login page (auto-discovered from the keycloak-themes jar) through a login theme into an html gallery (`target/theme-preview/`, ftl index); reusable by derived themes via `ThemePreviewSupport` subclassing (inheritance-chain hooks, per-page fixtures); doubles as a template/data-model compatibility smoke test

- [ENH] themes-bootstrap: bootstrap bumped 5.3.2 → 5.3.8 and bootstrap-icons 1.11.2 → 1.13.1 (CDN, SRI refreshed, integrity now also pinned on the icons stylesheet)
- [BRK] themes-bootstrap: `template.ftl` is a purely-additive fork of upstream 26.7.2 (stock macro signature): `header2`/`showTabs` sections, `bodyClass` rendering and the cards wrapper div are gone. Subtitles render in-form as `<p id="kc-page-subtitle">` (collapsed when empty, kept above alerts); tabs moved to page templates; `cards=true` now ships its two-column layout in the library (`left-card.ftl` include + per-page `footerCard` section, no wrapper div); head tags reorder so `styles=` overrides bootstrap (googleFonts/baseHeaders first, `themeHeaders` last); `meta=` properties replace hard-coded metas
- [BRK] themes-bootstrap: `theme.properties` maps all remaining `kc*Class` overrides (select-authenticator, otp tiles, recovery codes, webauthn icons, radio/checkbox groups, textarea, secondary/block buttons, social disabled state); `kcLocaleListClass` misassignment fixed and a styled locale dropdown ships in css
- [FIX] themes-bootstrap: undefined `--theme-border-color` broke the password-reveal button border and its invalid state; `--theme-*` defaults now ship in `:root` and apply to `.form-control` (fixes consumer transparent inputs); social heading selector targeted removed `h4`; invalid border lost on hover/focus; selects unstyled next to inputs; success icon was a sun; duplicate `max-width` in `.keycloak-card`; favicon fallback 404 (theme now ships `img/favicon.ico`)
- [ENH] themes-bootstrap: css rewritten with nesting and an explicit specificity ladder (`:where()` skin defaults at (0,1,0)); paired buttons get semantic layouts (equal choices share a row, `name="cancel-aia"` stacks below); checkbox skin scoped to `.form-check`/`.checkbox` wrappers; theming variables documented in the module readme
- [ENH] themes: `opfa-freemarker-configurable` now expands `${conf.*}` in `theme.properties` values (properties, keyed resources, `themeHeaders`/`baseHeaders`) in addition to message bundles; `msg()`/`advancedMsg()` formatters are rebuilt over the expanded bundle (previously `${conf.*}` in messages reached `msg()` unexpanded and got mangled by the flat variable resolver); expansion semantics (verbatim unknown refs, `${k:fallback}`, `${k1,k2}` key chains, recursion) pinned by unit tests
- [BRK] remoting: `HttpClients` rebuilt as a builder (`trustSystem/trustCertificatesIn/trustAny`, `verifyHostnames/verifyHostnamesWith/verifyNoHostname`); the `HostnameOptions`-based `create` signatures are removed. the old `VERIFY` mode trusted any certificate chain while checking hostnames only; defaults are now fully verifying, with redirects/cookies/auth-caching off and opt-in
- [BRK] online-access: action-token login links are single-use again (the replay marker was invisible to keycloak's check since 26.6, making links replayable until expiry)
- [BRK] idp-apple: id-token signatures are validated via apple's jwks and the issuer is pinned; outbound access to `appleid.apple.com/auth/keys` is now required
- [FIX] api-provisioning: `groups/membership` query was syntactically invalid; boolean filters (`enabled`, `emailVerified`) never worked on postgres; blank usernames, negative offset/limit and group paths with blank segments returned 500s instead of 400s
- [FIX] email-sender: invalid `from`/`replyTo` addresses pass realm validation; recipients are now validated and idn-converted; duplicate cid references embedded one attachment per reference and duplicate allowlist ids broke all email sending
- [FIX] authenticators: sns client trusted any certificate and skipped hostname verification; conditional authenticators crashed on missing config
- [FIX] filtering: `sort=field,desc` sorted ascending; user input ending in a backslash escaped the appended like wildcard; case folding was locale-sensitive
- [FIX] login-stats: a malformed stored attribute crashed the listener on every login event of that user
- [ENH] email-sender: realigned with upstream `DefaultEmailSenderProvider` (26.7.2); smtp timeouts configurable, `envelopeFrom` via `mail.smtp.from`
- [ENH] idp-apple: pem-armored private keys tolerated; unparseable key material fails loudly instead of yielding a cryptic `invalid_client`
- [ENH] online-access: forged/unknown `alg` headers rejected with 403 instead of 500
- [ENH] themes: `opfa-freemarker-configurable` now expands `${conf.*}` in `theme.properties` values (properties, keyed resources, `themeHeaders`/`baseHeaders`) in addition to message bundles; `msg()`/`advancedMsg()` formatters are rebuilt over the expanded bundle (previously `${conf.*}` in messages reached `msg()` unexpanded and got mangled by the flat variable resolver); expansion semantics (verbatim unknown refs, `${k:fallback}`, `${k1,k2}` key chains, recursion) pinned by unit tests
- [NEW] test suite migrated to junit-jupiter (208 tests); remoting ships a raw-socket test http(s) server with deterministic failure injection

## 9.12

- [FIX] missing macro in login theme template
