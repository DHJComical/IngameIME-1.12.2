package dhj.ingameime;

import dhj.ingameime.mixins.MixinGuiScreen;
import ingameime.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import static dhj.ingameime.IngameIME_Forge.LOG;

public class Internal {
    public static boolean LIBRARY_LOADED = false;
    public static InputContext InputCtx = null;
    static PreEditCallbackImpl preEditCallbackProxy = null;
    static CommitCallbackImpl commitCallbackProxy = null;
    static CandidateListCallbackImpl candidateListCallbackProxy = null;
    static InputModeCallbackImpl inputModeCallbackProxy = null;
    static PreEditCallback preEditCallback = null;
    static CommitCallback commitCallback = null;
    static CandidateListCallback candidateListCallback = null;
    static InputModeCallback inputModeCallback = null;

    private static void tryLoadLibrary(String libName) {
        if (!LIBRARY_LOADED) try {
            InputStream lib = IngameIME.class.getClassLoader().getResourceAsStream(libName);
            if (lib == null) throw new RuntimeException("Required library resource not exist!");
            Path path = Files.createTempFile("IngameIME-Native", null);
            Files.copy(lib, path, StandardCopyOption.REPLACE_EXISTING);
            System.load(path.toString());
            LIBRARY_LOADED = true;
            LOG.info("Library [{}] has loaded!", libName);
        } catch (Throwable e) {
            LOG.warn("Try to load library [{}] but failed: {}", libName, e.getClass().getSimpleName());
        }
        else LOG.info("Library has loaded, skip loading of [{}]", libName);
    }

    private static long getWindowHandle_LWJGL3() {
        try {
            Method getWindow = Display.class.getMethod("getWindow");
            Class<?> NativeWin32 = Class.forName("org.lwjgl.glfw.GLFWNativeWin32");
            long glfwWindow = (long) getWindow.invoke(null);
            Method glfwGetWin32Window = NativeWin32.getMethod("glfwGetWin32Window", long.class);
            return (long) glfwGetWin32Window.invoke(null, glfwWindow);
        } catch (Throwable e) {
            LOG.error("Failed to get window handle", e);
            return 0;
        }
    }

    private static long getWindowHandle_LWJGL2() {
        try {
            Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
            getImplementation.setAccessible(true);
            Object impl = getImplementation.invoke(null);
            Class<?> clsWindowsDisplay = Class.forName("org.lwjgl.opengl.WindowsDisplay");
            Method getHwnd = clsWindowsDisplay.getDeclaredMethod("getHwnd");
            getHwnd.setAccessible(true);
            return (Long) getHwnd.invoke(impl);
        } catch (Throwable e) {
            LOG.error("Failed to get window handle", e);
            return 0;
        }
    }

    public static void destroyInputCtx() {
        if (InputCtx != null) {
            InputCtx.delete();
            InputCtx = null;
            LOG.info("InputContext has destroyed!");
        }
    }

    public static void createInputCtx() {
        if (!LIBRARY_LOADED) return;

        LOG.info("Using IngameIME-Native: {}", InputContext.getVersion());

        long hWnd = Loader.isModLoaded("cleanroom") ? getWindowHandle_LWJGL3() : getWindowHandle_LWJGL2();
        if (hWnd != 0) {
            if (Minecraft.getMinecraft().isFullScreen()) Config.UiLess_Windows.set(true);
            API api = Config.API_Windows.getString().equals("TextServiceFramework") ? API.TextServiceFramework : API.Imm32;
            LOG.info("Using API: {}, UiLess: {}", api, Config.UiLess_Windows.getBoolean());
            InputCtx = IngameIME.CreateInputContextWin32(hWnd, api, Config.UiLess_Windows.getBoolean());
            LOG.info("InputContext has created!");
        } else {
            LOG.error("InputContext could not init as the hWnd is NULL!");
            return;
        }

        preEditCallbackProxy = new PreEditCallbackImpl() {
            @Override
            protected void call(CompositionState arg0, PreEditContext arg1) {
                try {
                    LOG.info("PreEdit State: {}", arg0);
                    if (arg0 == CompositionState.Begin) ClientProxy.Screen.WInputMode.setActive(false);
                    if (arg1 != null) ClientProxy.Screen.PreEdit.setContent(arg1.getContent(), arg1.getSelStart());
                    else ClientProxy.Screen.PreEdit.setContent(null, -1);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        preEditCallback = new PreEditCallback(preEditCallbackProxy);

        commitCallbackProxy = new CommitCallbackImpl() {
            @Override
            protected void call(String text) {
                try {
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        try {
                            if (Loader.isModLoaded("jei") && IMStates.ActiveControl != null &&
                                    IMStates.ActiveControl.getClass().getName().equals("mezz.jei.input.GuiTextFieldFilter")) {

                                LOG.info("JEI text field detected, using JEI API to set text.");
                                String oldText = JEICompat.getJEIFilterText();
                                JEICompat.setJEIFilterText(oldText + text);

                            } else {
                                final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                                if (screen != null) {
                                    for (char c : text.toCharArray()) {
                                        ((MixinGuiScreen) screen).callKeyTyped(c, Keyboard.KEY_NONE);
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            LOG.error("Exception thrown during scheduled commit task", e);
                        }
                    });
                } catch (Throwable e) {
                    LOG.error("Exception thrown when scheduling commit callback", e);
                }
            }
        };
        commitCallback = new CommitCallback(commitCallbackProxy);

        candidateListCallbackProxy = new CandidateListCallbackImpl() {
            @Override
            protected void call(CandidateListState arg0, CandidateListContext arg1) {
                try {
                    if (arg1 != null)
                        ClientProxy.Screen.CandidateList.setContent(new ArrayList<>(arg1.getCandidates()), arg1.getSelection());
                    else ClientProxy.Screen.CandidateList.setContent(null, -1);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        candidateListCallback = new CandidateListCallback(candidateListCallbackProxy);
        inputModeCallbackProxy = new InputModeCallbackImpl() {
            @Override
            protected void call(InputMode arg0) {
                try {
                    ClientProxy.Screen.WInputMode.setMode(arg0);
                } catch (Throwable e) {
                    LOG.error("Exception thrown during callback handling", e);
                }
            }
        };
        inputModeCallback = new InputModeCallback(inputModeCallbackProxy);

        InputCtx.setCallback(preEditCallback);
        InputCtx.setCallback(commitCallback);
        InputCtx.setCallback(candidateListCallback);
        InputCtx.setCallback(inputModeCallback);

        System.gc();
    }

    static void loadLibrary() {
        boolean isWindows = LWJGLUtil.getPlatform() == LWJGLUtil.PLATFORM_WINDOWS;

        if (!isWindows) {
            LOG.info("Unsupported platform: {}", LWJGLUtil.getPlatformName());
            return;
        }

        tryLoadLibrary("IngameIME_Java-arm64.dll");
        tryLoadLibrary("IngameIME_Java-x64.dll");
        tryLoadLibrary("IngameIME_Java-x86.dll");

        if (!LIBRARY_LOADED) {
            LOG.error("Unsupported arch: {}", System.getProperty("os.arch"));
        }
    }

    public static boolean getActivated() {
        if (InputCtx != null) return InputCtx.getActivated();
        else return false;
    }

    public static void setActivated(boolean activated) {
        if (InputCtx != null && getActivated() != activated) {
            InputCtx.setActivated(activated);
            LOG.info("IM active state: {}", activated);
        }
    }
}
