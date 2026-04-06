# IngameIME Theme System (1.12.2)

## Overview

The theme system is now **resource-pack based**.

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
