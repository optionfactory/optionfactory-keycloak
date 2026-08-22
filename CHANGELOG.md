# Changelog

## 9.13

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
- [NEW] test suite migrated to junit-jupiter (208 tests); remoting ships a raw-socket test http(s) server with deterministic failure injection

## 9.12

- [FIX] missing macro in login theme template
