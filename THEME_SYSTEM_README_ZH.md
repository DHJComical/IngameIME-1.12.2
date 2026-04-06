# IngameIME 主题系统（1.12.2）

## 概述

主题系统已改为**资源包路径方案**。

当前使用两个本地主题资源包：

- `run/resourcepacks/IngameIME_UserThemes`  
  由主题编辑器创建、保存、删除主题。
- `run/resourcepacks/IngameIME_ThirdPartyThemes`  
  用于手动放入第三方主题。

当前主题记录文件位置：

- `run/config/ingameime/last_theme.txt`

## 主题来源与覆盖优先级

按以下顺序加载（后加载可覆盖同 ID 主题）：

1. 已启用资源包中的 `assets/ingameime/themes/index.json`
2. 本地 `IngameIME_ThirdPartyThemes` 递归扫描（`**/theme.json`）
3. 本地 `IngameIME_UserThemes`（通过其 `index.json`）

最终覆盖优先级：

`UserThemes > ThirdPartyThemes > 已启用资源包 index`

## 目录结构

### 1）用户主题（编辑器管理）

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

`index.json` 示例：

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

### 2）第三方主题（手动放入）

```text
run/resourcepacks/IngameIME_ThirdPartyThemes/
├─ pack.mcmeta
└─ assets/ingameime/themes/
   └─ <任意层级目录>/
      └─ theme.json
```

第三方本地扫描不要求 `index.json`。

## 如何添加第三方主题

1. 在以下目录创建主题文件夹：  
   `run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/`
2. 放入 `theme.json` 和对应贴图文件。
3. 触发资源重载（或重新打开会触发主题重载的界面）。

示例：

```text
run/resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/test_advanced/
├─ theme.json
└─ DHJComical.png
```

## `theme.json` 最小示例

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

## 贴图路径规则

- `textureFile` 相对 `theme.json` 所在目录。
- 文件名大小写必须与实际文件一致（尤其在资源式加载场景中）。
- 出现贴图找不到时，先检查文件名、扩展名、路径层级。

## 说明

- `default` / `dark` / `light` 缺失时会自动写入 `IngameIME_UserThemes`。
- 主题编辑器只写 `IngameIME_UserThemes`。
- `ThemeManager.scanForNewThemes()` 当前实现为完整 `reloadThemes()`。

## 调试日志

在 `config/ingameime.cfg` 设置 `DebugLog = true`，查看 `run/logs/latest.log`。

常见日志：

```text
[ThemeManager] Reloading themes...
[ThemeManager] Loaded third-party-pack theme: test_advanced (...)
[ThemeManager] Loaded user-pack theme: default (...)
[ThemeManager] Reloaded N themes, current: ...
```
