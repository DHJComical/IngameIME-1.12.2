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
import java.util.concurrent.atomic.AtomicInteger;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public class Internal {
    public static boolean LIBRARY_LOADED = false;
    public static long InputCtx = 0;  // Rust context pointer
    static RustImeLibrary.PreEditCallback preEditCallback = null;
    static RustImeLibrary.CommitCallback commitCallback = null;
    static RustImeLibrary.CandidateListCallback candidateListCallback = null;
    static RustImeLibrary.InputModeCallback inputModeCallback = null;
    private static final AtomicInteger COMMIT_DEBUG_SEQUENCE = new AtomicInteger();
    private static final DeferredCallbackQueue LINUX_CALLBACK_QUEUE = new DeferredCallbackQueue();
    private static boolean forceAlphaApiUnavailable = false;
    private static boolean forceNativeApiUnavailable = false;
    private static boolean linuxKeyBridgeUnavailableLogged = false;

    private static void prepareRustJniBinding() {
        String prop = RustImeLibrary.JNI_BIND_CLASS_PROPERTY;
        String configured = System.getProperty(prop);
        if (configured != null && !configured.trim().isEmpty()) {
            LOG.info("Using configured Rust JNI bind class: {}", configured);
            return;
        }

        String bindClass = RustImeLibrary.getJniBindClassName();
        System.setProperty(prop, bindClass);
        LOG.info("Configured Rust JNI bind class: {}", bindClass);
    }

    private static void tryLoadLibrary(String libName) {
        if (LIBRARY_LOADED) {
            LOG.info("Library has loaded, skip loading of [{}]", libName);
            return;
        }

        try {
            prepareRustJniBinding();
            InputStream lib = Internal.class.getClassLoader().getResourceAsStream(libName);
            if (lib == null) {
                throw new RuntimeException("Required library resource not exist: " + libName);
            }

            String suffix = ".bin";
            int dot = libName.lastIndexOf('.');
            if (dot >= 0 && dot < libName.length() - 1) {
                suffix = libName.substring(dot);
            }

            Path path = Files.createTempFile("ingameime-core-", suffix);
            try (InputStream in = lib) {
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            }
            path.toFile().deleteOnExit();

            System.load(path.toAbsolutePath().toString());
            LIBRARY_LOADED = true;
            LOG.info("Library [{}] has loaded!", libName);
        } catch (Throwable e) {
            LOG.warn("Try to load library [{}] but failed: {}", libName, e.getClass().getSimpleName());
        }
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

    private static long callGlfwGetX11Window(long glfwWindow) {
        try {
            String[] possibleClasses = {
                    "org.lwjgl3.glfw.GLFWNativeX11",
                    "org.lwjgl3.system.linux.GLFWNativeX11",
                    "org.lwjgl.glfw.GLFWNativeX11",
                    "org.lwjgl.system.linux.GLFWNativeX11"
            };
            for (String className : possibleClasses) {
                try {
                    Class<?> nativeClass = Class.forName(className);
                    LOG.info("Found GLFWNativeX11 class: {}", className);
                    try {
                        Method getX11Window = nativeClass.getMethod("glfwGetX11Window", long.class);
                        long window = ((Number) getX11Window.invoke(null, glfwWindow)).longValue();
                        if (window != 0) {
                            LOG.info("Successfully got X11 window 0x{} via {}", Long.toHexString(window), className);
                            return window;
                        } else {
                            LOG.warn("{}.glfwGetX11Window returned 0", className);
                        }
                    } catch (NoSuchMethodException e) {
                        LOG.debug("Method glfwGetX11Window not found in {}", className);
                    }
                } catch (ClassNotFoundException e) {
                    LOG.debug("Class not found: {}", className);
                }
            }
            LOG.warn("Could not find any GLFWNativeX11 class to convert GLFW window to X11 window");
            return 0;
        } catch (Throwable e) {
            LOG.warn("Exception while calling glfwGetX11Window: {} - {}", e.getClass().getSimpleName(), e.getMessage());
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

    private static long getWindowHandleLinux_LWJGL3() {
        try {
            Method getWindow = Display.class.getMethod("getWindow");
            long glfwWindow = ((Number) getWindow.invoke(null)).longValue();

            if (glfwWindow == 0) {
                LOG.debug("GLFW window pointer is 0");
                return 0;
            }

            LOG.info("Got GLFW window pointer: 0x{}", Long.toHexString(glfwWindow));
            return callGlfwGetX11Window(glfwWindow);
        } catch (NoSuchMethodException e) {
            LOG.debug("Display.getWindow() method not found");
            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get Linux window handle via LWJGL3: {} - {}", e.getClass().getSimpleName(), e.getMessage());
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

    private static long getWindowHandleLinux_LWJGL2() {
        try {
            Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
            getImplementation.setAccessible(true);
            Object impl = getImplementation.invoke(null);
            if (impl == null) {
                return 0;
            }

            String[] methodNames = {"getWindow", "getCurrentWindow"};
            for (String methodName : methodNames) {
                try {
                    Method method = impl.getClass().getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    Object value = method.invoke(impl);
                    if (value instanceof Number) {
                        long window = ((Number) value).longValue();
                        if (window != 0) {
                            LOG.info("Got Linux window 0x{} via {}.{}", Long.toHexString(window), impl.getClass().getName(), methodName);
                            return window;
                        }
                    }
                } catch (NoSuchMethodException ignored) {
                    // Keep trying other method names and fallback fields.
                }
            }

            String[] fieldNames = {"current_window", "currentWindow", "window"};
            for (String fieldName : fieldNames) {
                try {
                    java.lang.reflect.Field field = impl.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(impl);
                    if (value instanceof Number) {
                        long window = ((Number) value).longValue();
                        if (window != 0) {
                            LOG.info("Got Linux window 0x{} via {}.{}", Long.toHexString(window), impl.getClass().getName(), fieldName);
                            return window;
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                    // Keep trying fallback fields.
                }
            }

            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get Linux window handle via LWJGL2: {} - {}", e.getClass().getSimpleName(), e.getMessage());
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

    private static long getLinuxWindowHandle() {
        long window = 0;
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

        if (hasGetWindow) {
            window = getWindowHandleLinux_LWJGL3();
        }
        if (window == 0 && hasGetImplementation) {
            window = getWindowHandleLinux_LWJGL2();
        }
        return window;
    }

    public static void destroyInputCtx() {
        LINUX_CALLBACK_QUEUE.clear();
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

        if (!Display.isCreated()) {
            LOG.warn("Display is not created yet, deferring InputContext creation");
            return;
        }

        int platform = LWJGLUtil.getPlatform();
        if (platform == LWJGLUtil.PLATFORM_WINDOWS) {
            long hWnd = getWindowHandle();
            if (hWnd == 0) {
                LOG.error("InputContext could not init as the hWnd is NULL!");
                return;
            }
            if (Minecraft.getMinecraft().isFullScreen()) {
                Config.UiLess_Windows = true;
                Config.sync();
            }
            int api = Config.API_Windows.equals("TextServiceFramework") ? 0 : 1;
            LOG.info("Using Windows API: {}, UiLess: {}", api, Config.UiLess_Windows);
            InputCtx = RustImeLibrary.createInputContext(hWnd, api, Config.UiLess_Windows);
        } else if (platform == LWJGLUtil.PLATFORM_LINUX) {
            long window = getLinuxWindowHandle();
            LOG.info("Using Linux backend, native window=0x{}", Long.toHexString(window));
            InputCtx = RustImeLibrary.createInputContext(window, 0, false);
        } else {
            LOG.error("Unsupported platform for context creation: {}", LWJGLUtil.getPlatformName());
            return;
        }

        if (InputCtx == 0) {
            LOG.error("Failed to create InputContext!");
            return;
        }

        LOG.info("InputContext has created!");
        LOG.info("Rust IME library version: {}", RustImeLibrary.getVersion());

        RustImeLibrary.initLogger(LOG);
        LOG.info("Rust logger initialized, forwarding to Log4j");

        RustImeLibrary.setMaxCandidates(InputCtx, Config.MaxCandidates);
        LOG.info("Max candidates set to: {}", Config.MaxCandidates);
        if (Config.DebugLog) {
            RustImeLibrary.setDebugLogging(true);
            LOG.info("Rust debug logging enabled");
        }

        // Setup callbacks
        preEditCallback = (state, content, cursor) -> {
            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX) {
                LINUX_CALLBACK_QUEUE.enqueue(() -> handlePreEditCallback(state, content, cursor));
            } else {
                handlePreEditCallback(state, content, cursor);
            }
        };

        commitCallback = text -> {
            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX) {
                LINUX_CALLBACK_QUEUE.enqueue(() -> handleCommitCallback(text));
            } else {
                handleCommitCallback(text);
            }
        };

        candidateListCallback = (state, candidates, selected) -> {
            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX) {
                LINUX_CALLBACK_QUEUE.enqueue(() -> handleCandidateListCallback(candidates, selected));
            } else {
                handleCandidateListCallback(candidates, selected);
            }
        };

        inputModeCallback = mode -> {
            if (LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_LINUX) {
                LINUX_CALLBACK_QUEUE.enqueue(() -> handleInputModeCallback(mode));
            } else {
                handleInputModeCallback(mode);
            }
        };

        // Register callbacks
        RustImeLibrary.setPreEditCallback(InputCtx, preEditCallback);
        RustImeLibrary.setCommitCallback(InputCtx, commitCallback);
        RustImeLibrary.setCandidateListCallback(InputCtx, candidateListCallback);
        RustImeLibrary.setInputModeCallback(InputCtx, inputModeCallback);

        System.gc();
    }

    private static void handlePreEditCallback(int state, String content, int cursor) {
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
    }

    private static void handleCommitCallback(String text) {
        try {
            int commitId = COMMIT_DEBUG_SEQUENCE.incrementAndGet();
            String commitText = UnicodeTextHelper.repairUtf8Mojibake(text);
            IngameIME_Forge.logDebugInfo(
                    "[IME Commit #{}] raw='{}' repaired='{}' rawUtf16=[{}] repairedUtf16=[{}] rawCp=[{}] repairedCp=[{}]",
                    commitId,
                    UnicodeTextHelper.debugEscaped(text),
                    UnicodeTextHelper.debugEscaped(commitText),
                    UnicodeTextHelper.debugUtf16(text),
                    UnicodeTextHelper.debugUtf16(commitText),
                    UnicodeTextHelper.debugCodePoints(text),
                    UnicodeTextHelper.debugCodePoints(commitText)
            );
            Minecraft.getMinecraft().addScheduledTask(() -> {
                try {
                    IngameIME_Forge.logDebugInfo(
                            "[IME Commit #{}] targetControl={} targetObject={}",
                            commitId,
                            IMStates.getActiveControl().getClass().getName(),
                            IMStates.getActiveControl().getControlObject() == null
                                    ? "null"
                                    : IMStates.getActiveControl().getControlObject().getClass().getName()
                    );
                    IMStates.getActiveControl().writeText(commitText);
                } catch (Throwable e) {
                    LOG.error("Exception in Commit callback", e);
                }
            });
        } catch (Throwable e) {
            LOG.error("Exception scheduling Commit task", e);
        }
    }

    private static void handleCandidateListCallback(String[] candidates, int selected) {
        try {
            if (candidates != null) {
                LOG.debug("CandidateList callback: received {} candidates, selected={}", candidates.length, selected);
                for (int i = 0; i < candidates.length; i++) {
                    IngameIME_Forge.logDebugInfo(
                            "[Java Candidates Raw]   [{}] '{}' utf16=[{}] cp=[{}]",
                            i,
                            UnicodeTextHelper.debugEscaped(candidates[i]),
                            UnicodeTextHelper.debugUtf16(candidates[i]),
                            UnicodeTextHelper.debugCodePoints(candidates[i])
                    );
                }
                // Keep one Java entry per native candidate so indexes match the IME.
                List<String> flattened = getStrings(candidates);
                LOG.debug("Normalized to {} candidates", flattened.size());
                for (int i = 0; i < flattened.size(); i++) {
                    String candidate = flattened.get(i);
                    IngameIME_Forge.logDebugInfo(
                            "[Java Candidates Flat]  [{}] '{}' utf16=[{}] cp=[{}]",
                            i,
                            UnicodeTextHelper.debugEscaped(candidate),
                            UnicodeTextHelper.debugUtf16(candidate),
                            UnicodeTextHelper.debugCodePoints(candidate)
                    );
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
    }

    private static void handleInputModeCallback(int mode) {
        try {
            // mode: 0=Alpha, 1=Native, 2=Unsupported
            ClientProxy.Screen.WInputMode.setMode(mode == 1);
        } catch (Throwable e) {
            LOG.error("Exception in InputMode callback", e);
        }
    }

    @Nonnull
    private static List<String> getStrings(String[] candidates) {
        List<String> out = new ArrayList<>(candidates.length);
        for (String candidate : candidates) {
            if (candidate == null) {
                out.add("");
                continue;
            }
            out.add(UnicodeTextHelper.repairUtf8Mojibake(candidate.trim()));
        }
        return out;
    }

    static void loadLibrary() {
        int platform = LWJGLUtil.getPlatform();
        if (platform == LWJGLUtil.PLATFORM_WINDOWS) {
            tryLoadLibrary("ingameime_core.dll");
        } else if (platform == LWJGLUtil.PLATFORM_LINUX) {
            String waylandDisplay = System.getenv("WAYLAND_DISPLAY");
            boolean preferWayland = waylandDisplay != null && !waylandDisplay.trim().isEmpty();
            if (preferWayland) {
                tryLoadLibrary("libingameime_core_wayland.so");
                if (!LIBRARY_LOADED) {
                    tryLoadLibrary("libingameime_core_x11.so");
                }
            } else {
                tryLoadLibrary("libingameime_core_x11.so");
                if (!LIBRARY_LOADED) {
                    tryLoadLibrary("libingameime_core_wayland.so");
                }
            }
        } else {
            LOG.error("Unsupported platform: {}", LWJGLUtil.getPlatformName());
            return;
        }

        if (!LIBRARY_LOADED) {
            LOG.error("Unsupported arch: {}", System.getProperty("os.arch"));
        }
    }

    public static boolean getActivated() {
        if (InputCtx != 0) return RustImeLibrary.isInputContextActivated(InputCtx);
        else return false;
    }

    public static void forceAlphaMode() {
        if (!LIBRARY_LOADED || InputCtx == 0 || forceAlphaApiUnavailable) {
            return;
        }
        try {
            RustImeLibrary.forceAlphaMode(InputCtx);
        } catch (UnsatisfiedLinkError e) {
            forceAlphaApiUnavailable = true;
            LOG.warn("Rust force alpha API is unavailable in current native library: {}", e.getClass().getSimpleName());
        } catch (Throwable t) {
            LOG.error("Failed to force alpha mode", t);
        }
    }

    public static void forceNativeMode() {
        if (!LIBRARY_LOADED || InputCtx == 0 || forceNativeApiUnavailable) {
            return;
        }
        try {
            RustImeLibrary.forceNativeMode(InputCtx);
        } catch (UnsatisfiedLinkError e) {
            forceNativeApiUnavailable = true;
            LOG.warn("Rust force native API is unavailable in current native library: {}", e.getClass().getSimpleName());
        } catch (Throwable t) {
            LOG.error("Failed to force native mode", t);
        }
    }

    public static boolean processLinuxKeyEvent(long keyval, int x11Keycode, int state, boolean release) {
        if (LWJGLUtil.getPlatform() != LWJGLUtil.PLATFORM_LINUX) {
            LOG.error("Linux key event bridge called on platform: {}", LWJGLUtil.getPlatformName());
            return false;
        }

        final int jniKeyval;
        final int ibusKeycode;
        try {
            jniKeyval = LinuxKeyEventConverter.toJniKeyval(keyval);
            ibusKeycode = LinuxKeyEventConverter.toIbusKeycode(x11Keycode);
            LinuxKeyEventConverter.validateState(state);
        } catch (IllegalArgumentException e) {
            LOG.error("Rejected invalid X11 key event", e);
            return false;
        }

        if (!LIBRARY_LOADED || InputCtx == 0) {
            if (!linuxKeyBridgeUnavailableLogged) {
                LOG.error(
                    "Linux key event bridge is unavailable: libraryLoaded={}, inputContext=0x{}",
                    LIBRARY_LOADED,
                    Long.toHexString(InputCtx));
                linuxKeyBridgeUnavailableLogged = true;
            }
            return false;
        }
        if (!RustImeLibrary.isInputContextActivated(InputCtx)) {
            return false;
        }

        linuxKeyBridgeUnavailableLogged = false;
        try {
            return RustImeLibrary.processKeyEvent(InputCtx, jniKeyval, ibusKeycode, state, release);
        } catch (Throwable t) {
            LOG.error("Failed to process Linux key event", t);
            return false;
        } finally {
            LINUX_CALLBACK_QUEUE.drain();
        }
    }

    public static void pollLinuxEvents() {
        if (LWJGLUtil.getPlatform() != LWJGLUtil.PLATFORM_LINUX || !LIBRARY_LOADED || InputCtx == 0) {
            return;
        }

        try {
            RustImeLibrary.pollEvents(InputCtx);
        } catch (Throwable t) {
            LOG.error("Failed to poll Linux IME events", t);
        } finally {
            LINUX_CALLBACK_QUEUE.drain();
        }
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
