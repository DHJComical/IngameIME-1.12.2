package com.dhj.ingameime;

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
    public static long InputCtx = 0;
    static RustImeLibrary.PreEditCallback preEditCallback = null;
    static RustImeLibrary.CommitCallback commitCallback = null;
    static RustImeLibrary.CandidateListCallback candidateListCallback = null;
    static RustImeLibrary.InputModeCallback inputModeCallback = null;
    private static boolean forceAlphaApiUnavailable = false;
    private static boolean forceNativeApiUnavailable = false;

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
        if (!LIBRARY_LOADED) try {
            prepareRustJniBinding();
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
        try {
            String[] possibleClasses = {
                    "org.lwjgl3.glfw.GLFWNativeWin32",
                    "org.lwjgl3.system.windows.GLFWNativeWin32",
                    "org.lwjgl.glfw.GLFWNativeWin32",
                    "org.lwjgl.system.windows.GLFWNativeWin32"
            };
            for (String className : possibleClasses) {
                try {
                    Class<?> nativeClass = Class.forName(className);
                    Method getWin32Window = nativeClass.getMethod("glfwGetWin32Window", long.class);
                    long hwnd = (long) getWin32Window.invoke(null, glfwWindow);
                    if (hwnd != 0) {
                        LOG.info("Successfully got Win32 HWND 0x{} via {}", Long.toHexString(hwnd), className);
                        return hwnd;
                    }
                } catch (ClassNotFoundException ignored) {
                } catch (NoSuchMethodException ignored) {
                }
            }
            LOG.warn("Could not find any GLFWNativeWin32 class to convert GLFW window to HWND");
            return 0;
        } catch (Throwable e) {
            LOG.error("Exception while calling glfwGetWin32Window: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

    private static long getWindowHandleLWJGL3() {
        try {
            Method getWindow = Display.class.getMethod("getWindow");
            long glfwWindow = (long) getWindow.invoke(null);
            if (glfwWindow == 0) return 0;
            return callGlfwGetWin32Window(glfwWindow);
        } catch (NoSuchMethodException e) {
            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get window handle via LWJGL3: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

    private static long getWindowHandleLWJGL2() {
        try {
            Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
            getImplementation.setAccessible(true);
            Object impl = getImplementation.invoke(null);

            if (impl == null) return 0;

            String[] possibleClasses = {
                    "org.lwjgl.opengl.WindowsDisplay",
                    "org.lwjgl.opengl.Win32Display",
                    "org.lwjgl.opengl.Display$WindowsDisplay"
            };
            for (String className : possibleClasses) {
                try {
                    Class<?> clsWindowsDisplay = Class.forName(className);
                    if (clsWindowsDisplay.isInstance(impl)) {
                        Method getHwnd = clsWindowsDisplay.getDeclaredMethod("getHwnd");
                        getHwnd.setAccessible(true);
                        long hwnd = (Long) getHwnd.invoke(impl);
                        if (hwnd != 0) return hwnd;
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }

            try {
                Method getHwnd = impl.getClass().getDeclaredMethod("getHwnd");
                getHwnd.setAccessible(true);
                long hwnd = (Long) getHwnd.invoke(impl);
                if (hwnd != 0) return hwnd;
            } catch (NoSuchMethodException ignored) {
            }

            try {
                java.lang.reflect.Field hwndField = impl.getClass().getDeclaredField("hwnd");
                hwndField.setAccessible(true);
                Object hwnd = hwndField.get(impl);
                if (hwnd instanceof Long && (Long) hwnd != 0) return (Long) hwnd;
            } catch (NoSuchFieldException ignored) {
            }

            return 0;
        } catch (NoSuchMethodException e) {
            return 0;
        } catch (Throwable e) {
            LOG.warn("Failed to get window handle via LWJGL2: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return 0;
        }
    }

    private static long getWindowHandle() {
        long hWnd = 0;

        try {
            Display.class.getMethod("getWindow");
            hWnd = getWindowHandleLWJGL3();
        } catch (NoSuchMethodException ignored) {
        }

        if (hWnd == 0) {
            hWnd = getWindowHandleLWJGL2();
        }

        if (hWnd == 0) {
            LOG.error("Failed to obtain window handle from all available LWJGL methods");
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
            int api = Config.API_Windows.equals("TextServiceFramework") ? 0 : 1;
            LOG.info("Using API: {}, UiLess: {}", api, Config.UiLess_Windows);
            InputCtx = RustImeLibrary.createInputContextWin32(hWnd, api, Config.UiLess_Windows);
            if (InputCtx == 0) {
                LOG.error("Failed to create InputContext!");
                return;
            }
            LOG.info("InputContext has created!");
            LOG.info("Rust IME library version: {}", RustImeLibrary.getVersion());
            try {
                RustImeLibrary.initLogger();
                LOG.info("Rust logger initialized, forwarding to Log4j");
            } catch (Throwable e) {
                LOG.warn("Rust logger bridge initialization failed: {}", e.getClass().getSimpleName());
            }
            try {
                RustImeLibrary.setMaxCandidates(InputCtx, Config.MaxCandidates);
                LOG.info("Max candidates set to: {}", Config.MaxCandidates);
            } catch (Throwable e) {
                LOG.warn("Setting max candidates failed: {}", e.getClass().getSimpleName());
            }
            if (Config.DebugLog) {
                try {
                    RustImeLibrary.setDebugLogging(true);
                    LOG.info("Rust debug logging enabled");
                } catch (Throwable e) {
                    LOG.warn("Enabling Rust debug logging failed: {}", e.getClass().getSimpleName());
                }
            }
        } else {
            LOG.error("InputContext could not init as the hWnd is NULL!");
            return;
        }

        preEditCallback = (state, content, cursor) -> {
            try {
                if (state == 0) {
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
                IMStates.getActiveControl().writeText(text);
            } catch (Throwable e) {
                LOG.error("Exception in Commit callback", e);
            }
        };

        candidateListCallback = (state, candidates, selected) -> {
            try {
                if (candidates != null) {
                    List<String> flattened = getStrings(candidates);
                    if (flattened.size() > Config.MaxCandidates) {
                        flattened = new ArrayList<>(flattened.subList(0, Config.MaxCandidates));
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
                ClientProxy.Screen.WInputMode.setMode(mode == 1);
            } catch (Throwable e) {
                LOG.error("Exception in InputMode callback", e);
            }
        };

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
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < candidate.length(); i++) {
                char c = candidate.charAt(i);
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

        tryLoadLibrary("ingameime_core.dll");

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
                IngameIME_Forge.logDebugInfo("Destroying stale InputContext...");
                destroyInputCtx();

                IngameIME_Forge.logDebugInfo("Recreating new InputContext...");
                createInputCtx();

                if (InputCtx != 0) {
                    try {
                        RustImeLibrary.setInputContextActivated(InputCtx, activated);
                    } catch (Throwable retryError) {
                        LOG.error("Failed to set active state even after recovery.", retryError);
                    }
                }
            } catch (Throwable recoveryError) {
                LOG.error("A critical error occurred during the recovery process itself.", recoveryError);
            }
        }
    }
}
