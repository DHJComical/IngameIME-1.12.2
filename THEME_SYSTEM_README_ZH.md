# IngameIME 主题系统

## 概述

IngameIME 主题系统是一个灵活、可扩展的UI定制框架，允许用户完全自定义输入法界面的外观。该系统支持实时预览、动态切换和易于分享的主题配置文件。

## 主题属性详解

每个主题包含以下可配置的UI属性：

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
   - **选择主题**：从下拉列表中选择现有主题
   - **修改颜色**：点击颜色按钮选择新颜色
   - **调整数值**：修改内边距、边框宽度等数值
   - **实时预览**：右侧预览区域即时显示效果
   - **应用主题**：点击 `应用主题` 保存并应用更改
   - **创建新主题**：点击 `创建新主题` 输入名称创建自定义主题
   - **删除主题**：点击 `删除主题` 删除当前自定义主题（无法删除默认主题）

### 2. 通过配置文件使用

主题文件保存在 `config/ingameime/themes/` 目录中，格式为JSON：

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
  "borderWidth": 1
}
```

**重要提示**：
- 颜色值支持多种格式：`0xFF000000`、`#FF000000`、`FF000000`
- 主题ID必须唯一，且不能与内置主题冲突（default、dark、light）
- 修改配置文件后，在游戏中重新加载主题即可生效

### 3. 主题管理文件

系统会自动维护以下文件：
- `config/ingameime/themes/last_theme.txt`：记录上次使用的主题ID
- `config/ingameime/themes/*.json`：各个主题的配置文件

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

// 切换主题（立即生效）
themeManager.setTheme("light");

// 切换主题并通知所有监听器
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
themeManager.saveCustomTheme(customTheme);

// 删除自定义主题
themeManager.deleteCustomTheme("my_theme");

// 重新加载所有主题（例如修改配置文件后）
themeManager.reloadThemes();
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
    
    public void cleanup() {
        // 移除监听器
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
```

### 在UI组件中使用主题

```java
public class MyWidget extends Widget {
    
    public MyWidget() {
        // Widget基类已自动注册主题监听器
        // 可通过以下方法获取主题颜色
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int textColor = theme.getTextColor();
        int bgColor = theme.getBackgroundColor();
        int padding = theme.getPadding();
    }
    
    @Override
    public void draw() {
        // 使用主题颜色绘制
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        drawRect(x, y, x + width, y + height, theme.getBackgroundColor());
        drawString(text, x + theme.getPadding(), y + theme.getPadding(), theme.getTextColor());
    }
}
```

### 文件结构

```
src/main/java/com/dhj/ingameime/theme/
├── Theme.java              # 主题数据类
├── ThemeManager.java       # 主题管理器（单例）
├── ColorTypeAdapter.java   # Gson颜色类型适配器
├── ThemeEditorGui.java     # 主题编辑器GUI
└── ThemeNameInputGui.java  # 主题名称输入GUI

src/main/java/com/dhj/ingameime/gui/
├── Widget.java             # UI组件基类（支持主题）
├── WidgetCandidateList.java # 候选列表组件
└── WidgetPreEdit.java      # 预编辑文本组件

src/main/java/com/dhj/ingameime/config/
├── ThemeEditorConfigElement.java # 配置界面中的主题编辑器入口
└── ThemeEditorEntry.java         # 主题编辑器配置项

config/ingameime/themes/
├── default.json           # 默认主题（自动生成）
├── dark.json             # 深色主题（自动生成）
├── light.json            # 浅色主题（自动生成）
├── last_theme.txt        # 上次使用的主题ID
└── *.json               # 用户自定义主题
```

## 故障排除

### 常见问题

1. **主题不生效**
   - 检查主题文件格式是否正确（有效的JSON）
   - 确认颜色值格式正确（支持 `0x...`、`#...`、纯十六进制）
   - 查看游戏日志是否有加载错误

2. **颜色显示异常**
   - 检查透明度通道（前2位十六进制）
   - 确保颜色值在有效范围内（0x00000000 - 0xFFFFFFFF）
   - 验证颜色值是否为32位整数

### 调试信息

启用调试日志查看主题系统运行状态：

```java
// 在代码中启用调试
IngameIME_Forge.logDebugInfo("主题加载: " + themeId);

// 或通过配置文件启用
// config/ingameime.cfg 中设置 DebugLog = true
```

## 贡献指南

欢迎为主题系统贡献代码或主题设计！

### 提交新主题

1. 在 `config/ingameime/themes/` 目录创建JSON文件
2. 确保包含所有必需的属性
3. 提供有意义的主题名称和ID
4. 测试在不同游戏场景下的显示效果
5. 提交Pull Request或分享主题文件

### 代码贡献

1. 遵循现有的代码风格和架构
2. 添加适当的注释和文档
3. 包含单元测试（如果适用）
4. 确保向后兼容性

### 主题分享

可以通过以下方式分享自定义主题：
- 直接分享JSON文件
- 发布到Mod社区
- 集成到主题包中

## 许可证

主题系统遵循与IngameIME相同的LGPL-2.1许可证。自定义主题文件不受许可证限制，可以自由分享和使用。

---

*最后更新：2026年2月22日*
*IngameIME-1.12.2 主题系统 v1.0*