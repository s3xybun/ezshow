# Changelog

## 1.1.0

- Keep `/show` as the only command form; remove the `main` and `off` arguments.
- Share the main-hand item when present, otherwise fall back to the off hand.
- Show the vanilla localized `/show` usage for empty hands, invalid arguments, and cooldown rejection.
- Remove custom locale reflection and server-side translation catalogues in favor of vanilla command feedback.
- Preserve translatable NBT display names in addition to default and custom item names.
- Add project branding, user-facing bilingual READMEs, centralized contributor documentation, and repository hygiene files.

## 1.0.1

- Keep default item names as translation components so every client renders them in its own language.
- Preserve explicit anvil/custom names as literal italic text.

## 1.0.0

- Add `/show`, `/show main`, and `/show off`.
- Broadcast vanilla hoverable item components with full item NBT.
- Add configurable per-player cooldown and Forge permission nodes.
- Add English and Simplified Chinese server-side feedback.
- Support dedicated-server-only and integrated-server deployments.
