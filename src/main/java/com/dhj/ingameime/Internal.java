package com.dhj.ingameime;

import com.dhj.ingameime.config.Config;
import com.dhj.ingameime.rust.RustImeLibrary;
import net.minecraft.client.Minecraft;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public class Internal {
    public static boolean LIBRARY_LOADED = false;
    public static long InputCtx = 0;  // Rust context pointer
    static RustImeLibrary.PreEditCallback preEditCallback = null;
    static RustImeLibrary.CommitCallback commitCallback = null;
    static RustImeLibrary.CandidateListCallback candidateListCallback = null;
    static RustImeLibrary.InputModeCallback inputModeCallback = null;

    private static void tryLoadLibrary(String libName) {
        if (!LIBRARY_LOADED) try {
            // Load DLL from resources using System.load() instead of System.loadLibrary()
            // because loadLibrary() adds platform prefixes/suffixes automatically
            InputStream lib = Internal.class.getClassLoader().getResourceAsStream(libName);
            if (lib == null) throw new RuntimeException("Required library resource not exist: " + libName);
            Path path = Files.createTempFile("ingameime-core", ".dll");
            Files.copy(lib, path, StandardCopyOption.REPLACE_EXISTING);
            System.load(path.toString());
            LIBRARY_LOADED = true;
            LOG.info("Library [{}] has loaded!", libName);
        } catch (Throwable e) {
            LOG.warn("Try to load library [{}] but failed: {}", libName, e.getClass().getSimpleName());
        }
        else LOG.info("Library has loaded, skip loading of [{}]", libName);
    }

    private static long callGlfwGetWin32Window(long glfwWindow) {
        // Cleanroom uses org.lwjgl3 package for their LWJGL3 fork
        // Standard LWJGL3 uses org.lwjgl package
        try {
            String[] possibleClasses = {
                    // Cleanroom's fork (org.lwjgl3.*)
                    "org.lwjgl3.glfw.GLFWNativeWin32",
                    "org.lwjgl3.system.windows.GLFWNativeWin32",
                    // Standard LWJGL3 (org.lwjgl.*)
                    "org.lwjgl.glfw.GLFWNativeWin32",
                    "org.lwjgl.system.windows.GLFWNativeWin32"
            };
            for (String className : possibleClasses) {
                try {
                    Class<?> nativeClass = Class.forName(className);
                    LOG.info("Found GLFWNativeWin32 class: {}", className);
                    try {
                        Method getWin32Window = nativeClass.getMethod("glfwGetWin32Window", long.class);
                        long hwnd = (long) getWin32Window.invoke(null, glfwWindow);

                        if (hwnd != 0) {
                            LOG.info("Successfully got Win32 HWND 0x{} via {}",
                                    Long.toHexString(hwnd), className);
                            return hwnd;
                        } else {
                            LOG.warn("{}.glfwGetWin32Window returned 0", className);
                        }
                    } catch (NoSuchMethodException e) {
                        LOG.debug("Method glfwGetWin32Window not found in {}", className);
                    }
                } catch (ClassNotFoundException e) {
                    LOG.debug("Class not found: {}", className);
                }
            }
            LOG.warn("Could not find any GLFWNativeWin32 class to convert GLFW window to HWND");
            return 0;
        } catch (Throwable e) {
            LOG.error("Exception while calling glfwGetWin32Window: {} - {}",
                    e.getClass().getSimpleName(), e.getMessage());
            e.getStackTrace();
            return 0;
        }
    }

    private static long getWindowHandle_LWJGL3() {
        try {
            // Check if we have Display.getWindow() method (LWJGL3 indicator)
            Method getWindow = Display.class.getMethod("getWindow");
            long glfwWindow = (long) getWindow.invoke(null);

            if (glfwWindow == 0) {
                LOG.debug("GLFW window pointer is 0");
                return 0;
            }

            LOG.info("Got GLFW window pointer: 0x{}", Long.toHexString(glfwWindow));

            // Try to convert GLFW window to Win32 HWND
            long hwnd = callGlfwGetWin32Window(glfwWindow);
            if (hwnd != 0) {
                return hwnd;
            }

            LOG.warn("Could not convert GLFW window to Win32 HWND via native methods");
            return 0;
        } catch (NoSuchMethodException e) {
            // Display.getWindow() doesn't exist, not LWJGL3
            LOG.debug("Display.getWindow() method not found");
            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get window handle via LWJGL3: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

    private static long getWindowHandle_LWJGL2() {
        try {
            Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
            getImplementation.setAccessible(true);
            Object impl = getImplementation.invoke(null);

            if (impl == null) {
                LOG.debug("Display.getImplementation() returned null");
                return 0;
            }

            LOG.info("Display implementation class: {}", impl.getClass().getName());

            String[] possibleClasses = {
                    "org.lwjgl.opengl.WindowsDisplay",
                    "org.lwjgl.opengl.Win32Display",
                    "org.lwjgl.opengl.Display$WindowsDisplay"
            };

            for (String className : possibleClasses) {
                try {
                    Class<?> clsWindowsDisplay = Class.forName(className);
                    if (clsWindowsDisplay.isInstance(impl)) {
                        LOG.info("Implementation is instance of {}", className);
                        Method getHwnd = clsWindowsDisplay.getDeclaredMethod("getHwnd");
                        getHwnd.setAccessible(true);
                        long hwnd = (Long) getHwnd.invoke(impl);
                        if (hwnd != 0) {
                            LOG.info("Successfully obtained hwnd 0x{} using class: {}", Long.toHexString(hwnd), className);
                            return hwnd;
                        } else {
                            LOG.warn("getHwnd() returned 0 for class {}", className);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    LOG.debug("Class not found: {}", className);
                }
            }

            // Try to find getHwnd method directly on the implementation
            try {
                Method getHwnd = impl.getClass().getDeclaredMethod("getHwnd");
                getHwnd.setAccessible(true);
                long hwnd = (Long) getHwnd.invoke(impl);
                if (hwnd != 0) {
                    LOG.info("Successfully obtained hwnd 0x{} directly from implementation", Long.toHexString(hwnd));
                    return hwnd;
                }
            } catch (NoSuchMethodException e) {
                LOG.debug("getHwnd() method not found in implementation class");
            }

            // Try to get hwnd directly from fields
            try {
                java.lang.reflect.Field hwndField = impl.getClass().getDeclaredField("hwnd");
                hwndField.setAccessible(true);
                Object hwnd = hwndField.get(impl);
                if (hwnd instanceof Long && (Long)hwnd != 0) {
                    LOG.info("Successfully obtained hwnd 0x{} from field", Long.toHexString((Long)hwnd));
                    return (Long) hwnd;
                }
            } catch (NoSuchFieldException e) {
                LOG.debug("hwnd field not found in implementation class");
            }

            return 0;
        } catch (NoSuchMethodException e) {
            // getImplementation doesn't exist, not LWJGL2
            LOG.debug("Display.getImplementation() method not found");
            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get window handle via LWJGL2: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

    private static long getWindowHandle() {
        long hWnd = 0;
        boolean hasGetWindow = false;
        boolean hasGetImplementation = false;

        try {
            Display.class.getMethod("getWindow");
            hasGetWindow = true;
        } catch (NoSuchMethodException ignored) {}

        try {
            Display.class.getDeclaredMethod("getImplementation");
            hasGetImplementation = true;
        } catch (NoSuchMethodException ignored) {}
        LOG.info("LWJGL method detection - getWindow: {}, getImplementation: {}", hasGetWindow, hasGetImplementation);

        // Try LWJGL3 first if available
        if (hasGetWindow) {
            LOG.info("Attempting LWJGL3 method...");
            hWnd = getWindowHandle_LWJGL3();
        }

        // If LWJGL3 failed and LWJGL2 is available, try LWJGL2
        if (hWnd == 0 && hasGetImplementation) {
            LOG.info("LWJGL3 failed or not available, attempting LWJGL2 method...");
            hWnd = getWindowHandle_LWJGL2();
        }

        if (hWnd == 0) {
            if (!hasGetWindow && !hasGetImplementation) {
                LOG.error("Cannot detect LWJGL version - neither getWindow() nor getImplementation() found");
            } else {
                LOG.error("Failed to obtain window handle from all available LWJGL methods");
            }
        } else {
            LOG.info("Successfully obtained window handle: 0x{}", Long.toHexString(hWnd));
        }

        return hWnd;
    }

    public static void destroyInputCtx() {
        if (InputCtx == 0) return;
        try {
            RustImeLibrary.destroyInputContext(InputCtx);
            LOG.info("InputContext has destroyed!");
        } catch (Throwable e) {
            LOG.error("Failed to destroy InputContext", e);
        }
        InputCtx = 0;
    }

    public static void createInputCtx() {
        if (!LIBRARY_LOADED) return;

        LOG.info("Using IngameIME Rust backend");
        // LOG.info("Using IngameIME Rust: {}", RustImeLibrary.rust_ime_library_get_version());

        if (!Display.isCreated()) {
            LOG.warn("Display is not created yet, deferring InputContext creation");
            return;
        }
        long hWnd = getWindowHandle();
        if (hWnd != 0) {
            if (Minecraft.getMinecraft().isFullScreen()) {
                Config.UiLess_Windows = true;
                Config.sync();
            }
            // API parameter is ignored in Rust version (only IMM32 supported)
            int api = Config.API_Windows.equals("TextServiceFramework") ? 0 : 1;
            LOG.info("Using API: {}, UiLess: {}", api, Config.UiLess_Windows);
            InputCtx = RustImeLibrary.createInputContextWin32(hWnd, api, Config.UiLess_Windows);
            if (InputCtx == 0) {
                LOG.error("Failed to create InputContext!");
                return;
            }
            LOG.info("InputContext has created!");
            LOG.info("Rust IME library version: {}", RustImeLibrary.getVersion());
            
            // Initialize Rust logger with Java's Log4j
            RustImeLibrary.initLogger(LOG);
            LOG.info("Rust logger initialized, forwarding to Log4j");
            
            // Set max candidates from config
            RustImeLibrary.setMaxCandidates(InputCtx, Config.MaxCandidates);
            LOG.info("Max candidates set to: {}", Config.MaxCandidates);
            // Enable debug logging if configured
            if (Config.DebugLog) {
                RustImeLibrary.setDebugLogging(true);
                LOG.info("Rust debug logging enabled");
            }
        } else {
            LOG.error("InputContext could not init as the hWnd is NULL!");
            return;
        }

        // Setup callbacks
        preEditCallback = (state, content, cursor) -> {
            try {
                if (state == 0) { // Begin
                    ClientProxy.Screen.WInputMode.setActive(false);
                }
                if (content != null) {
                    ClientProxy.Screen.PreEdit.setContent(content, cursor);
                } else {
                    ClientProxy.Screen.PreEdit.setContent(null, -1);
                }
            } catch (Throwable e) {
                LOG.error("Exception in PreEdit callback", e);
            }
        };

        commitCallback = text -> {
            try {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        IMStates.getActiveControl().writeText(text);
                    } catch (Throwable e) {
                        LOG.error("Exception in Commit callback", e);
                    }
                });
            } catch (Throwable e) {
                LOG.error("Exception scheduling Commit task", e);
            }
        };

        candidateListCallback = (state, candidates, selected) -> {
            try {
                if (candidates != null) {
                    LOG.debug("CandidateList callback: received {} candidates, selected={}", candidates.length, selected);
                    // Split candidates by whitespace and flatten the list
                    List<String> flattened = getStrings(candidates);
                    LOG.debug("Flattened to {} candidates", flattened.size());
                    for (int i = 0; i < flattened.size(); i++) {
                        LOG.debug("  [{}] {}", i, flattened.get(i));
                    }
                    // Apply max candidates limit
                    if (flattened.size() > Config.MaxCandidates) {
                        flattened = new ArrayList<>(flattened.subList(0, Config.MaxCandidates));
                        LOG.debug("Truncated to {} candidates", flattened.size());
                    }
                    ClientProxy.Screen.CandidateList.setContent(flattened, selected);
                } else {
                    ClientProxy.Screen.CandidateList.setContent(null, -1);
                }
            } catch (Throwable e) {
                LOG.error("Exception in CandidateList callback", e);
            }
        };

        inputModeCallback = mode -> {
            try {
                // mode: 0=Alpha, 1=Native, 2=Unsupported
                ClientProxy.Screen.WInputMode.setMode(mode == 1);
            } catch (Throwable e) {
                LOG.error("Exception in InputMode callback", e);
            }
        };

        // Register callbacks
        RustImeLibrary.setPreEditCallback(InputCtx, preEditCallback);
        RustImeLibrary.setCommitCallback(InputCtx, commitCallback);
        RustImeLibrary.setCandidateListCallback(InputCtx, candidateListCallback);
        RustImeLibrary.setInputModeCallback(InputCtx, inputModeCallback);

        System.gc();
    }

    @Nonnull
    private static List<String> getStrings(String[] candidates) {
        List<String> flattened = new ArrayList<>();
        for (String candidate : candidates) {
            // Extract individual candidates by splitting on any non-visible character
            // This handles special Unicode spaces, zero-width chars, etc.
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < candidate.length(); i++) {
                char c = candidate.charAt(i);
                // Check if character is visible (not whitespace, control, or format char)
                int type = Character.getType(c);
                boolean isVisible = type != Character.SPACE_SEPARATOR
                                 && type != Character.LINE_SEPARATOR
                                 && type != Character.PARAGRAPH_SEPARATOR
                                 && type != Character.CONTROL
                                 && type != Character.FORMAT
                                 && type != Character.PRIVATE_USE
                                 && type != Character.SURROGATE
                                 && type != Character.UNASSIGNED;
                
                if (isVisible) {
                    current.append(c);
                } else if (current.length() > 0) {
                    flattened.add(current.toString());
                    current.setLength(0);
                }
            }
            // Add remaining candidate
            if (current.length() > 0) {
                flattened.add(current.toString());
            }
        }
        return flattened;
    }

    static void loadLibrary() {
        boolean isWindows = LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS;

        if (!isWindows) {
            LOG.error("Unsupported platform: {}", LWJGLUtil.getPlatformName());
            return;
        }

        // Load Rust-compiled native library (only x64 supported for now)
        tryLoadLibrary("ingameime_core.dll");

        if (!LIBRARY_LOADED) {
            LOG.error("Unsupported arch: {}", System.getProperty("os.arch"));
        }
    }

    public static boolean getActivated() {
        if (InputCtx != 0) return RustImeLibrary.isInputContextActivated(InputCtx);
        else return false;
    }

    public static void setActivated(boolean activated) {
        if (InputCtx == 0) {
            if (activated) {
                LOG.warn("InputContext is 0. Attempting to recreate it...");
                createInputCtx();
                if (InputCtx == 0) {
                    LOG.error("Failed to recreate InputContext. IME will be unavailable.");
                    return;
                }
                LOG.info("InputContext recreated successfully.");
            } else {
                return;
            }
        }

        if (getActivated() == activated) {
            return;
        }

        try {
            RustImeLibrary.setInputContextActivated(InputCtx, activated);
            IngameIME_Forge.logDebugInfo("IM active state: {}", activated);
        } catch (Throwable t) {
            LOG.error("Failed to set IME active state. This indicates the InputContext may be stale. Attempting to recover.", t);

            try {
                LOG.debug("Destroying stale InputContext...");
                destroyInputCtx();

                LOG.debug("Recreating new InputContext...");
                createInputCtx();

                if (InputCtx != 0) {
                    LOG.info("Recovery successful. Retrying setActivated...");
                    try {
                        RustImeLibrary.setInputContextActivated(InputCtx, activated);
                        LOG.debug("IM active state after recovery: {}", activated);
                    } catch (Throwable retryError) {
                        LOG.error("Failed to set active state even after recovery.", retryError);
                    }
                }
                else {
                LOG.debug("Recovery failed. Could not recreate InputContext.");
                }
            } catch (Throwable recoveryError) {
                LOG.debug("A critical error occurred during the recovery process itself.", recoveryError);
            }
        }
    }
}