# IngameIME 主题系统

## 概述

IngameIME 现在支持完整的主题系统，允许用户自定义输入法界面的外观。主题系统支持：

1. **多种预定义主题**：默认、深色、浅色
2. **自定义主题**：用户可以创建自己的主题
3. **实时预览**：在主题编辑器中实时预览颜色变化
4. **配置文件支持**：主题保存为JSON文件，易于编辑和分享
5. **动态切换**：无需重启游戏即可切换主题

## 主题属性

每个主题包含以下可配置属性：

| 属性 | 描述 | 默认值 |
|------|------|--------|
| `textColor` | 文字颜色 | `0xFF000000` (黑色) |
| `backgroundColor` | 背景颜色 | `0xEBEBEBEB` (浅灰色半透明) |
| `indexColor` | 候选词索引颜色 | `0xFF555555` (深灰色) |
| `selectedBackgroundColor` | 选中项背景颜色 | `0xEBEBEBEB` (浅灰色半透明) |
| `cursorColor` | 光标颜色 | `0xFF000000` (黑色) |
| `padding` | 内边距 | `3` 像素 |
| `candidatePadding` | 候选框内边距 | `5` 像素 |
| `borderWidth` | 边框宽度 | `1` 像素 |
| `borderColor` | 边框颜色 | `0x80000000` (黑色半透明) |

## 使用方法

### 1. 通过配置界面使用

1. 在游戏中打开 Mod 配置界面
2. 点击 "主题编辑器" 按钮
3. 在主题编辑器中：
   - 选择现有主题
   - 修改颜色值（支持8位十六进制，如 `FF000000`）
   - 点击 "应用主题" 保存更改
   - 点击 "创建新主题" 创建自定义主题
   - 点击 "删除主题" 删除自定义主题（不能删除默认主题）

### 2. 通过配置文件使用

主题文件保存在 `config/ingameime/themes/` 目录中，格式为 JSON：

```json
{
  "id": "my_theme",
  "name": "我的主题",
  "textColor": -16777216,
  "backgroundColor": -337222421,
  "indexColor": -11184811,
  "selectedBackgroundColor": -337222421,
  "cursorColor": -16777216,
  "padding": 3,
  "candidatePadding": 5,
  "borderWidth": 1,
  "borderColor": -2147483648
}
```

**注意**：颜色值使用十进制整数表示（Java int），可以通过在线工具将十六进制转换为十进制。

### 3. 修改配置文件

在 `config/ingameime.cfg` 中，可以设置默认主题：

```properties
# 主题配置
theme {
    # 当前使用的主题。可以设置为 'default', 'dark', 'light' 或自定义主题文件名（不带.json后缀）
    S:currentTheme=default
}
```

## 预定义主题

### 1. 默认主题 (default)
- 文字颜色：黑色
- 背景：浅灰色半透明
- 适合大多数游戏场景

### 2. 深色主题 (dark)
- 文字颜色：白色
- 背景：深灰色半透明
- 适合暗色界面

### 3. 浅色主题 (light)
- 文字颜色：黑色
- 背景：白色半透明
- 适合亮色界面

## 开发者API

### 获取当前主题

```java
ThemeManager themeManager = ThemeManager.getInstance();
Theme currentTheme = themeManager.getCurrentTheme();
```

### 切换主题

```java
themeManager.setTheme("dark"); // 切换到深色主题
```

### 创建自定义主题

```java
Theme customTheme = new Theme(
    "custom_id",
    "自定义主题",
    0xFF000000,  // 文字颜色
    0xEBEBEBEB,  // 背景颜色
    0xFF555555,  // 索引颜色
    0xEBEBEBEB,  // 选中项背景
    0xFF000000,  // 光标颜色
    3,           // 内边距
    5,           // 候选框内边距
    1,           // 边框宽度
    0x80000000   // 边框颜色
);

themeManager.saveCustomTheme(customTheme);
```

## 技术实现

### 架构设计

1. **ThemeManager**：单例模式，管理所有主题的加载、保存和应用
2. **Theme**：数据类，包含所有可配置的UI属性
3. **ThemeEditorGui**：主题编辑器界面
4. **Widget集成**：所有UI组件都继承自Theme-aware的Widget基类

### 文件结构

```
src/main/java/com/dhj/ingameime/theme/
├── Theme.java              # 主题数据类
├── ThemeManager.java       # 主题管理器
└── ThemeEditorGui.java     # 主题编辑器GUI

config/ingameime/themes/
├── default.json           # 默认主题
├── dark.json             # 深色主题
└── light.json            # 浅色主题
```

### 扩展性

主题系统设计为可扩展的，未来可以轻松添加：
- 更多主题属性（字体、阴影、圆角等）
- 主题导入/导出功能
- 主题分享社区
- 动态主题（根据时间/环境自动切换）

## 故障排除

### 常见问题

1. **主题不生效**：检查主题文件格式是否正确，颜色值是否为有效的32位整数
2. **编译错误**：确保已添加Gson依赖（已在dependencies.gradle中添加）
3. **GUI显示异常**：检查颜色值的透明度通道（前两位十六进制）

### 调试

启用调试日志查看主题加载过程：

```properties
# 在config/ingameime.cfg中
debug {
    B:DebugLog=true
}
```

## 贡献指南

欢迎贡献新的主题！请遵循以下步骤：

1. 在 `config/ingameime/themes/` 目录中创建新的JSON主题文件
2. 确保包含所有必需的属性
3. 测试主题在不同场景下的显示效果
4. 提交Pull Request

## 许可证

主题系统遵循与IngameIME相同的LGPL-2.1许可证。