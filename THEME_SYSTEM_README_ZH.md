# IngameIME 主题系统 v3（1.12.2）

## 概述

主题系统已改为**资源包路径方案**。

> 版本声明：本文档描述的是 **Theme System v3**。

当前使用两个本地主题资源包：

- `resourcepacks/IngameIME_UserThemes`  
  由主题编辑器创建、保存、删除主题。
- `resourcepacks/IngameIME_ThirdPartyThemes`  
  用于手动放入第三方主题。

当前主题记录文件位置：

- `config/ingameime/last_theme.txt`

## 主题来源与覆盖优先级

按以下顺序加载（后加载可覆盖同 ID 主题）：

1. 已启用资源包中的 `assets/ingameime/themes/index.json`
2. 本地 `IngameIME_ThirdPartyThemes` 递归扫描（`**/theme.json`）
3. 本地 `IngameIME_UserThemes`（通过其 `index.json`）

最终覆盖优先级：

`UserThemes > ThirdPartyThemes > 已启用资源包 index`

## 导入资源包自动检测

除上述两个本地主题包外，系统还会自动扫描放入以下目录的资源包：

- `resourcepacks/`

支持形式：

- 目录资源包
- `.zip` 资源包

导入包内要求路径：

- `assets/ingameime/themes/**/theme.json`

说明：

- zip 文件名**不需要固定**（任意名称都可）。
- 该自动扫描路径下 `index.json` 可选（没有也可识别）。
- 以下内部保留包名会在“导入包扫描”中跳过：
  - `IngameIME_UserThemes` / `IngameIME_UserThemes.zip`
  - `IngameIME_ThirdPartyThemes` / `IngameIME_ThirdPartyThemes.zip`

## 目录结构

### 1）用户主题（编辑器管理）

```text
resourcepacks/IngameIME_UserThemes/
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
resourcepacks/IngameIME_ThirdPartyThemes/
├─ pack.mcmeta
└─ assets/ingameime/themes/
   └─ <任意层级目录>/
      └─ theme.json
```

第三方本地扫描不要求 `index.json`。

## 如何添加第三方主题

1. 在以下目录创建主题文件夹：  
   `resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/`
2. 放入 `theme.json` 和对应贴图文件。
3. 触发资源重载（或重新打开会触发主题重载的界面）。

示例：

```text
resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/test_advanced/
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

## 主题包详细教程

这里是从零开始制作主题包的完整流程，适合直接照做。

### 第 1 步：选目标目录

二选一：

- 编辑器管理主题（推荐）：
  `resourcepacks/IngameIME_UserThemes/assets/ingameime/themes/<theme_id>/`
- 手动第三方主题：
  `resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/<theme_id>/`
- 导入资源包（目录或 zip，名称不限）：
  `resourcepacks/<任意包名>/assets/ingameime/themes/<theme_id>/theme.json`
  或 `<任意包名>.zip`（内部同路径）

第三方本地扫描不需要 `index.json`。

### 第 2 步：准备文件结构

建议目录：

```text
<theme_id>/
├─ theme.json
├─ skin.png              （可选，背景/9-slice贴图）
├─ DHJComical.png        （可选，装饰贴图）
└─ other_*.png           （可选）
```

### 第 3 步：写完整 `theme.json`

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

### 第 4 步：9-slice 速记

- `sliceSize > 0` 才会走 9-slice。
- `cornerSize` 是固定角区域尺寸。
- `textureWidth/textureHeight` 应与实际图片尺寸一致。
- 只想纯色背景：`textureFile` 设空字符串，`sliceSize` 设 `0`。

### 第 5 步：装饰锚点速记

锚点网格：

```text
0 --- 1 --- 2
|     |     |
3 --- 4 --- 5
|     |     |
6 --- 7 --- 8
```

位置公式：

`最终位置 = 父锚点 + 偏移 - 图片锚点偏移`

常用组合：

- 右上角徽章：`anchorPoint=2`，`imageAnchor=0 或 2`
- 居中光效：`anchorPoint=4`，`imageAnchor=4`
- 右下角贴纸：`anchorPoint=8`，`imageAnchor=8`

### 第 6 步：重载与验证

- 资源重载，或触发主题重载的 GUI 操作。
- 开启 `DebugLog = true`，查看 `run/logs/latest.log`。

预期日志：

```text
[ThemeManager] Reloading themes...
[ThemeManager] Loaded third-party-pack theme: <theme_id> (...)
[ThemeRenderer] Rendering ID: <theme_id> | Texture: ... | Slice: ...
```

### 第 7 步：常见问题

1. 主题列表里看不到
- `theme.json` 非法 JSON。
- `id` 与已有主题冲突。
- 路径不对，必须是 `.../themes/<theme_id>/theme.json`。

2. 装饰贴图找不到
- 文件名大小写不一致。
- 贴图不在 `theme.json` 同级（或相对路径错误）。
- 典型日志：`Decoration texture not found: <id>:DHJComical.png`

3. 背景贴图不显示
- `textureFile` 为空或文件不存在。
- `sliceSize` 没有大于 0。
- `textureWidth/textureHeight` 与实际图片不一致。

4. 装饰位置/大小不对
- `anchorPoint` 与 `imageAnchor` 组合不对。
- 需要微调 `offsetX/offsetY`、`scale`。
- `target` 目标组件不正确（`all`/`candidate`/`preedit`/`inputmode`）。

### 第 8 步：分发主题

支持两种 zip 分发方式：

1. **单主题 zip**
- zip 根目录是 `<theme_id>/`
- 目录内必须包含 `theme.json`（以及贴图）

```text
<theme_id>.zip
└─ <theme_id>/
   ├─ theme.json
   └─ *.png
```

接收方操作：
- 解压到 `resourcepacks/IngameIME_ThirdPartyThemes/assets/ingameime/themes/`

2. **完整资源包 zip（可直接导入）**
- zip 文件名可任意
- zip 根目录必须包含 `assets/ingameime/themes/`

```text
任意包名.zip
└─ assets/ingameime/themes/
   └─ <theme_id>/
      ├─ theme.json
      └─ *.png
```

接收方操作：
- 直接把 zip 放到 `resourcepacks/`（无需解压）

两种方式都需要在游戏内重载资源/主题后生效。

