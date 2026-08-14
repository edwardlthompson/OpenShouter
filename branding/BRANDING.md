# Branding kit

Replaceable product identity for projects bootstrapped from **agent-project-bootstrap**.

## Single sources of truth

| Concern | Edit here | Then run |
|---------|-----------|----------|
| Colors, type, spacing | [`design-tokens/design-tokens.json`](../design-tokens/design-tokens.json) | `python3 scripts/sync-design-tokens.py` |
| Logos / favicon / heroes | [`branding/assets/`](assets/) | `python3 scripts/sync-design-tokens.py` |
| Name, pitch, README copy | [`branding/product.json`](product.json) | `python3 scripts/generate-project-readme.py` |
| Voice guidelines | [`branding/voice.md`](voice.md) | (docs only) |
Official color stylesheet (generated): [`official-colors.css`](official-colors.css).

## Asset inventory

| File | Use |
|------|-----|
| `assets/logo-mark.png` | Flat app / launcher mark (source of truth for tiles and icons) |
| `assets/logo-mark.svg` | SVG wrapper around the PNG for tools that want SVG |
| `assets/logo-mark-mono.svg` | Monochrome / print |
| `assets/logo-mark-photo.jpg` | Photorealistic 3D mark for splash (not the launcher icon) |
| `assets/logo-wordmark.svg` | Wordmark only |
| `assets/logo-lockup.svg` | Mark + wordmark |
| `assets/favicon.svg` | Browser tab |
| `assets/app-icon-512.svg` | Wrapper; PNG twin is `logo-mark.png` |
| `assets/readme-hero.jpg` | Photorealistic README banner |
| `assets/readme-hero.svg` | Thin SVG wrapper around `readme-hero.jpg` |
| `assets/social-preview.svg` | GitHub / OG 1280×640 (upload PNG export in repo Settings → Social preview) |
## Clear space & contrast

- Keep at least 1/8 of the mark’s width as padding around the mark.
- Prefer mark-on-dark (`#1a1a2e`) or mono mark on light surfaces.
- Check contrast for primary on surface in both light and dark themes after token edits.

## Rebrand checklist

1. Update `meta.name` and colors in `design-tokens/design-tokens.json`.
2. Replace SVGs under `branding/assets/` (keep filenames).
3. Fill `branding/product.json` (set `"mode": "product"` in child repos).
4. Run `python3 scripts/sync-design-tokens.py`.
5. Run `python3 scripts/generate-project-readme.py`.
6. Align UI strings (`locales` / `strings.xml`), `manifest.webmanifest`, and GitHub About.
7. Export PNGs for F-Droid / social preview when ready — do not commit large binaries unless intentional.

## Template vs product README

- `"mode": "template"` (default here) — generator writes only `generated/README.preview.md`; root `README.md` stays the template guide.
- `"mode": "product"` — generator overwrites root `README.md` with the pitch README. **Never** set this on the upstream template.

## Store listing sizes

See [`examples/android/metadata/en-US/images/README.md`](../examples/android/metadata/en-US/images/README.md) for `icon.png` and `featureGraphic.png`. Source art: `app-icon-512.svg` and `social-preview.svg`.
