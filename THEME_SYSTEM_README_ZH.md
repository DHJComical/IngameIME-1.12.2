# IngameIME 主题系统

## 概述

IngameIME 主题系统是一个灵活、可扩展的UI定制框架，允许用户完全自定义输入法界面的外观。该系统支持实时预览、动态切换、9-slice纹理渲染、装饰挂件系统以及易于分享的主题配置文件。

## 主题属性详解

每个主题包含以下可配置的UI属性：

### 基础属性

| 属性 | 描述 | 默认值 | 格式说明 |
|------|------|--------|----------|
| `id` | 主题唯一标识符 | `"default"` | 小写字母、数字、下划线 |
| `name` | 主题显示名称 | `"Default Theme"` | 任意字符串 |
| `textColor` | 文字颜色 | `0xFF000000` | 32位十六进制ARGB格式 |
| `backgroundColor` | 背景颜色 | `0xEBEBEBEB` | 32位十六进制ARGB格式 |
| `indexColor` | 候选词索引颜色 | `0xFF555555` | 32位十六进制ARGB格式 |
| `selectedBackgroundColor` | 选中项背景颜色 | `0xEBEBEBEB` | 32位十六进制ARGB格式 |
| `cursorColor` | 光标颜色 | `0xFF000000` | 32位十六进制ARGB格式 |
| `borderColor` | 边框颜色 | `0x80000000` | 32位十六进制ARGB格式 |
| `padding` | 内边距 | `3` | 像素值，最小为0 |
| `candidatePadding` | 候选框内边距 | `3` | 像素值，最小为0 |
| `borderWidth` | 边框宽度 | `1` | 像素值，最小为0 |

### 高级纹理属性（9-Slice渲染）

| 属性 | 描述 | 默认值 | 格式说明 |
|------|------|--------|----------|
| `textureFile` | 背景纹理文件名 | `""` | 主题文件夹中的文件名（如"bg.png"） |
| `u` | 纹理U坐标 | `0` | 纹理图集中的水平偏移 |
| `v` | 纹理V坐标 | `0` | 纹理图集中的垂直偏移 |
| `sliceSize` | 9-slice网格大小 | `0` | 9-slice网格尺寸（0=禁用9-slice） |
| `cornerSize` | 角落大小 | `0` | 9-slice中固定角落的大小 |
| `textureWidth` | 纹理宽度 | `256` | 完整纹理宽度 |
| `textureHeight` | 纹理高度 | `256` | 完整纹理高度 |

### 装饰系统

装饰系统允许你使用双锚点定位算法在UI组件上叠加外部图片。这可以实现高级视觉效果，如发光、徽章、指示器和装饰元素，这些元素会随父组件移动和缩放。

#### 理解装饰

装饰是单独的图片文件，渲染在主题化UI组件的上方（或后方）。它们使用双锚点系统：
- **父锚点 (`anchorPoint`)**：在父组件上的附着位置
- **图片锚点 (`imageAnchor`)**：装饰图片的哪个点与父锚点对齐

**使用场景：**
- 角落装饰和花纹
- 文字后方的发光效果
- 选中指示器
- 装饰性徽章
- 阴影/高光叠加层

#### 双锚点定位算法

最终位置计算公式：
```
最终位置 = 父锚点位置 + 偏移量 - 图片锚点偏移量
```

**分步计算：**
1. 根据 `anchorPoint` (0-8网格) 计算父锚点位置
2. 应用 `offsetX` 和 `offsetY` 得到目标点
3. 根据 `imageAnchor` 和图片尺寸计算图片锚点偏移量
4. 减去图片锚点偏移量以对齐图片的正确部分

**示例：右上角徽章**
```json
{
  "textureFile": "new_badge.png",
  "anchorPoint": 2,    // 父元素的右上角
  "imageAnchor": 0,    // 图片的左上角
  "offsetX": -5,       // 父元素右上角左侧5px
  "offsetY": 5,        // 父元素顶部下方5px
  "scale": 0.8,
  "alpha": 1.0,
  "target": "candidate"
}
```
结果：徽章出现在候选列表右上角附近，略微内嵌。

**示例：居中发光效果**
```json
{
  "textureFile": "glow.png",
  "anchorPoint": 4,    // 父元素中心
  "imageAnchor": 4,    // 图片中心
  "offsetX": 0,
  "offsetY": 0,
  "scale": 1.5,
  "alpha": 0.5,
  "target": "all"
}
```
结果：发光居中于组件，1.5倍大小，50%透明度。

#### 装饰属性说明

| 属性 | 描述 | 默认值 | 范围/格式 |
|------|------|--------|----------|
| `textureFile` | 装饰纹理文件 | `""` | 主题文件夹中的文件名 |
| `anchorPoint` | 父元素上的锚点（0-8） | `0` | 0=左上, 2=右上, 4=中心, 6=左下, 8=右下 |
| `imageAnchor` | 图片自身的锚点（0-8） | `0` | 同anchorPoint |
| `offsetX` | 水平偏移 | `0` | 像素值 |
| `offsetY` | 垂直偏移 | `0` | 像素值 |
| `scale` | 图片缩放 | `1.0` | 浮点数，> 0 |
| `alpha` | 透明度 | `1.0` | 0.0-1.0 |
| `target` | 目标组件 | `"all"` | "all", "candidate", "preedit"等 |
| `width` | 固定宽度（-1=自动） | `-1` | 像素值或-1 |
| `height` | 固定高度（-1=自动） | `-1` | 像素值或-1 |

**锚点布局（3x3网格）：**
```
0 --- 1 --- 2
|     |     |
3 --- 4 --- 5
|     |     |
6 --- 7 --- 8
```

#### 目标组件

`target` 属性控制哪些UI组件显示装饰：

| 目标值 | 描述 |
|--------|------|
| `"all"` | 所有组件（默认） |
| `"candidate"` | 仅候选列表 |
| `"preedit"` | 仅预编辑文本 |
| `"inputmode"` | 仅输入模式指示器 |

#### 配置示例

**角落装饰（右上）：**
```json
{
  "textureFile": "corner_flourish.png",
  "anchorPoint": 2,
  "imageAnchor": 2,
  "offsetX": -10,
  "offsetY": 10,
  "scale": 1.0,
  "alpha": 0.9,
  "target": "all"
}
```

**选中发光（选中项后方）：**
```json
{
  "textureFile": "selection_glow.png",
  "anchorPoint": 4,
  "imageAnchor": 4,
  "offsetX": 0,
  "offsetY": 0,
  "scale": 1.2,
  "alpha": 0.4,
  "target": "candidate"
}
```

**浮动指示器（文字上方）：**
```json
{
  "textureFile": "indicator.png",
  "anchorPoint": 1,
  "imageAnchor": 7,
  "offsetX": 0,
  "offsetY": -15,
  "scale": 0.6,
  "alpha": 1.0,
  "target": "preedit"
}
```

#### 常见装饰问题

1. **装饰不显示**
   - **原因**：`alpha` 为0，`scale` 为0，或纹理文件未找到
   - **解决**：检查 alpha > 0，scale > 0，验证纹理文件存在

2. **装饰位置错误**
   - **原因**：anchorPoint 或 imageAnchor 值不正确
   - **解决**：可视化3x3网格，测试不同的锚点组合

3. **装饰被裁剪/截断**
   - **原因**：装饰超出裁剪框或屏幕边界
   - **解决**：减小缩放，调整偏移量，或使用更小的纹理

4. **多个装饰重叠顺序错误**
   - **原因**：渲染顺序未控制
   - **解决**：装饰按数组顺序渲染；相应调整JSON数组顺序

5. **装饰太大/太小**
   - **原因**：缩放或纹理分辨率错误
   - **解决**：调整缩放，或调整源纹理到合适尺寸

### 颜色格式说明

颜色值使用32位ARGB格式：
- **A (Alpha)**：前2位十六进制，表示透明度（00=完全透明，FF=完全不透明）
- **R (Red)**：第3-4位十六进制，红色分量
- **G (Green)**：第5-6位十六进制，绿色分量
- **B (Blue)**：第7-8位十六进制，蓝色分量

示例：
- `0xFF000000`：完全不透明的黑色
- `0x80000000`：50%透明度的黑色
- `0xEBEBEBEB`：约92%透明度的浅灰色
- `0xFFFFFFFF`：完全不透明的白色

## 使用方法

### 1. 通过游戏内配置界面使用

1. 在游戏中按 `ESC` 打开菜单
2. 选择 `Mod选项` → `IngameIME` → `配置`
3. 在配置界面中点击 `主题编辑器` 按钮
4. 在主题编辑器中：
   - **选择主题**：点击 `选择主题` 按钮打开主题选择界面
   - **修改颜色**：在文本框中编辑十六进制颜色值
   - **调整数值**：修改内边距、边框宽度等数值
   - **实时预览**：颜色预览框即时显示效果
   - **应用主题**：点击 `应用` 保存并应用更改
   - **创建新主题**：点击 `创建新主题` 输入名称创建自定义主题
   - **删除主题**：点击 `删除` 删除当前自定义主题（无法删除默认主题）

### 2. 通过配置文件使用

主题文件保存在 `config/ingameime/themes/<theme_id>/` 目录中，格式为JSON：

```json
{
  "id": "my_custom_theme",
  "name": "我的自定义主题",
  "textColor": "0xFF000000",
  "backgroundColor": "0xEBEBEBEB",
  "indexColor": "0xFF555555",
  "selectedBackgroundColor": "0xEBEBEBEB",
  "cursorColor": "0xFF000000",
  "borderColor": "0x80000000",
  "padding": 3,
  "candidatePadding": 3,
  "borderWidth": 1,
  "textureFile": "background.png",
  "u": 0,
  "v": 0,
  "sliceSize": 24,
  "cornerSize": 8,
  "textureWidth": 256,
  "textureHeight": 256,
  "decorations": [
    {
      "textureFile": "corner_deco.png",
      "anchorPoint": 2,
      "imageAnchor": 2,
      "offsetX": 5,
      "offsetY": 5,
      "scale": 1.0,
      "alpha": 0.8,
      "target": "all"
    }
  ]
}
```

**文件结构：**
```
config/ingameime/themes/
├── default/
│   └── theme.json
├── dark/
│   └── theme.json
├── light/
│   └── theme.json
├── my_custom_theme/
│   ├── theme.json
│   ├── background.png
│   └── corner_deco.png
└── last_theme.txt
```

**重要提示**：
- 颜色值支持多种格式：`0xFF000000`、`#FF000000`、`FF000000`
- 主题ID必须唯一，且不能与内置主题冲突（default、dark、light）
- 纹理文件必须放在与 `theme.json` 相同的文件夹中
- 修改配置文件后，在游戏中重新加载主题即可生效

### 3. 9-Slice纹理渲染

9-slice（或称9-patch）渲染系统允许创建可缩放的纹理背景，在拉伸边缘和中心的同时保持角落清晰。这对于需要动态调整大小同时保持视觉质量的UI元素至关重要。

#### 理解9-Slice

9-slice纹理被划分为9个区域：

```
+--------+--------+--------+
| 角落   |  边缘  | 角落   |  ← 第1行：固定高度 (cornerSize)
|  (1)   |  (2)   |  (3)   |
+--------+--------+--------+
| 边缘   |  中心  | 边缘   |  ← 第2行：垂直拉伸
|  (4)   |  (5)   |  (6)   |
+--------+--------+--------+
| 角落   |  边缘  | 角落   |  ← 第3行：固定高度 (cornerSize)
|  (7)   |  (8)   |  (9)   |
+--------+--------+--------+
   ↑                      ↑
   └─ 固定宽度            └─ 固定宽度
      (cornerSize)           (cornerSize)
```

**拉伸行为：**
- **角落 (1,3,7,9)**：永不拉伸，保持原始大小
- **边缘 (2,4,6,8)**：单向拉伸（水平或垂直）
- **中心 (5)**：双向拉伸以填充剩余空间

#### 准备9-Slice纹理

**步骤1：设计纹理**
- 创建方形图片（如64x64、128x128、256x256）
- 设计带有边框和角落的UI框架/背景
- 确保角落对称以获得最佳效果

**步骤2：定义切片参数**
- `sliceSize`：9-slice网格的总尺寸（通常是完整纹理尺寸）
- `cornerSize`：角落区域的大小（必须小于sliceSize/2）

**纹理布局示例（64x64，16px角落）：**
```
+------+------------------+------+
| 16x16|     32x16        | 16x16|  ← 上角 + 上边缘
|      |   (水平拉伸)     |      |
|      |                  |      |
+------+------------------+------+
| 16x32|     32x32        | 16x32|  ← 中间行
|      |   (双向拉伸)     |      |
|(固定  |                  |(固定 |
|水平)  |                  |水平) |
+------+------------------+------+
| 16x16|     32x16        | 16x16|  ← 下角 + 下边缘
|      |   (水平拉伸)     |      |
|      |                  |      |
+------+------------------+------+
```

**步骤3：在theme.json中配置**
```json
{
  "textureFile": "panel_bg.png",
  "u": 0,
  "v": 0,
  "sliceSize": 64,
  "cornerSize": 16,
  "textureWidth": 64,
  "textureHeight": 64
}
```

#### 配置示例

**简单面板（64x64纹理，8px角落）：**
```json
{
  "textureFile": "simple_panel.png",
  "sliceSize": 64,
  "cornerSize": 8,
  "textureWidth": 64,
  "textureHeight": 64
}
```

**精美边框（128x128纹理，24px角落带发光效果）：**
```json
{
  "textureFile": "fancy_border.png",
  "sliceSize": 128,
  "cornerSize": 24,
  "textureWidth": 128,
  "textureHeight": 128
}
```

**纹理图集（256x256图集包含多个UI元素）：**
```json
{
  "textureFile": "ui_atlas.png",
  "u": 64,
  "v": 0,
  "sliceSize": 64,
  "cornerSize": 12,
  "textureWidth": 256,
  "textureHeight": 256
}
```

#### 常见9-Slice问题

1. **边缘可见接缝**
   - **原因**：cornerSize太大或纹理出血
   - **解决**：减小cornerSize，为纹理添加1px透明边框

2. **角落被拉伸**
   - **原因**：cornerSize >= sliceSize/2
   - **解决**：确保cornerSize < sliceSize/2（例如64px切片，最大角落为31px）

3. **纹理缩放时模糊**
   - **原因**：纹理过滤或尺寸错误
   - **解决**：使用2的幂次方纹理（64、128、256），启用最近邻过滤

4. **使用了错误的区域**
   - **原因**：图集中u/v坐标不正确
   - **解决**：验证u,v指向纹理图集中的正确区域

5. **9-Slice不渲染**
   - **原因**：sliceSize为0或textureFile未找到
   - **解决**：设置sliceSize > 0，验证纹理文件存在于主题文件夹中

### 4. 主题管理文件

系统会自动维护以下文件：
- `config/ingameime/themes/last_theme.txt`：记录上次使用的主题ID
- `config/ingameime/themes/<theme_id>/theme.json`：各个主题的配置文件
- `config/ingameime/themes/<theme_id>/*.png`：各个主题的纹理文件

## 预定义主题

### 1. 默认主题 (default)
- **文字颜色**：黑色 (`0xFF000000`)
- **背景颜色**：浅灰色半透明 (`0xEBEBEBEB`)
- **适用场景**：通用设计，适合大多数游戏界面
- **特点**：良好的可读性和适度的透明度

### 2. 深色主题 (dark)
- **文字颜色**：白色 (`0xFFFFFFFF`)
- **背景颜色**：深灰色半透明 (`0x80333333`)
- **适用场景**：暗色游戏界面、夜间使用
- **特点**：低亮度，减少视觉干扰

### 3. 浅色主题 (light)
- **文字颜色**：黑色 (`0xFF000000`)
- **背景颜色**：白色半透明 (`0xF0FFFFFF`)
- **适用场景**：亮色游戏界面、白天使用
- **特点**：高对比度，清晰易读

## 开发者API

### 获取主题管理器实例

```java
ThemeManager themeManager = ThemeManager.getInstance();
```

### 获取和切换主题

```java
// 获取当前主题
Theme currentTheme = themeManager.getCurrentTheme();

// 根据ID获取特定主题
Theme darkTheme = themeManager.getTheme("dark");

// 切换主题并通知所有监听器（立即生效）
themeManager.setThemeAndNotify("custom_theme");
```

### 主题管理操作

```java
// 获取所有可用主题
Map<String, Theme> allThemes = themeManager.getAvailableThemes();

// 创建并保存自定义主题
Theme customTheme = Theme.createCustomTheme("my_theme", "我的主题");
customTheme.setTextColor(0xFF3366CC);  // 修改颜色
customTheme.setPadding(5);             // 修改内边距
customTheme.setTextureFile("bg.png");  // 设置纹理
themeManager.saveCustomTheme(customTheme);

// 删除自定义主题
themeManager.deleteCustomTheme("my_theme");

// 重新加载所有主题（例如修改配置文件后）
themeManager.reloadThemes();

// 扫描新主题而不完全重新加载
themeManager.scanForNewThemes();
```

### 主题变更监听器

```java
public class MyUIComponent implements ThemeManager.ThemeChangeListener {
    
    public MyUIComponent() {
        // 注册主题变更监听器
        ThemeManager.getInstance().addThemeChangeListener(this);
    }
    
    @Override
    public void onThemeChanged(Theme newTheme) {
        // 主题变更时自动调用
        updateColors(newTheme);
        refreshDisplay();
    }
}
```

### 在自定义组件中使用ThemeRenderer

```java
public class MyCustomWidget {
    
    public void draw(int x, int y, int width, int height) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        
        // 渲染带装饰的主题背景
        ThemeRenderer.render(theme, x, y, width, height, "my_component");
        
        // 使用主题颜色绘制文字
        drawString(text, x + theme.getPadding(), y + theme.getPadding(), 
                   theme.getTextColor());
    }
}
```

### 以编程方式创建装饰

```java
ThemeDecoration decoration = new ThemeDecoration();
decoration.textureFile = "glow.png";
decoration.anchorPoint = 4;  // 父元素中心
decoration.imageAnchor = 4;  // 图片中心
decoration.offsetX = 0;
decoration.offsetY = -20;  // 中心上方20像素
decoration.scale = 1.5f;
decoration.alpha = 0.6f;
decoration.target = "candidate";  // 只在候选列表上显示

theme.getDecorations().add(decoration);
themeManager.saveCustomTheme(theme);
```

### 文件结构

```
src/main/java/com/dhj/ingameime/theme/
├── api/
│   ├── Theme.java              # 主题数据类
│   ├── ThemeManager.java       # 主题管理器（单例）
│   ├── ThemeRenderer.java      # 主题渲染（9-slice + 装饰）
│   └── ThemeDecoration.java    # 装饰挂件数据
├── ColorTypeAdapter.java       # Gson颜色类型适配器
├── ThemeEditorGui.java         # 主题编辑器GUI
├── ThemeNameInputGui.java      # 主题名称输入GUI
├── ThemeSelectionGui.java      # 主题选择GUI
└── ThemeType.java              # 主题类型枚举

src/main/java/com/dhj/ingameime/gui/
├── Widget.java                 # UI组件基类（支持主题）
├── WidgetCandidateList.java    # 候选列表组件
├── WidgetInputMode.java        # 输入模式指示器
└── WidgetPreEdit.java          # 预编辑文本组件

config/ingameime/themes/
├── default/theme.json          # 默认主题（自动生成）
├── dark/theme.json             # 深色主题（自动生成）
├── light/theme.json            # 浅色主题（自动生成）
├── last_theme.txt              # 上次使用的主题ID
└── */                          # 用户自定义主题及资源
```

## 扩展性设计

### 添加新主题属性

1. 在 `Theme.java` 中添加新字段和getter/setter
2. 在 `ThemeEditorGui.java` 中添加相应的编辑控件
3. 如果属性影响渲染，更新 `ThemeRenderer.java`
4. 更新文档

### 自定义主题序列化

修改 `ColorTypeAdapter.java` 以支持更多颜色格式，或为其他属性创建自定义适配器。

### 主题导入/导出

扩展 `ThemeManager` 以添加主题导入/导出功能，便于主题分享：

```java
// 导出主题为zip
public void exportTheme(String themeId, File destination);

// 从zip导入主题
public Theme importTheme(File source);
```

## 故障排除

### 常见问题

1. **主题不生效**
   - 检查主题文件格式是否正确（有效的JSON）
   - 确认颜色值格式正确（支持 `0x...`、`#...`、纯十六进制）
   - 查看游戏日志是否有加载错误
   - 验证主题文件夹结构（`themes/<theme_id>/theme.json`）

2. **纹理不显示**
   - 验证纹理文件是否存在于主题文件夹中
   - 检查 `textureFile` 名称是否与实际文件名匹配（区分大小写）
   - 确保 `sliceSize` > 0 以启用9-slice渲染
   - 验证 `textureWidth` 和 `textureHeight` 与实际图片尺寸匹配

3. **装饰不显示**
   - 检查装饰 `textureFile` 是否存在
   - 验证 `alpha` > 0
   - 检查 `target` 是否匹配组件ID或使用 `"all"`
   - 检查锚点计算

4. **颜色显示异常**
   - 检查透明度通道（前2位十六进制）
   - 确保颜色值在有效范围内（0x00000000 - 0xFFFFFFFF）
   - 验证颜色值是否为32位整数

### 调试信息

启用调试日志查看主题系统运行状态：

```java
// 当DebugLog启用时，IngameIME会自动记录主题操作
// 在 config/ingameime.cfg 中设置 DebugLog = true
```

**日志输出示例：**
```
[ThemeManager] Reloading themes...
[ThemeManager] Loaded theme: default (Default Theme)
[ThemeManager] Loaded theme: dark (Dark Theme)
[ThemeManager] Reloaded 3 themes, current: default
[ThemeManager] Theme switched to: dark (Dark Theme)
[ThemeRenderer] Rendering ID: dark | Texture: false | Slice: 0
[ThemeEditor] Theme saved and applied: my_theme (My Custom Theme)
```

## 贡献指南

欢迎为主题系统贡献代码或主题设计！

### 提交新主题

1. 在 `config/ingameime/themes/` 目录创建主题文件夹
2. 创建包含所有必需属性的 `theme.json`
3. 如使用自定义图形，添加纹理文件
4. 测试在不同游戏场景下的显示效果
5. 提交Pull Request或分享主题文件

### 代码贡献

1. 遵循现有的代码风格和架构
2. 添加适当的注释和文档
3. 包含单元测试（如果适用）
4. 确保向后兼容性
5. 同时更新英文和中文README文件

### 主题分享

可以通过以下方式分享自定义主题：
- 直接分享文件夹（将主题文件夹打包为zip）
- 发布到Mod社区
- 集成到主题包中

## 许可证

主题系统遵循与IngameIME相同的LGPL-2.1许可证。自定义主题文件不受许可证限制，可以自由分享和使用。

---

*最后更新：2026年2月22日*
*IngameIME-1.12.2 主题系统 v2.0*
