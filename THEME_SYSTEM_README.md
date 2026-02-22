# IngameIME Theme System

## Overview

The IngameIME Theme System is a flexible and extensible UI customization framework that allows users to fully customize the appearance of the input method interface. The system supports real-time preview, dynamic switching, and easily shareable theme configuration files.

## Theme Properties Explained

Each theme contains the following configurable UI properties:

| Property | Description | Default Value | Format Notes |
|----------|-------------|---------------|--------------|
| `id` | Unique theme identifier | `"default"` | Lowercase letters, numbers, underscores |
| `name` | Theme display name | `"Default Theme"` | Any string |
| `textColor` | Text color | `0xFF000000` | 32-bit hexadecimal ARGB format |
| `backgroundColor` | Background color | `0xEBEBEBEB` | 32-bit hexadecimal ARGB format |
| `indexColor` | Candidate index color | `0xFF555555` | 32-bit hexadecimal ARGB format |
| `selectedBackgroundColor` | Selected item background color | `0xEBEBEBEB` | 32-bit hexadecimal ARGB format |
| `cursorColor` | Cursor color | `0xFF000000` | 32-bit hexadecimal ARGB format |
| `borderColor` | Border color | `0x80000000` | 32-bit hexadecimal ARGB format |
| `padding` | Padding | `3` | Pixel value, minimum 0 |
| `candidatePadding` | Candidate box padding | `3` | Pixel value, minimum 0 |
| `borderWidth` | Border width | `1` | Pixel value, minimum 0 |

### Color Format Explanation

Color values use 32-bit ARGB format:
- **A (Alpha)**: First 2 hexadecimal digits, representing transparency (00=fully transparent, FF=fully opaque)
- **R (Red)**: 3rd-4th hexadecimal digits, red component
- **G (Green)**: 5th-6th hexadecimal digits, green component
- **B (Blue)**: 7th-8th hexadecimal digits, blue component

Examples:
- `0xFF000000`: Fully opaque black
- `0x80000000`: 50% transparent black
- `0xEBEBEBEB`: Approximately 92% transparent light gray
- `0xFFFFFFFF`: Fully opaque white

## Usage Guide

### 1. Using the In-game Configuration Interface

1. Press `ESC` in-game to open the menu
2. Select `Mod Options` → `IngameIME` → `Config`
3. Click the `Theme Editor` button in the configuration interface
4. In the theme editor:
   - **Select Theme**: Choose existing theme from dropdown list
   - **Modify Colors**: Click color buttons to select new colors
   - **Adjust Values**: Modify padding, border width, etc.
   - **Real-time Preview**: Right-side preview area shows instant effects
   - **Apply Theme**: Click `Apply Theme` to save and apply changes
   - **Create New Theme**: Click `Create New Theme` to input name and create custom theme
   - **Delete Theme**: Click `Delete Theme` to remove current custom theme (cannot delete default themes)

### 2. Using Configuration Files

Theme files are saved in the `config/ingameime/themes/` directory in JSON format:

```json
{
  "id": "my_custom_theme",
  "name": "My Custom Theme",
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

**Important Notes**:
- Color values support multiple formats: `0xFF000000`, `#FF000000`, `FF000000`
- Theme IDs must be unique and cannot conflict with built-in themes (default, dark, light)
- After modifying configuration files, reload themes in-game to take effect

### 3. Theme Management Files

The system automatically maintains the following files:
- `config/ingameime/themes/last_theme.txt`: Records the last used theme ID
- `config/ingameime/themes/*.json`: Configuration files for each theme

## Predefined Themes

### 1. Default Theme (default)
- **Text Color**: Black (`0xFF000000`)
- **Background Color**: Light gray semi-transparent (`0xEBEBEBEB`)
- **Use Case**: Universal design, suitable for most game interfaces
- **Characteristics**: Good readability with moderate transparency

### 2. Dark Theme (dark)
- **Text Color**: White (`0xFFFFFFFF`)
- **Background Color**: Dark gray semi-transparent (`0x80333333`)
- **Use Case**: Dark game interfaces, nighttime use
- **Characteristics**: Low brightness, reduces visual distraction

### 3. Light Theme (light)
- **Text Color**: Black (`0xFF000000`)
- **Background Color**: White semi-transparent (`0xF0FFFFFF`)
- **Use Case**: Light game interfaces, daytime use
- **Characteristics**: High contrast, clear and readable

## Developer API

### Get Theme Manager Instance

```java
ThemeManager themeManager = ThemeManager.getInstance();
```

### Get and Switch Themes

```java
// Get current theme
Theme currentTheme = themeManager.getCurrentTheme();

// Get specific theme by ID
Theme darkTheme = themeManager.getTheme("dark");

// Switch theme (takes effect immediately)
themeManager.setTheme("light");

// Switch theme and notify all listeners
themeManager.setThemeAndNotify("custom_theme");
```

### Theme Management Operations

```java
// Get all available themes
Map<String, Theme> allThemes = themeManager.getAvailableThemes();

// Create and save custom theme
Theme customTheme = Theme.createCustomTheme("my_theme", "My Theme");
customTheme.setTextColor(0xFF3366CC);  // Modify color
customTheme.setPadding(5);             // Modify padding
themeManager.saveCustomTheme(customTheme);

// Delete custom theme
themeManager.deleteCustomTheme("my_theme");

// Reload all themes (e.g., after modifying config files)
themeManager.reloadThemes();
```

### Theme Change Listener

```java
public class MyUIComponent implements ThemeManager.ThemeChangeListener {
    
    public MyUIComponent() {
        // Register theme change listener
        ThemeManager.getInstance().addThemeChangeListener(this);
    }
    
    @Override
    public void onThemeChanged(Theme newTheme) {
        // Automatically called when theme changes
        updateColors(newTheme);
        refreshDisplay();
    }
    
    public void cleanup() {
        // Remove listener
        ThemeManager.getInstance().removeThemeChangeListener(this);
    }
}
```

### Using Themes in UI Components

```java
public class MyWidget extends Widget {
    
    public MyWidget() {
        // Widget base class automatically registers theme listener
        // Get theme colors using the following methods
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int textColor = theme.getTextColor();
        int bgColor = theme.getBackgroundColor();
        int padding = theme.getPadding();
    }
    
    @Override
    public void draw() {
        // Draw using theme colors
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        drawRect(x, y, x + width, y + height, theme.getBackgroundColor());
        drawString(text, x + theme.getPadding(), y + theme.getPadding(), theme.getTextColor());
    }
}
```

### File Structure

```
src/main/java/com/dhj/ingameime/theme/
├── Theme.java              # Theme data class
├── ThemeManager.java       # Theme manager (singleton)
├── ColorTypeAdapter.java   # Gson color type adapter
├── ThemeEditorGui.java     # Theme editor GUI
└── ThemeNameInputGui.java  # Theme name input GUI

src/main/java/com/dhj/ingameime/gui/
├── Widget.java             # UI component base class (theme-aware)
├── WidgetCandidateList.java # Candidate list component
└── WidgetPreEdit.java      # Pre-edit text component

src/main/java/com/dhj/ingameime/config/
├── ThemeEditorConfigElement.java # Theme editor entry in config interface
└── ThemeEditorEntry.java         # Theme editor configuration item

config/ingameime/themes/
├── default.json           # Default theme (auto-generated)
├── dark.json             # Dark theme (auto-generated)
├── light.json            # Light theme (auto-generated)
├── last_theme.txt        # Last used theme ID
└── *.json               # User custom themes
```

## Extensibility Design

### Adding New Theme Properties

1. Add new field and getter/setter in `Theme.java`
2. Add corresponding editing controls in `ThemeEditorGui.java`
3. Use new property in UI components

### Custom Theme Serialization

Modify `ColorTypeAdapter.java` to support more color formats, or create custom adapters for other properties.

### Theme Import/Export

Extend `ThemeManager` to add theme import/export functionality for theme sharing.

## Troubleshooting

### Common Issues

1. **Theme Not Applying**
   - Check if theme file format is correct (valid JSON)
   - Confirm color value format is correct (supports `0x...`, `#...`, plain hexadecimal)
   - Check game logs for loading errors

2. **Color Display Abnormalities**
   - Check alpha channel (first 2 hexadecimal digits)
   - Ensure color values are within valid range (0x00000000 - 0xFFFFFFFF)
   - Verify color values are 32-bit integers


### Debug Information

Enable debug logging to view theme system operation status:

```java
// Enable debugging in code
IngameIME_Forge.logDebugInfo("Theme loading: " + themeId);

// Or enable via configuration file
// Set DebugLog = true in config/ingameime.cfg
```

## Contribution Guidelines

Welcome contributions to the theme system code or theme designs!

### Submitting New Themes

1. Create JSON file in `config/ingameime/themes/` directory
2. Ensure all required properties are included
3. Provide meaningful theme name and ID
4. Test display effects in different game scenarios
5. Submit Pull Request or share theme file

### Code Contributions

1. Follow existing code style and architecture
2. Add appropriate comments and documentation
3. Include unit tests (if applicable)
4. Ensure backward compatibility

### Theme Sharing

Custom themes can be shared through:
- Direct JSON file sharing
- Publishing to Mod communities
- Integration into theme packs

## License

The theme system follows the same LGPL-2.1 license as IngameIME. Custom theme files are not restricted by the license and can be freely shared and used.

---

*Last Updated: February 22, 2026*
*IngameIME-1.12.2 Theme System v1.0*