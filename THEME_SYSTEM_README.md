# IngameIME Theme System

## Overview

The IngameIME Theme System is a flexible and extensible UI customization framework that allows users to fully customize the appearance of the input method interface. The system supports real-time preview, dynamic switching, 9-slice texture rendering, decoration widgets, and easily shareable theme configuration files.

## Theme Properties Explained

Each theme contains the following configurable UI properties:

### Basic Properties

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

### Advanced Texture Properties (9-Slice Rendering)

| Property | Description | Default Value | Format Notes |
|----------|-------------|---------------|--------------|
| `textureFile` | Background texture file name | `""` | File name in theme folder (e.g., "bg.png") |
| `u` | Texture U coordinate | `0` | Horizontal offset in texture atlas |
| `v` | Texture V coordinate | `0` | Vertical offset in texture atlas |
| `sliceSize` | 9-slice grid size | `0` | Size of the 9-slice grid (0 = disable 9-slice) |
| `cornerSize` | Corner size | `0` | Size of fixed corners in 9-slice |
| `textureWidth` | Texture width | `256` | Full texture width |
| `textureHeight` | Texture height | `256` | Full texture height |

### Decoration System

The decoration system allows you to overlay external images on UI components using a dual-anchor positioning algorithm. This enables advanced visual effects like glows, badges, indicators, and ornamental elements that move and scale with the parent component.

#### Understanding Decorations

Decorations are separate image files that are rendered on top of (or behind) themed UI components. They use a dual-anchor system:
- **Parent Anchor (`anchorPoint`)**: Where on the parent component to attach
- **Image Anchor (`imageAnchor`)**: Which point of the decoration image aligns with the parent anchor

**Use Cases:**
- Corner ornaments and flourishes
- Glow effects behind text
- Selection indicators
- Decorative badges
- Shadow/highlight overlays

#### Dual-Anchor Positioning Algorithm

The final position is calculated as:
```
Final Position = Parent Anchor Position + Offset - Image Anchor Offset
```

**Step-by-Step Calculation:**
1. Calculate parent anchor position based on `anchorPoint` (0-8 grid)
2. Apply `offsetX` and `offsetY` to get the target point
3. Calculate image anchor offset based on `imageAnchor` and image size
4. Subtract image anchor offset to align the correct part of the image

**Example: Top-Right Badge**
```json
{
  "textureFile": "new_badge.png",
  "anchorPoint": 2,    // Parent's top-right corner
  "imageAnchor": 0,    // Image's top-left corner
  "offsetX": -5,       // 5px left of parent's top-right
  "offsetY": 5,        // 5px below parent's top edge
  "scale": 0.8,
  "alpha": 1.0,
  "target": "candidate"
}
```
Result: Badge appears near the top-right of candidate list, slightly inset.

**Example: Centered Glow Effect**
```json
{
  "textureFile": "glow.png",
  "anchorPoint": 4,    // Parent's center
  "imageAnchor": 4,    // Image's center
  "offsetX": 0,
  "offsetY": 0,
  "scale": 1.5,
  "alpha": 0.5,
  "target": "all"
}
```
Result: Glow centered on component, 1.5x size, 50% transparent.

#### Decoration Properties

| Property | Description | Default | Range/Format |
|----------|-------------|---------|--------------|
| `textureFile` | Decoration texture file | `""` | File name in theme folder |
| `anchorPoint` | Anchor point on parent (0-8) | `0` | 0=top-left, 2=top-right, 4=center, 6=bottom-left, 8=bottom-right |
| `imageAnchor` | Anchor point on image itself (0-8) | `0` | Same as anchorPoint |
| `offsetX` | Horizontal offset | `0` | Pixels |
| `offsetY` | Vertical offset | `0` | Pixels |
| `scale` | Image scale | `1.0` | Float, > 0 |
| `alpha` | Transparency | `1.0` | 0.0-1.0 |
| `target` | Target component | `"all"` | "all", "candidate", "preedit", etc. |
| `width` | Fixed width (-1 = auto) | `-1` | Pixels or -1 |
| `height` | Fixed height (-1 = auto) | `-1` | Pixels or -1 |

**Anchor Point Layout (3x3 Grid):**
```
0 --- 1 --- 2
|     |     |
3 --- 4 --- 5
|     |     |
6 --- 7 --- 8
```

#### Target Components

The `target` property controls which UI components display the decoration:

| Target Value | Description |
|--------------|-------------|
| `"all"` | All components (default) |
| `"candidate"` | Candidate list only |
| `"preedit"` | Pre-edit text only |
| `"inputmode"` | Input mode indicator only |

#### Configuration Examples

**Corner Ornament (Top-Right):**
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

**Selection Glow (Behind selected item):**
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

**Floating Indicator (Above text):**
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

#### Common Decoration Issues

1. **Decoration Not Showing**
   - **Cause**: `alpha` is 0, `scale` is 0, or texture file not found
   - **Solution**: Check alpha > 0, scale > 0, verify texture file exists

2. **Decoration in Wrong Position**
   - **Cause**: Incorrect anchorPoint or imageAnchor values
   - **Solution**: Visualize the 3x3 grid, test different anchor combinations

3. **Decoration Clipped/Cut Off**
   - **Cause**: Decoration extends outside scissor box or screen bounds
   - **Solution**: Reduce scale, adjust offsets, or use smaller texture

4. **Multiple Decorations Overlapping Incorrectly**
   - **Cause**: Render order not controlled
   - **Solution**: Decorations render in array order; arrange JSON array accordingly

5. **Decoration Too Big/Small**
   - **Cause**: Wrong scale or texture resolution
   - **Solution**: Adjust scale, or resize source texture to appropriate dimensions

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
   - **Select Theme**: Click `Select Theme` button to open theme selection GUI
   - **Modify Colors**: Edit hexadecimal color values in text fields
   - **Adjust Values**: Modify padding, border width, etc.
   - **Real-time Preview**: Color preview boxes show instant effects
   - **Apply Theme**: Click `Apply` to save and apply changes
   - **Create New Theme**: Click `Create New` to input name and create custom theme
   - **Delete Theme**: Click `Delete` to remove current custom theme (cannot delete default themes)

### 2. Using Configuration Files

Theme files are saved in the `config/ingameime/themes/<theme_id>/` directory in JSON format:

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

**File Structure:**
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

**Important Notes**:
- Color values support multiple formats: `0xFF000000`, `#FF000000`, `FF000000`
- Theme IDs must be unique and cannot conflict with built-in themes (default, dark, light)
- Texture files must be placed in the same folder as `theme.json`
- After modifying configuration files, reload themes in-game to take effect

### 3. 9-Slice Texture Rendering

The 9-slice (or 9-patch) rendering system allows scalable textured backgrounds that maintain crisp corners while stretching the edges and center. This is essential for UI elements that need to resize dynamically while preserving visual quality.

#### Understanding 9-Slice

A 9-slice texture is divided into 9 regions:

```
+--------+--------+--------+
| Corner |  Edge  | Corner |  ← Row 1: Fixed height (cornerSize)
|   (1)  |   (2)  |   (3)  |
+--------+--------+--------+
|  Edge  | Center |  Edge  |  ← Row 2: Stretched vertically
|   (4)  |   (5)  |   (6)  |
+--------+--------+--------+
| Corner |  Edge  | Corner |  ← Row 3: Fixed height (cornerSize)
|   (7)  |   (8)  |   (9)  |
+--------+--------+--------+
   ↑                    ↑
   └─ Fixed width       └─ Fixed width
      (cornerSize)         (cornerSize)
```

**Stretching Behavior:**
- **Corners (1,3,7,9)**: Never stretch, maintain original size
- **Edges (2,4,6,8)**: Stretch in one direction (horizontal or vertical)
- **Center (5)**: Stretch in both directions to fill remaining space

#### Preparing 9-Slice Textures

**Step 1: Design Your Texture**
- Create a square image (e.g., 64x64, 128x128, 256x256)
- Design your UI frame/background with borders and corners
- Ensure corners are symmetrical for best results

**Step 2: Define Slice Parameters**
- `sliceSize`: The total size of your 9-slice grid (typically the full texture size)
- `cornerSize`: The size of your corner regions (must be less than sliceSize/2)

**Example Texture Layout (64x64 with 16px corners):**
```
+------+------------------+------+
| 16x16|     32x16        | 16x16|  ← Top corners + top edge
|      |   (stretches     |      |
|      |    horizontally) |      |
+------+------------------+------+
| 16x32|     32x32        | 16x32|  ← Middle row
|      |   (stretches     |      |
|(fixed|   both ways)     |(fixed|
| horiz|                  | horiz|
+------+------------------+------+
| 16x16|     32x16        | 16x16|  ← Bottom corners + bottom edge
|      |   (stretches     |      |
|      |    horizontally) |      |
+------+------------------+------+
```

**Step 3: Configure in theme.json**
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

#### Configuration Examples

**Simple Panel (64x64 texture, 8px corners):**
```json
{
  "textureFile": "simple_panel.png",
  "sliceSize": 64,
  "cornerSize": 8,
  "textureWidth": 64,
  "textureHeight": 64
}
```

**Fancy Border (128x128 texture, 24px corners with glow):**
```json
{
  "textureFile": "fancy_border.png",
  "sliceSize": 128,
  "cornerSize": 24,
  "textureWidth": 128,
  "textureHeight": 128
}
```

**Texture Atlas (256x256 atlas with multiple UI elements):**
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

#### Common 9-Slice Issues

1. **Visible Seams at Edges**
   - **Cause**: CornerSize too large or texture bleeding
   - **Solution**: Reduce cornerSize, add 1px transparent border to texture

2. **Corners Stretching**
   - **Cause**: cornerSize >= sliceSize/2
   - **Solution**: Ensure cornerSize < sliceSize/2 (e.g., for 64px slice, max corner is 31px)

3. **Texture Looks Blurry When Scaled**
   - **Cause**: Texture filtering or wrong dimensions
   - **Solution**: Use power-of-2 textures (64, 128, 256), enable nearest-neighbor filtering

4. **Wrong Region Being Used**
   - **Cause**: Incorrect u/v coordinates in atlas
   - **Solution**: Verify u,v point to correct region in texture atlas

5. **9-Slice Not Rendering**
   - **Cause**: sliceSize is 0 or textureFile not found
   - **Solution**: Set sliceSize > 0, verify texture file exists in theme folder

### 4. Theme Management Files

The system automatically maintains the following files:
- `config/ingameime/themes/last_theme.txt`: Records the last used theme ID
- `config/ingameime/themes/<theme_id>/theme.json`: Configuration files for each theme
- `config/ingameime/themes/<theme_id>/*.png`: Texture files for each theme

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

// Switch theme and notify all listeners (takes effect immediately)
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
customTheme.setTextureFile("bg.png");  // Set texture
themeManager.saveCustomTheme(customTheme);

// Delete custom theme
themeManager.deleteCustomTheme("my_theme");

// Reload all themes (e.g., after modifying config files)
themeManager.reloadThemes();

// Scan for new themes without full reload
themeManager.scanForNewThemes();
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
}
```

### Using ThemeRenderer for Custom Components

```java
public class MyCustomWidget {
    
    public void draw(int x, int y, int width, int height) {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        
        // Render themed background with decorations
        ThemeRenderer.render(theme, x, y, width, height, "my_component");
        
        // Draw text with theme colors
        drawString(text, x + theme.getPadding(), y + theme.getPadding(), 
                   theme.getTextColor());
    }
}
```

### Creating Decorations Programmatically

```java
ThemeDecoration decoration = new ThemeDecoration();
decoration.textureFile = "glow.png";
decoration.anchorPoint = 4;  // Center of parent
decoration.imageAnchor = 4;  // Center of image
decoration.offsetX = 0;
decoration.offsetY = -20;  // 20 pixels above center
decoration.scale = 1.5f;
decoration.alpha = 0.6f;
decoration.target = "candidate";  // Only show on candidate list

theme.getDecorations().add(decoration);
themeManager.saveCustomTheme(theme);
```

### File Structure

```
src/main/java/com/dhj/ingameime/theme/
├── api/
│   ├── Theme.java              # Theme data class
│   ├── ThemeManager.java       # Theme manager (singleton)
│   ├── ThemeRenderer.java      # Theme rendering (9-slice + decorations)
│   └── ThemeDecoration.java    # Decoration widget data
├── ColorTypeAdapter.java       # Gson color type adapter
├── ThemeEditorGui.java         # Theme editor GUI
├── ThemeNameInputGui.java      # Theme name input GUI
├── ThemeSelectionGui.java      # Theme selection GUI
└── ThemeType.java              # Theme type enumeration

src/main/java/com/dhj/ingameime/gui/
├── Widget.java                 # UI component base class (theme-aware)
├── WidgetCandidateList.java    # Candidate list component
├── WidgetInputMode.java        # Input mode indicator
└── WidgetPreEdit.java          # Pre-edit text component

config/ingameime/themes/
├── default/theme.json          # Default theme (auto-generated)
├── dark/theme.json             # Dark theme (auto-generated)
├── light/theme.json            # Light theme (auto-generated)
├── last_theme.txt              # Last used theme ID
└── */                          # User custom themes with assets
```

## Extensibility Design

### Adding New Theme Properties

1. Add new field and getter/setter in `Theme.java`
2. Add corresponding editing controls in `ThemeEditorGui.java`
3. Update `ThemeRenderer.java` if the property affects rendering
4. Update documentation

### Custom Theme Serialization

Modify `ColorTypeAdapter.java` to support more color formats, or create custom adapters for other properties.

### Theme Import/Export

Extend `ThemeManager` to add theme import/export functionality for theme sharing:

```java
// Export theme to zip
public void exportTheme(String themeId, File destination);

// Import theme from zip
public Theme importTheme(File source);
```

## Troubleshooting

### Common Issues

1. **Theme Not Applying**
   - Check if theme file format is correct (valid JSON)
   - Confirm color value format is correct (supports `0x...`, `#...`, plain hexadecimal)
   - Check game logs for loading errors
   - Verify theme folder structure (`themes/<theme_id>/theme.json`)

2. **Texture Not Displaying**
   - Verify texture file exists in theme folder
   - Check `textureFile` name matches actual file name (case-sensitive)
   - Ensure `sliceSize` > 0 to enable 9-slice rendering
   - Verify `textureWidth` and `textureHeight` match actual image dimensions

3. **Decorations Not Showing**
   - Check decoration `textureFile` exists
   - Verify `alpha` > 0
   - Check `target` matches component ID or use `"all"`
   - Review anchor point calculations

4. **Color Display Abnormalities**
   - Check alpha channel (first 2 hexadecimal digits)
   - Ensure color values are within valid range (0x00000000 - 0xFFFFFFFF)
   - Verify color values are 32-bit integers

### Debug Information

Enable debug logging to view theme system operation status:

```java
// IngameIME logs theme operations automatically when DebugLog is enabled
// Set DebugLog = true in config/ingameime.cfg
```

**Log Output Examples:**
```
[ThemeManager] Reloading themes...
[ThemeManager] Loaded theme: default (Default Theme)
[ThemeManager] Loaded theme: dark (Dark Theme)
[ThemeManager] Reloaded 3 themes, current: default
[ThemeManager] Theme switched to: dark (Dark Theme)
[ThemeRenderer] Rendering ID: dark | Texture: false | Slice: 0
[ThemeEditor] Theme saved and applied: my_theme (My Custom Theme)
```

## Contribution Guidelines

Welcome contributions to the theme system code or theme designs!

### Submitting New Themes

1. Create theme folder in `config/ingameime/themes/`
2. Create `theme.json` with all required properties
3. Add texture files if using custom graphics
4. Test display effects in different game scenarios
5. Submit Pull Request or share theme file

### Code Contributions

1. Follow existing code style and architecture
2. Add appropriate comments and documentation
3. Include unit tests (if applicable)
4. Ensure backward compatibility
5. Update both English and Chinese README files

### Theme Sharing

Custom themes can be shared through:
- Direct folder sharing (zip the theme folder)
- Publishing to Mod communities
- Integration into theme packs

## License

The theme system follows the same LGPL-2.1 license as IngameIME. Custom theme files are not restricted by the license and can be freely shared and used.

---

*Last Updated: February 22, 2026*
*IngameIME-1.12.2 Theme System v2.0*
