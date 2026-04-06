# IngameIME Theme System v3 (1.12.2)

## Overview

The theme system is now **resource-pack based**.

> Version declaration: This document describes **Theme System v3**.

Two local theme packs are used:

- `run/resourcepacks/IngameIME_UserThemes`  
  Used by the in-game Theme Editor (create/save/delete custom themes).
- `run/resourcepacks/IngameIME_ThirdPartyThemes`  
  Used for manually dropped third-party themes.

`last_theme.txt` is stored at:

- `run/config/ingameime/last_theme.txt`

## Theme Sources and Priority

Themes are loaded in this order (later overrides earlier if IDs are the same):

1. Enabled resource packs via `assets/ingameime/themes/index.json`
2. `IngameIME_ThirdPartyThemes` local folder scan (`**/theme.json`)
3. `IngameIME_UserThemes` via `index.json`

So final override priority is:

`UserThemes > ThirdPartyThemes > Enabled resource-pack index themes`

## Imported Resource Pack Auto-Detection

In addition to the two local packs, the loader now auto-scans any resource pack dropped into:

- `run/resourcepacks/`

Supported forms:

- folder pack
- `.zip` pack

Required structure inside the imported pack:

- `assets/ingameime/themes/**/theme.json`

Notes:

- Zip file name is **not fixed** (any name is accepted).
- `index.json` is optional for this auto-scan path.
- Reserved internal pack names are skipped by imported-pack scan:
  - `IngameIME_UserThemes` / `IngameIME_UserThemes.zip`
  - `IngameIME_ThirdPartyThemes` / `IngameIME_ThirdPartyThemes.zip`

## Folder Layout

### 1) User Themes (editor-managed)

```text
run/resourcepacks/IngameIME_UserThemes/
├─ pack.mcmeta
└─ assets/ingameime/themes/
   ├─ index.json
   ├─ default/theme.json
   ├─ dark/theme.json
   ├─ light/theme.json
   └─ <your_theme_id>/
      ├─ theme.json
      └─ *.png
```

`index.json` example:

```json
{
  "themes": [
    "default/theme.json",
    "dark/theme.json",
    "light/theme.json",
    "my_theme/theme.json"
  ]
}
```

### 2) Third-party Themes (manual drop-in)

```text
run/resourcepacks/IngameIME_ThirdPartyThemes/
├─ pack.mcmeta
└─ assets/ingameime/themes/
   └─ <any_folder_depth>/
      └─ theme.json
```

No `index.json` is required for third-party local folder scanning.

## Adding a Third-party Theme

1. Create a folder under:
   `run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/`
2. Put `theme.json` and texture files in that folder.
3. Reload resources (or reopen GUI that triggers theme reload).

Example:

```text
run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/test_advanced/
├─ theme.json
└─ DHJComical.png
```

## `theme.json` Minimal Example

```json
{
  "id": "test_advanced",
  "name": "Advanced Test Theme",
  "textColor": "0xB5FFFFFF",
  "backgroundColor": "0x80000000",
  "indexColor": "0xFFFFFFFF",
  "selectedBackgroundColor": "0x40FFFFFF",
  "cursorColor": "0xFFFFFFFF",
  "borderColor": "0x00000000",
  "padding": 3,
  "candidatePadding": 3,
  "borderWidth": 0,
  "textureFile": ""
}
```

## Texture Path Rules

- Texture file paths are relative to the folder containing `theme.json`.
- File name is case-sensitive in resource-style loading contexts.
- If a texture is not found, check exact file name and extension first.

## Notes

- Default themes (`default`, `dark`, `light`) are auto-created in `IngameIME_UserThemes` if missing.
- Theme Editor saves to `IngameIME_UserThemes` only.
- `ThemeManager.scanForNewThemes()` performs a full `reloadThemes()`.

## Debug Log Tips

Enable `DebugLog = true` in `config/ingameime.cfg`, then check `run/logs/latest.log`.

Typical lines:

```text
[ThemeManager] Reloading themes...
[ThemeManager] Loaded third-party-pack theme: test_advanced (...)
[ThemeManager] Loaded user-pack theme: default (...)
[ThemeManager] Reloaded N themes, current: ...
```

## Detailed Theme Pack Tutorial

This section is a practical end-to-end guide for making and validating a complete theme pack.

### Step 1: Choose a target location

Use one of the following:

- Editor-managed theme (recommended for in-game editing):
  `run/resourcepacks/IngameIME_UserThemes/assets/ingameime/themes/<theme_id>/`
- Manual third-party theme:
  `run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/<theme_id>/`
- Imported pack (folder or zip, file name not fixed):
  `run/resourcepacks/<any_pack_name>/assets/ingameime/themes/<theme_id>/theme.json`
  or `<any_pack_name>.zip` with the same internal path

For third-party local pack scanning, no `index.json` is required.

### Step 2: Prepare files

Create this structure:

```text
<theme_id>/
├─ theme.json
├─ skin.png              (optional, background/9-slice texture)
├─ DHJComical.png        (optional, decoration texture)
└─ other_*.png           (optional)
```

### Step 3: Write a full `theme.json`

```json
{
  "id": "test_advanced",
  "name": "Advanced Test Theme",
  "textColor": "0xB5FFFFFF",
  "backgroundColor": "0x80000000",
  "indexColor": "0xFFFFFFFF",
  "selectedBackgroundColor": "0x40FFFFFF",
  "cursorColor": "0xFFFFFFFF",
  "borderColor": "0x00000000",
  "padding": 3,
  "candidatePadding": 3,
  "borderWidth": 0,

  "u": 0,
  "v": 0,
  "sliceSize": 100,
  "cornerSize": 16,
  "textureWidth": 100,
  "textureHeight": 100,
  "textureFile": "skin.png",

  "decorations": [
    {
      "textureFile": "DHJComical.png",
      "anchorPoint": 2,
      "imageAnchor": 8,
      "offsetX": 0,
      "offsetY": 0,
      "scale": 0.02,
      "alpha": 0.6,
      "target": "candidate",
      "width": 0,
      "height": 0
    }
  ]
}
```

### Step 4: Understand 9-slice quickly

- `sliceSize > 0` enables 9-slice rendering.
- `cornerSize` is the fixed corner region size.
- `textureWidth/textureHeight` should match actual texture dimensions.
- If you only want flat color background, set `textureFile` to empty string and `sliceSize` to `0`.

### Step 5: Understand decoration anchors

Anchor grid:

```text
0 --- 1 --- 2
|     |     |
3 --- 4 --- 5
|     |     |
6 --- 7 --- 8
```

Position formula:

`Final = ParentAnchor + (offsetX, offsetY) - ImageAnchorOffset`

Useful presets:

- Top-right badge: `anchorPoint=2`, `imageAnchor=0 or 2`
- Center overlay/glow: `anchorPoint=4`, `imageAnchor=4`
- Bottom-right sticker: `anchorPoint=8`, `imageAnchor=8`

### Step 6: Reload and verify

- Reload resources (or trigger theme reload from GUI).
- Turn on debug log (`DebugLog = true`) and check `run/logs/latest.log`.

Expected lines:

```text
[ThemeManager] Reloading themes...
[ThemeManager] Loaded third-party-pack theme: <theme_id> (...)
[ThemeRenderer] Rendering ID: <theme_id> | Texture: ... | Slice: ...
```

### Step 7: Common failures and fixes

1. Theme not listed
- Check `theme.json` is valid JSON.
- Check `id` is unique.
- Check path ends with `.../themes/<theme_id>/theme.json`.

2. Decoration texture not found
- Verify exact file name and case.
- Ensure texture is in the same folder as `theme.json` (or correct relative path).
- Example log: `Decoration texture not found: <id>:DHJComical.png`

3. No texture rendering
- Ensure `textureFile` is non-empty and file exists.
- Ensure `sliceSize > 0` for 9-slice path.
- Ensure `textureWidth/textureHeight` match real image size.

4. Decoration appears in wrong position/size
- Re-check `anchorPoint` and `imageAnchor` combination.
- Tune `offsetX/offsetY` and `scale`.
- Check `target` (`all`, `candidate`, `preedit`, `inputmode`).

### Step 8: Share your theme

You can distribute in either zip format:

1. **Single-theme zip (recommended)**
- Zip content root is `<theme_id>/`
- Inside it must include `theme.json` (and textures)

```text
<theme_id>.zip
└─ <theme_id>/
   ├─ theme.json
   └─ *.png
```

Receiver action:
- Unzip to `run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/`

2. **Full resource-pack zip (direct import)**
- Zip file name can be any name
- Zip root must contain `assets/ingameime/themes/`

```text
AnyName.zip
└─ assets/ingameime/themes/
   └─ <theme_id>/
      ├─ theme.json
      └─ *.png
```

Receiver action:
- Put zip directly into `run/resourcepacks/` (no unpack needed)

After either method, reload resources/themes.

